package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.ShortPointer;
import static org.bytedeco.javacpp.avutil.*;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

public class Video extends ImageSource {

    private SourceDataLine mLine;
    private AudioConverter converter;
    private AudioFormat audioFormat;

    private final String sourceFile;
    private final Object imageLock = new Object();
    private BufferedImage currentImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);

    private static void log(String format, Object... params) {
        System.out.println(String.format("%d %s - %s", System.currentTimeMillis(),
                Thread.currentThread().getName(), String.format(format, params)));
    }
    private final Runnable callback;

    public Video(String sourceFile, Runnable callback) throws FrameGrabber.Exception {
        this.sourceFile = sourceFile;
        this.callback = callback;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void start() {
        try {
            openSourceFile();
        } catch (FrameGrabber.Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        try {
            closeSourceFile();
        } catch (FrameGrabber.Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public BufferedImage getCurrentImage() {
        synchronized (imageLock) {
            return currentImage;
        }
    }

    private void openJavaSound(FrameGrabber grabber) throws FileNotFoundException {
        int sampleFormat = grabber.getSampleFormat();
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
                converter = new InterleavedConverter();
                signed = false;
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
                converter = new PlanarConverter(bitsPerSample, grabber.getAudioChannels());
                signed = true;
                break;
            case AV_SAMPLE_FMT_S32P:
                bitsPerSample = 32;
                converter = new PlanarIntConverter(grabber.getAudioChannels());
                signed = true;
                break;
            case AV_SAMPLE_FMT_FLTP:
                bitsPerSample = 16;
                converter = new PlanarFltConverter(grabber.getAudioChannels());
                signed = true;
                break;
            case AV_SAMPLE_FMT_U8P:
                bitsPerSample = 8;
                converter = new PlanarConverter(bitsPerSample, grabber.getAudioChannels());
                signed = false;
                break;
            case AV_SAMPLE_FMT_DBL:
                bitsPerSample = 16;
                converter = new InterleavedDblConverter();
                signed = true;
                break;
            case AV_SAMPLE_FMT_DBLP:
                bitsPerSample = 16;
                converter = new PlanarDblConverter(grabber.getAudioChannels());
                signed = true;
                break;
            default:
                log("No support for %d %s", sampleFormat, bytePointerToString(av_get_sample_fmt_name(sampleFormat)));
                return;
        }
        audioFormat = new AudioFormat(
                grabber.getSampleRate(),
                bitsPerSample,
                grabber.getAudioChannels(),
                signed,
                false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            mLine = (SourceDataLine) AudioSystem.getLine(info);
            mLine.open(audioFormat);
            mLine.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException("could not open audio line");
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

    private void openSourceFile() throws FrameGrabber.Exception {
        log("initializing new processing thread");
        grabber = new GrabberThread(sourceFile);
        grabber.start();
        log("processing thread %s started", grabber.getName());
    }

    private void closeSourceFile() throws FrameGrabber.Exception {
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

    private static final Object threadLock = new Object();

    private String bytePointerToString(BytePointer ptr) {
        byte[] bytes = new byte[ptr.limit()];
        ptr.get(bytes);
        return new String(bytes);
    }

    private class GrabberThread extends Thread {

        private volatile boolean canceled = false;
        private final String sourceFile;

        private GrabberThread(String sourceFile) {
            this.sourceFile = sourceFile;
        }

        @Override
        public void run() {
            boolean normalExit = false;
            AudioThread audioThread = new AudioThread();
            audioThread.start();
            try {
                synchronized (threadLock) {
                    try {
                        FFmpegFrameGrabber ffmpeg = FFmpegFrameGrabber.createDefault(sourceFile);
                        try {
                            ffmpeg.start();
                            openJavaSound(ffmpeg);
                            log("sound system initialized");
                            long startTime = System.nanoTime() / 1000;
                            long expectedInterframe = (long) (900_000 / ffmpeg.getFrameRate());
                            while (!canceled && !interrupted()) {
                                try {
                                    long frameStart = System.nanoTime() / 1000;
                                    Frame frame = ffmpeg.grabFrame();
                                    if (frame != null) {
                                        if (frame.samples != null) {
                                            audioThread.samples.put(converter.prepareSamplesForPlayback(frame.samples));
                                            continue;
                                        }
                                        if (frame.image != null) {
                                            BufferedImage frameImage = ImageUtils.copy(frame.image.getBufferedImage());
                                            synchronized (imageLock) {
                                                currentImage = frameImage;
                                            }
                                        }
                                    } else {
                                        normalExit = true;
                                        break;
                                    }
                                    final long pts = ffmpeg.getTimestamp();
                                    long targetEnd = (long) (startTime + pts);
                                    if (Math.abs(targetEnd - frameStart - expectedInterframe) > 0.1 * expectedInterframe) {
                                        targetEnd = frameStart + expectedInterframe;
                                    }
                                    long delay = targetEnd - System.nanoTime() / 1000;
                                    if (delay > 0) {
                                        TimeUnit.MICROSECONDS.sleep(delay);
                                    }
                                } catch (FrameGrabber.Exception e) {
                                    e.printStackTrace();
                                    synchronized (imageLock) {
                                        currentImage = ImageUtils.emptyImage();
                                    }
                                } catch (InterruptedException ex) {
                                    log("got interrupted");
                                    return;
                                }
                            }
                        } finally {
                            audioThread.canceled = true;
                            audioThread.interrupt();
                            log("in finally");
                            ffmpeg.stop();
                            ffmpeg.release();
                            closeJavaSound();
                            log("processing thread terminated");
                        }
                        log("post finally");
                    } catch (Throwable ex) {
                        log("in catch");
                        ex.printStackTrace();
                    }

                    log("post catch");
                }
            } finally {
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

    private class AudioThread extends Thread {

        volatile boolean canceled = false;
        final BlockingQueue<byte[]> samples = new ArrayBlockingQueue<byte[]>(16);

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
