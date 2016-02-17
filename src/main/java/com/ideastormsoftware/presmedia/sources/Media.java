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

import com.ideastormsoftware.presmedia.util.Stats;
import com.ideastormsoftware.presmedia.sources.media.FFmpegFrameGrabber;
import com.ideastormsoftware.presmedia.sources.media.AvException;
import com.ideastormsoftware.presmedia.sources.media.InterleavedShortConverter;
import com.ideastormsoftware.presmedia.sources.media.PlanarDblConverter;
import com.ideastormsoftware.presmedia.sources.media.PlanarFltConverter;
import com.ideastormsoftware.presmedia.sources.media.InterleavedFltConverter;
import com.ideastormsoftware.presmedia.sources.media.InterleavedConverter;
import com.ideastormsoftware.presmedia.sources.media.PlanarConverter;
import com.ideastormsoftware.presmedia.sources.media.InterleavedDblConverter;
import com.ideastormsoftware.presmedia.sources.media.PlanarIntConverter;
import com.ideastormsoftware.presmedia.sources.media.InterleavedIntConverter;
import com.ideastormsoftware.presmedia.sources.media.AudioConverter;
import com.ideastormsoftware.presmedia.sources.media.PlanarShortConverter;
import com.ideastormsoftware.presmedia.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.interrupted;
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

public class Media implements ImageSource, CleanCloseable, Startable, Pauseable {

    private SourceDataLine mLine;
    private AudioConverter converter;
    private AudioFormat audioFormat;
    private final Runnable callback;
    private volatile boolean paused = false;
    private volatile boolean postSeekBuffer = false;
    private volatile int minimumSamples = 0;
    private volatile int minimumFrames = 0;
    private final BlockingQueue<DataFrame<byte[]>> samples = new ArrayBlockingQueue<>(256);
    private final BlockingQueue<DataFrame<BufferedImage>> frames = new ArrayBlockingQueue<>(256);
    private FFmpegFrameGrabber ffmpeg;

    private final String sourceFile;
    private final Object imageLock = new Object();
    private BufferedImage currentImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
    private Stats fpsStats = new Stats();

    private static void log(String format, Object... params) {
        System.out.println(String.format("%d %s - %s", System.currentTimeMillis(),
                Thread.currentThread().getName(), String.format(format, params)));
    }
    private long mediaPosition;
    private long mediaDuration;

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

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void togglePaused() {
        paused = !paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void seekTo(long position) throws AvException {
        if (position >= mediaDuration) {
            return;
        }
        boolean pauseState = paused;
        setPaused(true);
        ffmpeg.setTimestamp(position);
        frames.clear();
        samples.clear();
        postSeekBuffer = true;
        do {
            delay(2);
        } while (frames.size() < minimumFrames && samples.size() < minimumSamples);
        updatePausedImage();
        setMediaPosition(position);
        setPaused(pauseState);
        postSeekBuffer = false;
    }

    public long getMediaDuration() {
        return mediaDuration;
    }

    public int getAudioBufferLoad() {
        return samples.size() * 100 / 256;
    }

    public int getVideoBufferLoad() {
        return frames.size() * 100 / 256;
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
                converter = new PlanarShortConverter(audioChannels);
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

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
        }
    }

    @Override
    public double getFps() {
        return fpsStats.getRate();
    }

    public long getMediaPosition() {
        return mediaPosition;
    }

    private void setMediaPosition(long position) {
        mediaPosition = position;
    }

    private class GrabberThread extends Thread {

        private volatile boolean canceled = false;

