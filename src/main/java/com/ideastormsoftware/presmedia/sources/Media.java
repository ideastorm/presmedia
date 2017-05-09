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
import com.ideastormsoftware.presmedia.util.FrameCoordinator;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.interrupted;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final int QUEUE_CAPACITY = 128;
    private final Queue<BufferedImage> imageBuffer = new ConcurrentLinkedQueue<>();
    private final Queue<DataFrame<byte[]>> samples = new ConcurrentLinkedQueue<>();
    private final Queue<DataFrame<BufferedImage>> frames = new ConcurrentLinkedQueue<>();
    private final AtomicInteger sampleQueueSize = new AtomicInteger(0);
    private final AtomicInteger frameQueueSize = new AtomicInteger(0);
    private FFmpegFrameGrabber ffmpeg;

    private final String sourceFile;
    private volatile BufferedImage currentImage = null;
    private final Stats fpsStats = new Stats();

    private static void log(String format, Object... params) {
        System.out.println(String.format("%d %s - %s", System.currentTimeMillis(),
                Thread.currentThread().getName(), String.format(format, params)));
    }
    private volatile long mediaPosition;
    private long mediaDuration;
    private double frameRate;
    private volatile long videoPosition;

    public Media(String sourceFile, Runnable callback) {
        this.sourceFile = sourceFile;
        this.callback = callback;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    @Override
    public void start(Runnable startedCallback) throws InterruptedException {
        openSource(startedCallback);
    }

    @Override
    public void close() {
        closeSource();
    }

    @Override
    public Optional<BufferedImage> get() {
        return Optional.ofNullable(currentImage);
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

    public void seekTo(long position) throws AvException, InterruptedException {
        if (position >= mediaDuration) {
            return;
        }
        boolean pauseState = paused;
        setPaused(true);
        TimeUnit.MILLISECONDS.sleep(50);
        ffmpeg.setTimestamp(position);
        frameQueueSize.set(0);
        if (currentImage != null) {
            imageBuffer.offer(currentImage);
            currentImage = null;
        }
        while (!frames.isEmpty()) {
            imageBuffer.offer(frames.poll().data);
        }
        sampleQueueSize.set(0);
        samples.clear();
        postSeekBuffer = true;
        do {
            TimeUnit.MILLISECONDS.sleep(2);
        } while (frameQueueSize.get() < minimumFrames && sampleQueueSize.get() < minimumSamples);
        updatePausedImage();
        setMediaPosition(position);
        setPaused(pauseState);
        postSeekBuffer = false;
    }

    public long getMediaDuration() {
        return mediaDuration;
    }

    public int getAudioBufferLoad() {
        return Math.min(sampleQueueSize.get() * 100 / QUEUE_CAPACITY, 100);
    }

    public int getVideoBufferLoad() {
        return Math.min(frameQueueSize.get() * 100 / QUEUE_CAPACITY, 100);
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

    private GrabberThread grabber = null;

    private void openSource(Runnable startedCallback) {
        if (grabber == null) {
            log("initializing new processing thread");
            grabber = new GrabberThread(startedCallback);
            grabber.start();
            log("processing thread %s started", grabber.getName());

        }
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
            currentImage = null;
        }
    }

    private String bytePointerToString(BytePointer ptr) {
        byte[] bytes = new byte[ptr.limit()];
        ptr.get(bytes);
        return new String(bytes);
    }

    @Override
    public double getFps() {
        return frameRate;
    }

    public long getVideoPosition() {
        return videoPosition;
    }

    public long getMediaPosition() {
        return mediaPosition;
    }

    private void setMediaPosition(long position) {
        mediaPosition = position;
    }

    private class GrabberThread extends Thread {

        private final Runnable startedCallback;

        public GrabberThread(Runnable startedCallback) {
            super("FrameGrabThread");
            this.startedCallback = startedCallback;
        }

        private volatile boolean canceled = false;

        @Override
        public void run() {
            log("Media processing thread initialized");
            boolean normalExit = false;
            VideoThread videoThread = new VideoThread();
            AudioThread audioThread = new AudioThread();
            try {
                try {
                    ffmpeg = FFmpegFrameGrabber.createDefault(getSourceFile());
                    try {
                        ffmpeg.start();
                        mediaDuration = ffmpeg.getLengthInTime();
                        boolean started = false;
                        minimumFrames = ffmpeg.getVideoBitrate() < 0 ? 0 : 5;
                        minimumSamples = ffmpeg.getAudioBitrate() < 0 ? 0 : 5;
                        if (minimumFrames > 0) {
                            Media.this.frameRate = ffmpeg.getFrameRate();
                        }
                        if (minimumSamples > 0) {
                            openJavaSound(ffmpeg.getSampleFormat(), ffmpeg.getAudioChannels(), ffmpeg.getSampleRate());
                            log("sound system initialized");
                        }
                        while (!canceled && !interrupted()) {
                            if (paused && !postSeekBuffer) {
                                TimeUnit.MILLISECONDS.sleep(2);
                            } else {
                                long start = System.nanoTime();
                                Frame frame = ffmpeg.grabFrame();
                                if (frame != null) {
                                    if (frame.samples != null) {
                                        sampleQueueSize.incrementAndGet();
                                        samples.offer(new DataFrame(converter.prepareSamplesForPlayback(frame.samples), frame.timestamp, frame.duration));
                                    }
                                    if (frame.image != null) {
                                        if (!started && imageBuffer.isEmpty()) {
                                            for (int i = 0; i < QUEUE_CAPACITY; i++) {
                                                imageBuffer.offer(new BufferedImage(frame.image.getWidth(), frame.image.getHeight(), BufferedImage.TYPE_3BYTE_BGR));
                                            }
                                        }
                                        while (frameQueueSize.get() > QUEUE_CAPACITY * 0.9) //try to make sure we're not overwriting an image before it's rendered
                                        {
                                            TimeUnit.MILLISECONDS.sleep(2);
                                        }
                                        while (imageBuffer.isEmpty()) {
                                            TimeUnit.MILLISECONDS.sleep(2);
                                        }
                                        BufferedImage image = imageBuffer.poll();
                                        image.createGraphics().drawImage(frame.image, 0, 0, null);
                                        frameQueueSize.incrementAndGet();
                                        frames.offer(new DataFrame(image, frame.timestamp, frame.duration));
                                    }
                                } else {
                                    normalExit = true;
                                    break;
                                }
                                if (frames.size() >= minimumFrames && samples.size() >= minimumSamples && !started) {
                                    audioThread.start();
                                    videoThread.start();
                                    started = true;
                                    if (startedCallback != null) {
                                        startedCallback.run();
                                    }
                                }
                            }
                        }
                        if (normalExit) {
                            try {
                                while (!samples.isEmpty() && !frames.isEmpty()) {
                                    minimumFrames = frames.isEmpty() ? 0 : 1;
                                    minimumSamples = samples.isEmpty() ? 0 : 1;
                                    Thread.sleep(5);
                                }
                            } catch (InterruptedException e) {
                                log("got interrupted waiting for buffers to flush");
                                return;
                            }
                        }
                    } catch (InterruptedException e) {
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
                log("exited thread lock - ready to go with the next thread");
                frames.clear();
                samples.clear();
                imageBuffer.clear();
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
        if (minimumFrames > 0) {
            DataFrame<BufferedImage> frame = frames.peek();
            if (frame != null) {
                currentImage = frame.data;
            }
        }
    }

    private long microTime() {
        return System.nanoTime() / 1000;
    }

    private class VideoThread extends Thread {

        volatile boolean canceled = false;
        private final Stats closeMatch = new Stats();
        private final Stats fastVideo = new Stats();
        private final Stats slowVideo = new Stats();
        private final Stats positionJump = new Stats();

        public VideoThread() {
            super("VideoThread");
        }

        @Override
        public void run() {
            Long interframe = (long) (1_000_000 / frameRate);
            long delayStep = (long) (interframe / frameRate);
            long lastMediaPosition = 0;
            try {
                while (!canceled && !interrupted()) {
                    if (paused) {
                        TimeUnit.MILLISECONDS.sleep(2);
                    } else {
                        long frameStart = microTime();
                        DataFrame<BufferedImage> frame = frames.poll();
                        if (frame != null) {
                            frameQueueSize.decrementAndGet();
                            BufferedImage image = frame.data;
                            if (currentImage != null) {
                                imageBuffer.offer(currentImage); //recycle it!
                            }
                            currentImage = image;
                            FrameCoordinator.notify(Media.this);
                            if (minimumSamples > 0) {
                                long mediaPosition = getMediaPosition();
                                positionJump.addValue(mediaPosition - lastMediaPosition);
                                lastMediaPosition = mediaPosition;
                                long delay = 0;
                                while (frame.timestamp > getMediaPosition() + interframe && delay < interframe) {
                                    delay += delayStep;
                                    TimeUnit.MICROSECONDS.sleep(delayStep);
                                }
                                fpsStats.addValue(delay);
                            } else {
                                setMediaPosition(frame.timestamp);
                                long delay = interframe - (microTime() - frameStart);
                                fpsStats.addValue(delay);
                                TimeUnit.MICROSECONDS.sleep(delay);
                            }
                            videoPosition = frame.timestamp;
                        }
                    }
                }
            } catch (InterruptedException e) {
                //just exit
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                fpsStats.report("interframe delay", 0.001);
                positionJump.report("Media position deltas", 0.001);
            }
        }
    }

    private class AudioThread extends Thread {

        volatile boolean canceled = false;

        public AudioThread() {
            super("AudioThread");
        }

        @Override
        public void run() {
            Thread updateThread = null;
            try {
                while (!canceled && !interrupted()) {
                    if (paused) {
                        if (mLine != null) {
                            mLine.stop();
                        }
                        sleep(2);
                        if (updateThread != null) {
                            updateThread.interrupt();
                            updateThread = null;
                        }
                    } else {
                        if (mLine != null && !mLine.isRunning()) {
                            mLine.start();
                        }
                        DataFrame<byte[]> frame = samples.poll();
                        if (frame != null) {
                            if (updateThread == null) {
                                final long offset = frame.timestamp - mLine.getMicrosecondPosition();
                                updateThread = new Thread(() -> {
                                    try {
                                        long lastUpdate = microTime();
                                        long lastPosition = 0;
                                        while (!interrupted()) {
                                            if (mLine != null) {
                                                long readPosition = mLine.getMicrosecondPosition();
                                                if (readPosition != lastPosition) {
                                                    lastPosition = readPosition;
                                                    lastUpdate = microTime();
                                                }
                                                setMediaPosition(lastPosition + offset + microTime() - lastUpdate);
                                            }
                                            sleep(1);
                                        }
                                    } catch (InterruptedException e) {
                                    }
                                });
                                updateThread.start();
                            }
                            sampleQueueSize.decrementAndGet();
                            byte[] buffer = frame.data;

                            if (mLine != null) {
                                mLine.write(buffer, 0, buffer.length);
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
            } finally {
                if (updateThread != null)
                    updateThread.interrupt();
            }
        }
    }

    private static class DataFrame<T> {

        final T data;
        final long timestamp;
        final long duration;

        DataFrame(T data, long timestamp, long duration) {
            this.data = data;
            this.timestamp = timestamp;
            this.duration = duration;
        }
    }
}
