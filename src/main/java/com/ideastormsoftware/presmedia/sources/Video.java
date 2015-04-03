package com.ideastormsoftware.presmedia.sources;

import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.bytedeco.javacpp.BytePointer;
import static org.bytedeco.javacpp.avutil.*;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

public class Video extends ImageSource {

    private SourceDataLine mLine;
    private AudioConverter converter;
    private AudioFormat audioFormat;
    private boolean planarFormat;

    private String sourceFile;
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
        return currentImage;
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        return false;
    }

    @Override
    public void replaceSource(ImageSource source, ImageSource replacement) {
    }

    @Override
    protected String sourceDescription() {
        return "Video";
    }

    private void openJavaSound(FrameGrabber grabber) {
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
            case AV_SAMPLE_FMT_FLT:
                bitsPerSample = 32;
                converter = new InterleavedConverter();
                signed = true;
                break;
            case AV_SAMPLE_FMT_S16P:
                bitsPerSample = 16;
                converter = new PlanarConverter(bitsPerSample, grabber.getAudioChannels());
                signed = true;
                break;
            case AV_SAMPLE_FMT_S32P:
            case AV_SAMPLE_FMT_FLTP:
                bitsPerSample = 32;
                converter = new PlanarConverter(bitsPerSample, grabber.getAudioChannels());
                signed = true;
                break;
            case AV_SAMPLE_FMT_U8P:
                bitsPerSample = 8;
                converter = new PlanarConverter(bitsPerSample, grabber.getAudioChannels());
                signed = false;
                break;
            case AV_SAMPLE_FMT_DBL:
                bitsPerSample = 32;
                converter = new InterleavedDblConverter();
                signed = true;
                break;
            case AV_SAMPLE_FMT_DBLP:
                bitsPerSample = 32;
                converter = new PlanarDblConverter(grabber.getAudioChannels());
                signed = true;
                break;
            default:
                log("No support for %d %s", sampleFormat, bytePointerToString(av_get_sample_fmt_name(sampleFormat)));
                return;
        }
        planarFormat = av_sample_fmt_is_planar(sampleFormat) != 0;
        audioFormat = new AudioFormat(
                grabber.getSampleRate(),
                bitsPerSample,
                grabber.getAudioChannels(),
                signed,
                false);
        if (true) {
            return;
        }
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            mLine = (SourceDataLine) AudioSystem.getLine(info);
            mLine.open(audioFormat);
            mLine.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException("could not open audio line");
        }
    }

    private void playSamples(Buffer[] samples) {
        if (mLine != null) {
            byte[] buffer = converter.prepareSamplesForPlayback(samples);
            mLine.write(buffer, 0, buffer.length);
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
        byte[] bytes = new byte[ptr.capacity()];
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
            try {
                synchronized (threadLock) {
                    try {
                        FFmpegFrameGrabber ffmpeg = FFmpegFrameGrabber.createDefault(sourceFile);
                        try {

                            ffmpeg.start();
                            openJavaSound(ffmpeg);
                            log("sound system initialized");
                            long targetDuration = (long) (1_000_000_000 / ffmpeg.getFrameRate());
                            while (!canceled && !interrupted()) {
                                try {
                                    long loopStart = System.nanoTime();
                                    Frame frame = ffmpeg.grabFrame();
                                    if (frame != null) {
                                        if (frame.image != null) {
                                            currentImage = frame.image.getBufferedImage();
                                        }
                                        if (frame.samples != null) {
                                            playSamples(frame.samples);
                                        }
                                    } else {
                                        normalExit = true;
                                        break;
                                    }
                                    long loopDuration = System.nanoTime() - loopStart;
                                    TimeUnit.NANOSECONDS.sleep(targetDuration - loopDuration);
                                } catch (FrameGrabber.Exception e) {
                                    e.printStackTrace();
                                    currentImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
                                } catch (InterruptedException ex) {
                                    log("got interrupted");
                                    return;
                                }
                            }
                        } finally {
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

    private static IntBuffer convertDblToIntBuffer(DoubleBuffer buffer) {
        double[] bufferData;
        if (buffer.hasArray()) {
            bufferData = buffer.array();
        } else {
            bufferData = new double[buffer.capacity()];
            buffer.get(bufferData);
        }
        IntBuffer intBuffer = IntBuffer.allocate(bufferData.length);
        for (double c : bufferData) {
            intBuffer.put((int) (c * Integer.MAX_VALUE));
        }
        return intBuffer;
    }

    private static class PlanarDblConverter extends PlanarConverter {

        public PlanarDblConverter(int channels) {
            super(32, channels);
        }

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {
            Buffer[] convertedSamples = new Buffer[samples.length];
            for (int i = 0; i < samples.length; i++) {
                convertedSamples[i] = convertDblToIntBuffer((DoubleBuffer) samples[i]);
            }
            return super.prepareSamplesForPlayback(convertedSamples);
        }

    }

    private static class InterleavedDblConverter extends InterleavedConverter {

        @Override
        public byte[] prepareSamplesForPlayback(Buffer[] samples) {
            DoubleBuffer buffer = (DoubleBuffer) samples[0];
            Buffer[] convertedSamples = {convertDblToIntBuffer(buffer)};
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
            int frames = ((ByteBuffer) samples[0]).capacity() / sampleByteSize;
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
                bufferData = new byte[buffer.capacity()];
                buffer.get(bufferData);
            }
            return bufferData;
        }
    }
}
