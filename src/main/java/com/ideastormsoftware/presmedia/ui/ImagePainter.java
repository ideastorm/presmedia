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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class ImagePainter {

    private final Object imageLock = new Object();
    private BufferedImage nextImage;

    private static void log(String format, Object... params) {
        System.out.printf(format + "\n", params);
    }
    private LoopingThread timer;
    private int width;
    private int height;

    ImagePainter(Dimension size) {
        this.width = (int) size.getWidth();
        this.height = (int) size.getHeight();
    }

    public void setup(ScaledSource source, Runnable callback) {
        this.timer = new LoopingThread(1000 / 40, () -> {
            synchronized (imageLock) {
                nextImage = source.get();
            }
            if (callback != null) {
                callback.run();
            }
        });
        timer.start();
    }

    public void paint(Graphics g) {
        BufferedImage img;
        synchronized (imageLock) {
            img = nextImage;
        }

        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);
        if (img != null) {
            Point offset = new Point((width - img.getWidth()) / 2, (height - img.getHeight()) / 2);
            g.drawImage(img, offset.x, offset.y, null);
        }
    }

    void setSize(Dimension size) {
        this.width = (int) size.getWidth();
        this.height = (int) size.getHeight();
    }

    private static class LoopingThread extends Thread {

        private final long minimumDelayMillis;
        private final Runnable task;

        LoopingThread(long minDelayMs, Runnable task) {
            this.minimumDelayMillis = minDelayMs;
            this.task = task;
        }

        private void delay(long nanos) throws InterruptedException {
            long millis = nanos / 1_000_000;
            int remainingNanos = (int) (nanos % 1_000_000);
            Thread.sleep(millis, remainingNanos);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    long runStart = System.nanoTime();
                    try {
                        task.run();
                    } catch (Exception e) {
                        log("Failed to render: %s", e);
                        e.printStackTrace();
                    }
                    long runEnd = System.nanoTime();
                    long delay = minimumDelayMillis * 1_000_000 - (runEnd - runStart);
                    if (delay > 0) {
                        delay(delay);
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }

}