        @Override
        public void run() {
            log("Media processing thread initialized");
            boolean normalExit = false;
            VideoThread videoThread = new VideoThread();
            AudioThread audioThread = new AudioThread();
            Stats audioStats = new Stats();
            Stats videoStats = new Stats();
            Stats audioBuffer = new Stats();
            Stats videoBuffer = new Stats();
            Stats audioProcessing = new Stats();
            Stats videoProcessing = new Stats();
            try {
                try {
                    ffmpeg = FFmpegFrameGrabber.createDefault(getSourceFile());
                    try {
                        ffmpeg.start();
                        openJavaSound(ffmpeg.getSampleFormat(), ffmpeg.getAudioChannels(), ffmpeg.getSampleRate());
                        log("sound system initialized");
                        videoThread.setFrameRate(ffmpeg.getFrameRate());
                        mediaDuration = ffmpeg.getLengthInTime();
                        boolean started = false;
                        minimumFrames = ffmpeg.getVideoBitrate() < 0 ? 0 : 1;
                        minimumSamples = ffmpeg.getAudioBitrate() < 0 ? 0 : 1;
                        while (!canceled && !interrupted()) {
                            if (paused && !postSeekBuffer) {
                                delay(2);
                            } else {
                                try {
                                    long start = System.nanoTime();
                                    Frame frame = ffmpeg.grabFrame();
                                    long captureTime = System.nanoTime() - start;
                                    if (frame != null) {
                                        audioBuffer.addValue(samples.size());
                                        videoBuffer.addValue(frames.size());
                                        if (frame.samples != null) {
                                            audioStats.addValue(captureTime);
                                            samples.put(new DataFrame(converter.prepareSamplesForPlayback(frame.samples), frame.timestamp));
                                            audioProcessing.addValue(System.nanoTime() - start);
                                        }
                                        if (frame.image != null) {
                                            videoStats.addValue(captureTime);
                                            frames.put(new DataFrame(ImageUtils.copy(frame.image), frame.timestamp));
                                            videoProcessing.addValue(System.nanoTime() - start);
                                        }
                                    } else {
                                        normalExit = true;
                                        break;
                                    }
                                    if (frames.size() >= minimumFrames && samples.size() >= minimumSamples && !started) {
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
                        }
                        if (normalExit) {
                            try {
                                while (!samples.isEmpty() && !frames.isEmpty()) {
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
                audioStats.report("Audio frames");
                videoStats.report("Video frames");
                audioBuffer.report("Audio buffer");
                videoBuffer.report("Video buffer");
                audioProcessing.report("Audio processing");
                videoProcessing.report("Video processing");
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

    private void updatePausedImage() {
        DataFrame<BufferedImage> frame = frames.peek();
        if (frame != null) {
            BufferedImage image = frame.data;
            synchronized (imageLock) {
                currentImage = image;
            }
        }
    }

    private class VideoThread extends Thread {

        volatile boolean canceled = false;
        private double interframe = 1_000_000.0 / 29.97; //target microseconds per frame, updated with setFrameRate
        private final Stats closeMatch = new Stats();
        private final Stats fastVideo = new Stats();
        private final Stats slowVideo = new Stats();

        @Override
        public void run() {
            long lastFrame = System.nanoTime() / 1000;
            try {
                while (!canceled && !interrupted()) {
                    if (paused) {
                        delay(2);
                        lastFrame = System.nanoTime() / 1000;
                    } else {
                        try {
                            DataFrame<BufferedImage> frame = frames.take();
                            BufferedImage image = frame.data;
                            synchronized (imageLock) {
                                currentImage = image;
                            }
                            long now = System.nanoTime() / 1000;
                            long mediaPosition = getMediaPosition();

                            double targetTime = lastFrame + interframe;
                            //if delta > 0, we're ahead of the audio, and need to sleep longer
                            long delta = frame.timestamp - mediaPosition;
                            if (minimumSamples > 0) {
                                if (delta > interframe) {
                                    targetTime = lastFrame + interframe + delta * 1.5;
                                    fastVideo.addValue(delta);
                                } else if (delta < 0) {
                                    slowVideo.addValue(delta);
                                    targetTime = lastFrame + interframe * 0.8;
                                } else {
                                    closeMatch.addValue(delta);
                                    targetTime = lastFrame + interframe;
                                }
                            } else {
                                setMediaPosition(frame.timestamp);
                            }

                            if (now < targetTime) {
                                long delay = (long) (targetTime - now);
                                fpsStats.addValue(delay);
                                TimeUnit.MICROSECONDS.sleep(delay);
                            } else {
                                fpsStats.addValue(0);
                            }
                            lastFrame = now;
                        } catch (InterruptedException ex) {
                            return;
                        }
                    }
                }
            } finally {
                fastVideo.report("Video running fast");
                slowVideo.report("Video running slow");
                closeMatch.report("A/V sync");
            }
        }

        private void setFrameRate(double frameRate) {
            this.interframe = 1_000_000 / frameRate;
        }
    }

    private class AudioThread extends Thread {

        volatile boolean canceled = false;
        private long lastTs = 0;

        @Override
        public void run() {
            while (!canceled && !interrupted()) {
                if (paused) {
                    delay(2);
                } else {
                    try {
                        DataFrame<byte[]> frame = samples.take();
                        byte[] buffer = frame.data;

                        if (mLine != null) {
                            mLine.write(buffer, 0, buffer.length);
                        }
                        setMediaPosition(lastTs);
                        lastTs = frame.timestamp;
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        }
    }

    private static class DataFrame<T> {

        final T data;
        final long timestamp;

        DataFrame(T data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
}
