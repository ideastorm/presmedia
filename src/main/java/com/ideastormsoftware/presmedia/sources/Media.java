/*
 * Copyright 2015 Phillip Hayward <phil@pjhayward.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.FileNotFoundException;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.interrupted;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.ShortPointer;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_DBL;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_DBLP;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_FLT;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_FLTP;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_NONE;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_S16;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_S16P;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_S32;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_S32P;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_U8;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_U8P;
import static org.bytedeco.javacpp.avutil.av_get_sample_fmt_name;

public class Media implements Supplier<BufferedImage>, CleanCloseable, Startable {

    private SourceDataLine mLine;
    private AudioConverter converter;
    private AudioFormat audioFormat;
    private final Runnable callback;

    private final String sourceFile;
    private final Object imageLock = new Object();
    private BufferedImage currentImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);

    private static void log(String format, Object... params) {
        System.out.println(String.format("%d %s - %s", System.currentTimeMillis(),
                Thread.currentThread().getName(), String.format(format, params)));
    }

    public Media(String sourceFile, Runnable callback) {
        this.sourceFile = sourceFile;
        this.callback = callback;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    @Override
    public void start() {
        openSource();
    }

    @Override
    public void close() {
        closeSource();
    }

    @Override
    public BufferedImage get() {
        synchronized (imageLock) {
            return currentImage;
        }
    }

    private void openJavaSound(int sampleFormat, int audioChannels, float sampleRate) throws FileNotFoundException {
        int bitsPerSample;
        boolean signed;
        switch (sampleFormat) {
            case AV_SAMPLE_FMT_NONE:
                throw new IllegalStateException("no sample format");
            case AV_SAMPLE_FMT_U8:
                bitsPerSample = 8;
                converter = new InterleavedConverter();
                signed = false;
                break;
            case AV_SAMPLE_FMT_S16:
                bitsPerSample = 16;
                converter = new InterleavedShortConverter();
                signed = true;
                break;
            case AV_SAMPLE_FMT_S32:
                bitsPerSample = 16;
                converter = new InterleavedIntConverter();
                signed = true;
                break;
            case AV_SAMPLE_FMT_FLT:
                bitsPerSample = 16;
                converter = new InterleavedFltConverter();
                signed = true;
                break;
            case AV_SAMPLE_FMT_S16P:
                bitsPerSample = 16;
                converter = new PlanarConverter(bitsPerSample, audioChannels);
                signed = true;
                break;
            case AV_SAMPLE_FMT_S32P:
                bitsPerSample = 32;
                converter = new PlanarIntConverter(audioChannels);
                signed = true;
                break;
            case AV_SAMPLE_FMT_FLTP:
                bitsPerSample = 16;
                converter = new PlanarFltConverter(audioChannels);
                signed = true;
                break;
            case AV_SAMPLE_FMT_U8P:
                bitsPerSample = 8;
                converter = new PlanarConverter(bitsPerSample, audioChannels);
                signed = false;
                break;
            case AV_SAMPLE_FMT_DBL:
                bitsPerSample = 16;
                converter = new InterleavedDblConverter();
                signed = true;
                break;
            case AV_SAMPLE_FMT_DBLP:
                bitsPerSample = 16;
                converter = new PlanarDblConverter(audioChannels);
                signed = true;
                break;
            default:
                log("No support for %d %s", sampleFormat, bytePointerToString(av_get_sample_fmt_name(sampleFormat)));
                return;
        }
        audioFormat = new AudioFormat(
                sampleRate,
                bitsPerSample,
                audioChannels,
                signed,
                false);
        if (audioChannels > 0) {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            try {
                mLine = (SourceDataLine) AudioSystem.getLine(info);
                mLine.open(audioFormat);
                mLine.start();
            } catch (LineUnavailableException e) {
                throw new RuntimeException("could not open audio line");
            }
        }
    }

    private void closeJavaSound() {
        if (mLine != null) {
            mLine.drain();
            mLine.close();
            mLine = null;
        }
    }

    private GrabberThread grabber;

    private void openSource() {
        log("initializing new processing thread");
        grabber = new GrabberThread();
        grabber.start();
        log("processing thread %s started", grabber.getName());
    }

    private void closeSource() {
        try {
            log("shutting down processing thread");
            if (grabber != null) {
                log("shutting down processing thread.%s", grabber.getName());
                grabber.interrupt();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            grabber = null;
        }
    }

    private String bytePointerToString(BytePointer ptr) {
        byte[] bytes = new byte[ptr.limit()];
        ptr.get(bytes);
        return new String(bytes);
    }

    private class GrabberThread extends Thread {

        private volatile boolean canceled = false;

        @Override
        public void run() {
            log("Media processing thread initialized");
            boolean normalExit = false;
            AudioThread audioThread = new AudioThread();
            VideoThread videoThread = new VideoThread();
            Stats audioStats = new Stats();
            Stats videoStats = new Stats();
            Stats audioBuffer = new Stats();
            Stats videoBuffer = new Stats();
            Stats audioProcessing = new Stats();
            Stats videoProcessing = new Stats();
            try {
                try {
                    FFmpegFrameGrabber ffmpeg = FFmpegFrameGrabber.createDefault(getSourceFile());
                    try {
                        ffmpeg.start();
                        openJavaSound(ffmpeg.getSampleFormat(), ffmpeg.getAudioChannels(), ffmpeg.getSampleRate());
                        log("sound system initialized");
                        videoThread.setFrameRate(ffmpeg.getFrameRate());
                        boolean started = false;
                        boolean gotAudio = ffmpeg.getAudioBitrate() == 0; //bitrate = 0 means no audio, so don't wait
                        boolean gotVideo = ffmpeg.getVideoBitrate() == 0; //same for video
                        while (!canceled && !interrupted()) {
                            try {
                                long start = System.nanoTime();
                                Frame frame = ffmpeg.grabFrame();
                                long captureTime = System.nanoTime() - start;
                                if (frame != null) {
                                    audioBuffer.addValue(audioThread.samples.size());
                                    videoBuffer.addValue(videoThread.frames.size());
                                    if (frame.samples != null) {
                                        audioStats.addValue(captureTime);
                                        audioThread.samples.put(converter.prepareSamplesForPlayback(frame.samples));
                                        audioProcessing.addValue(System.nanoTime() - start);
                                        gotAudio = true;
                                    }
                                    if (frame.image != null) {
                                        videoStats.addValue(captureTime);
                                        videoThread.frames.put(ImageUtils.copy(frame.image));
                                        videoProcessing.addValue(System.nanoTime() - start);
                                        gotVideo = true;
                                    }
                                } else {
                                    normalExit = true;
                                    break;
                                }
                                if (gotAudio && gotVideo && !started) {
                                    audioThread.start();
                                    videoThread.start();
                                    started = true;
                                }
                            } catch (InterruptedException ex) {
                                log("got interrupted");
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                                synchronized (imageLock) {
                                    currentImage = ImageUtils.emptyImage();
                                }
                            }
                        }
                        if (normalExit) {
                            try {
                                while (!audioThread.samples.isEmpty() && !videoThread.frames.isEmpty()) {
                                    Thread.sleep(5);
                                }
                            } catch (InterruptedException e) {
                                log("got interrupted waiting for buffers to flush");
                                return;
                            }
                        }
                    } finally {
                        audioThread.canceled = true;
                        videoThread.canceled = true;
                        audioThread.interrupt();
                        videoThread.interrupt();
                        ffmpeg.stop();
                        ffmpeg.release();
                        closeJavaSound();
                        log("processing thread terminated");
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }

            } finally {
                log("Audio frames:\n\tmin: %d\n\tmax: %d\n\tavg: %d\n\tcount: %d", audioStats.getMin(), audioStats.getMax(), audioStats.getAverage(), audioStats.getCount());
                log("Video frames:\n\tmin: %d\n\tmax: %d\n\tavg: %d\n\tcount: %d", videoStats.getMin(), videoStats.getMax(), videoStats.getAverage(), videoStats.getCount());
                log("Audio buffer:\n\tmin: %d\n\tmax: %d\n\tavg: %d\n\tcount: %d", audioBuffer.getMin(), audioBuffer.getMax(), audioBuffer.getAverage(), audioBuffer.getCount());
                log("Video buffer:\n\tmin: %d\n\tmax: %d\n\tavg: %d\n\tcount: %d", videoBuffer.getMin(), videoBuffer.getMax(), videoBuffer.getAverage(), videoBuffer.getCount());
                log("Audio processing:\n\tmin: %d\n\tmax: %d\n\tavg: %d\n\tcount: %d", audioProcessing.getMin(), audioProcessing.getMax(), audioProcessing.getAverage(), audioProcessing.getCount());
                log("Video processing:\n\tmin: %d\n\tmax: %d\n\tavg: %d\n\tcount: %d", videoProcessing.getMin(), videoProcessing.getMax(), videoProcessing.getAverage(), videoProcessing.getCount());
                log("exited thread lock - ready to go with the next thread");
                if (normalExit) {
                    callback.run();
                }
            }
        }

        @Override
        public void interrupt() {
            canceled = true;
            log("processing thread %s interrupted by %s", getName(), currentThread().getName());
            super.interrupt();
        }
    }

    private interface AudioConverter {

        byte[] prepareSamplesForPlayback(Buffer[] samples);
    }

    private static Buffer convertDblToShortBuffer(DoubleBuffer buffer) {
        double[] bufferData;
        if (buffer.hasArray()) {
            bufferData = buffer.array();
        } else {
            bufferData = new double[buffer.limit()];
            buffer.get(bufferData);
        }
        ShortBuffer shortBuffer = ShortBuffer.allocate(bufferData.length);
        for (double c : bufferData) {
            shortBuffer.put((short) (c * Short.MAX_VALUE));
        }
        shortBuffer.rewind();
        return new ShortPointer(shortBuffer).asByteBuffer();
    }

    private static Buffer convertFltToShortBuffer(FloatBuffer buffer) {
        float[] bufferData;
        if (buffer.hasArray()) {
            bufferData = buffer.array();
        } else {
            bufferData = new float[buffer.limit()];
            buffer.get(bufferData);
        }
        ShortBuffer shortBuffer = ShortBuffer.allocate(bufferData.length);
        for (double c : bufferData) {
            shortBuffer.put((short) (c * Short.MAX_VALUE));
        }
        shortBuffer.rewind();
        return new ShortPointer(shortBuffer).asByteBuffer();
    }

    private static Buffer convertIntToShortBuffer(IntBuffer buffer) {
        int[] bufferData;
        if (buffer.hasArray()) {
            bufferData = buffer.array();
        } else {
            bufferData = new int[buffer.limit()];
            buffer.get(bufferData);
        }
        ShortBuffer shortBuffer = ShortBuffer.allocate(bufferData.length);
        for (double c : bufferData) {
            shortBuffer.put((short) (((double) c) / Integer.MAX_VALUE * Short.MAX_VALUE));
        }
        shortBuffer.rewind();
        return new ShortPointer(shortBuffer).asByteBuffer();
    }

    private static class PlanarFltConverter extends PlanarConverter {

        public PlanarFltConverter(int channels) {
            super(16, channels);
        }

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {
            Buffer[] convertedSamples = new Buffer[samples.length];
            for (int i = 0; i < samples.length; i++) {
                convertedSamples[i] = convertFltToShortBuffer((FloatBuffer) samples[i]);
            }
            return super.prepareSamplesForPlayback(convertedSamples);
        }

    }

    private static class PlanarDblConverter extends PlanarConverter {

        public PlanarDblConverter(int channels) {
            super(16, channels);
        }

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {
            Buffer[] convertedSamples = new Buffer[samples.length];
            for (int i = 0; i < samples.length; i++) {
                convertedSamples[i] = convertDblToShortBuffer((DoubleBuffer) samples[i]);
            }
            return super.prepareSamplesForPlayback(convertedSamples);
        }

    }

    private static class PlanarIntConverter extends PlanarConverter {

        public PlanarIntConverter(int channels) {
            super(16, channels);
        }

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {
            Buffer[] convertedSamples = new Buffer[samples.length];
            for (int i = 0; i < samples.length; i++) {
                convertedSamples[i] = convertIntToShortBuffer((IntBuffer) samples[i]);
            }
            return super.prepareSamplesForPlayback(convertedSamples);
        }

    }

    private static class InterleavedDblConverter extends InterleavedConverter {

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {
            DoubleBuffer buffer = (DoubleBuffer) samples[0];
            Buffer[] convertedSamples = {convertDblToShortBuffer(buffer)};
            return super.prepareSamplesForPlayback(convertedSamples);
        }

    }

    private static class InterleavedFltConverter extends InterleavedConverter {

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {
            FloatBuffer buffer = (FloatBuffer) samples[0];
            Buffer[] convertedSamples = {convertFltToShortBuffer(buffer)};
            return super.prepareSamplesForPlayback(convertedSamples);
        }

    }

    private static class InterleavedIntConverter extends InterleavedConverter {

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {
            IntBuffer buffer = (IntBuffer) samples[0];
            Buffer[] convertedSamples = {convertIntToShortBuffer(buffer)};
            return super.prepareSamplesForPlayback(convertedSamples);
        }

    }

    private static class PlanarConverter implements AudioConverter {

        private final int bitSampleSize;
        private final int channels;

        public PlanarConverter(int bitSampleSize, int channels) {
            this.bitSampleSize = bitSampleSize;
            this.channels = channels;
        }

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {

            int sampleByteSize = (bitSampleSize + 7) / 8;
            int frameSize = channels * sampleByteSize;
            int frames = ((ByteBuffer) samples[0]).limit() / sampleByteSize;
            byte[] buffer = new byte[frames * frameSize];
            for (int frame = 0; frame < frames; frame++) {
                int frameOffset = frame * frameSize;
                for (int channel = 0; channel < channels; channel++) {
                    int channelOffset = channel * sampleByteSize;
                    ((ByteBuffer) samples[channel]).get(buffer, frameOffset + channelOffset, sampleByteSize);
                }
            }
            return buffer;
        }
    }

    private static class InterleavedShortConverter extends InterleavedConverter {

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {
            ShortPointer ptr = new ShortPointer((ShortBuffer) samples[0]);
            samples[0] = ptr.asByteBuffer();
            return super.prepareSamplesForPlayback(samples);
        }

    }

    private static class InterleavedConverter implements AudioConverter {

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {
            ByteBuffer buffer = (ByteBuffer) samples[0];
            byte[] bufferData;
            if (buffer.hasArray()) {
                bufferData = buffer.array();
            } else {
                bufferData = new byte[buffer.limit()];
                buffer.get(bufferData);
            }
            return bufferData;
        }
    }

    private class VideoThread extends Thread {

        volatile boolean canceled = false;
        private final BlockingQueue<BufferedImage> frames = new ArrayBlockingQueue<>(256);
        private double interframe = 1_000_000.0 / 29.97; //target microseconds per frame

        @Override
        public void run() {
            long startTime = System.nanoTime() / 1000;
            long index = 0;
            while (!canceled && !interrupted()) {
                try {
                    BufferedImage image = frames.take();
                    synchronized (imageLock) {
                        currentImage = image;
                    }
                    index++;
                    double targetTime = startTime + index * interframe;
                    long now = System.nanoTime() / 1000;
                    if (now < targetTime) {
                        TimeUnit.MICROSECONDS.sleep((long) (targetTime - now));
                    }
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }

        private void setFrameRate(double frameRate) {
            this.interframe = 1_000_000 / frameRate;
        }
    }

    private class AudioThread extends Thread {

        volatile boolean canceled = false;
        final BlockingQueue<byte[]> samples = new ArrayBlockingQueue<byte[]>(256);

        @Override
        public void run() {
            while (!canceled && !interrupted()) {
                try {
                    byte[] buffer = samples.take();
                    if (mLine != null) {
                        mLine.write(buffer, 0, buffer.length);
                    }
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }
    }
}
