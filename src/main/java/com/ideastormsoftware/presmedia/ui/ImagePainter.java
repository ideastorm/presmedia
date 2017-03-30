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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.imgscalr.Scalr;

public class ImagePainter {

    private static int targetFps = 30; //set to screen refresh rate /2

    private static void log(String format, Object... params) {
        System.out.printf(format + "\n", params);
    }
    private LoopingThread paintTimer;
    private int width;
    private int height;
    private Supplier<Double> fpsSource;

    public static void setFrameRate(int targetFps) {
        ImagePainter.targetFps = targetFps;
    }

    private ScaledSource source;
    private Optional<Scalr.Method> quality;

    ImagePainter(Dimension size) {
        this.width = (int) size.getWidth();
        this.height = (int) size.getHeight();
    }

    public double getFps() {
        return paintTimer.getRate();
    }

    public void setup(ScaledSource source, Supplier<Double> fpsSource, Optional<Scalr.Method> quality, Runnable callback) {
        this.fpsSource = fpsSource;
        this.paintTimer = new LoopingThread(() -> {
            if (callback != null) {
                callback.run();
            }
        });
        this.source = source;
        this.quality = quality;
        paintTimer.start();
    }

    public void paint(Graphics2D g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);
        final Dimension size = new Dimension(width, height);
        source.scaleInto(g, size, quality);
        if (fpsSource != null) {
            String fps = String.format("FPS: %01.1f SFPS: %01.1f", paintTimer.getRate(), fpsSource.get());
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

        LoopingThread(Runnable task) {
            this.task = task;
        }

        private void delay(long nanos) throws InterruptedException {
            TimeUnit.NANOSECONDS.sleep(nanos);
        }

        public double getRate() {
            if (startTimes.isEmpty()) {
                return 0;
            }
            return startTimes.size() / ((System.nanoTime() - startTimes.peek()) / 1_000_000_000.0);
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
                        delay(delay);
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }

}
