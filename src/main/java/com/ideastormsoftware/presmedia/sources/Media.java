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
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
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

public class Media implements ImageSource, CleanCloseable, Startable, Pauseable, SyncSource {

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
    public void start() {
        openSource();
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

    public void seekTo(long position) throws AvException {
        if (position >= mediaDuration) {
            return;
        }
        boolean pauseState = paused;
        setPaused(true);
        delay(50, TimeUnit.MILLISECONDS); //give time for the pause to take effect
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
            delay(2);
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

    private void openSource() {
        if (grabber == null) {
            log("initializing new processing thread");
            grabber = new GrabberThread();
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
        }
    }

    private String bytePointerToString(BytePointer ptr) {
        byte[] bytes = new byte[ptr.limit()];
        ptr.get(bytes);
        return new String(bytes);
    }

    private void delay(long number, TimeUnit units) {
        try {
            units.sleep(number);
        } catch (InterruptedException ex) {
        }
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

        public GrabberThread() {
            super("FrameGrabThread");
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
                                delay(2);
                            } else {
                                try {
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
                                                delay(2);
                                            }
                                            while (imageBuffer.isEmpty()) {
                                                delay(2);
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
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    currentImage = ImageUtils.emptyImage();
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

    private class VideoThread extends Thread {

        volatile boolean canceled = false;
        private final Stats closeMatch = new Stats();
        private final Stats fastVideo = new Stats();
        private final Stats slowVideo = new Stats();
        private final Stats positionJump = new Stats();

        public VideoThread() {
            super("VideoThread");
        }

        private long microTime() {
            return System.nanoTime() / 1000;
        }

        @Override
        public void run() {
            long lastFrame = microTime();
            Long interframe = (long) (1_000_000 / frameRate);
            long lastMediaPosition = 0;
            try {
                while (!canceled && !interrupted()) {
                    if (paused) {
                        delay(2);
                        lastFrame = microTime();
                    } else {
                        long frameStart = microTime();
                        long targetTime = lastFrame + interframe;
                        DataFrame<BufferedImage> frame = frames.poll();
                        if (frame != null) {
                            frameQueueSize.decrementAndGet();
                            BufferedImage image = frame.data;
                            if (currentImage != null) {
                                imageBuffer.offer(currentImage); //recycle it!
                            }
                            currentImage = image;
                            notifyListeners();
                            if (minimumSamples > 0) {
                                long mediaPosition = getMediaPosition();
                                positionJump.addValue(mediaPosition - lastMediaPosition);
                                lastMediaPosition = mediaPosition;
                                long delta = frame.timestamp - mediaPosition;
                                boolean fast = frame.timestamp > mediaPosition;
                                boolean slow = frame.timestamp + interframe < mediaPosition;
                                if (fast) {
                                    fastVideo.addValue(delta);
                                    targetTime += delta / 2;
                                    interframe+=10;
                                } else if (slow) {
                                    slowVideo.addValue(delta);
                                    targetTime += delta / 4; //delta is negative here
                                    interframe-=5;
                                } else {
                                    closeMatch.addValue(delta);
                                }
                            } else {
                                setMediaPosition(frame.timestamp);
                            }
                            videoPosition = frame.timestamp;
                        }
                        long predelayMicros = microTime();
                        if (predelayMicros < targetTime) {
                            long delay = Math.min(targetTime - predelayMicros, interframe * 2);
                            fpsStats.addValue(delay);
                            TimeUnit.MICROSECONDS.sleep(delay);
                        } else {
                            fpsStats.addValue(0);
                        }
                        lastFrame = frameStart;
                    }
                }
            } catch (InterruptedException e) {
                //just exit
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                fastVideo.report("Video running fast", 0.001);
                slowVideo.report("Video running slow", 0.001);
                closeMatch.report("A/V sync", 0.001);
                fpsStats.report("post-processing delay", 0.001);
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
            while (!canceled && !interrupted()) {
                if (paused) {
                    if (mLine != null) {
                        mLine.stop();
                    }
                    delay(2);
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
                                    while (!interrupted()) {
                                        if (mLine != null) {
                                            setMediaPosition(mLine.getMicrosecondPosition() + offset);
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
