/*
 * Copyright 2016 Phillip Hayward <phil@pjhayward.net>.
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
package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.sources.ScaledSource;
import com.ideastormsoftware.presmedia.util.ImageUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

public class ImagePainter {

    private static double targetFps = 29.97;

    private static void log(String format, Object... params) {
        System.out.printf(format + "\n", params);
    }
    private LoopingThread paintTimer;
    private LoopingThread prepTimer;
    private int width;
    private int height;
    private Supplier<Double> fpsSource;
    private ScaledSource source;
    private BufferedImage nextImage;
    private final Object imgLock = new Object();

    public static void setFrameRate(double targetFps) {
        ImagePainter.targetFps = targetFps;
    }

    public static double getFrameRate() {
        return ImagePainter.targetFps;
    }

    ImagePainter(Dimension size) {
        this.width = (int) size.getWidth();
        this.height = (int) size.getHeight();
    }

    public double getFps() {
        return paintTimer.getRate();
    }

    public void setup(ScaledSource source, Supplier<Double> fpsSource, Runnable callback) {
        this.fpsSource = fpsSource;
        this.source = source;
        this.paintTimer = new LoopingThread(() -> {
            if (callback != null) {
                callback.run();
            }
        });
        this.prepTimer = new LoopingThread(() -> {
            final Dimension size = new Dimension(width, height);
            BufferedImage image = ImageUtils.emptyImage(size);
            source.scaleInto(image.createGraphics(), size);
            synchronized (imgLock) {
                nextImage = image;
            }
        });
        prepTimer.start();
        paintTimer.start();
    }

    public void paint(Graphics2D g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);
        BufferedImage image = null;
        synchronized (imgLock) {
            image = nextImage;
        }
        g.drawImage(image, 0, 0, null);
        if (fpsSource != null) {
            String fps = String.format("FPS: %01.1f SRC FPS: %01.1f", paintTimer.getRate(), fpsSource.get());
            g.setColor(Color.black);
            g.drawString(fps, 5, height - 16);
            g.setColor(Color.white);
            g.drawString(fps, 6, height - 15);
        }
    }

    void setSize(Dimension size) {
        this.width = (int) size.getWidth();
        this.height = (int) size.getHeight();

    }

    private static class LoopingThread extends Thread {

        private final Runnable task;
        private Queue<Long> startTimes = new ArrayDeque<>();
        private long lastDelay;

        LoopingThread(Runnable task) {
            this.task = task;
        }

        private void delay(long nanos) throws InterruptedException {
            long millis = nanos / 1_000_000;
            int remainingNanos = (int) (nanos % 1_000_000);
            Thread.sleep(millis, remainingNanos);
        }

        public double getRate() {
            if (startTimes.isEmpty()) {
                return 0;
            }
            return startTimes.size() / ((System.nanoTime() - startTimes.peek()) / 1_000_000_000.0);
        }

        public double getLastDelay() {
            return lastDelay / 1_000_000d; //convert to millis
        }

        @Override
        public void run() {
            try {
                while (true) {
                    long runStart = System.nanoTime();
                    startTimes.offer(runStart);
                    while (startTimes.peek() < runStart - 1_000_000_000) {
                        startTimes.poll();
                    }
                    try {
                        task.run();
                    } catch (Exception e) {
                        log("Failed to render: %s", e);
                        e.printStackTrace();
                    }
                    long runEnd = System.nanoTime();
                    long minimumDelayNanos = (long) (1_000_000_000 / targetFps);
                    long delay = minimumDelayNanos - (runEnd - runStart);
                    if (delay > 0) {
                        lastDelay = delay;
                        delay(delay);
                    } else {
                        lastDelay = 0;
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }

}
