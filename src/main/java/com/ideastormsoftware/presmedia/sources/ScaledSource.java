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
package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 * @author Phillip Hayward <phil@pjhayward.net>
 */
public class ScaledSource implements Supplier<BufferedImage>, Runnable {

    public Supplier<BufferedImage> source;
    protected Dimension targetSize = new Dimension(1280, 720);
    private BufferedImage scaledImage = ImageUtils.emptyImage();
    private static final long RUN_DELAY = 1000 / 30;
    private boolean active = true;
    private boolean shutdown = false;
    private long lastGet = System.currentTimeMillis();
    private final Object syncPoint = new Object();
    private Thread workerThread = null;
    

    public ScaledSource() {

    }

    public <T extends ScaledSource> T setSource(Supplier<BufferedImage> source) {
        this.source = source;
        return (T) this;
    }

    @Override
    public BufferedImage get() {
        synchronized (syncPoint) {
            lastGet = System.currentTimeMillis();
            if (workerThread == null) {
                workerThread = new Thread(this, getClass().getSimpleName());
                workerThread.start();
            }
        }
        return scaledImage;
    }

    public void setTargetSize(Dimension size) {
        this.targetSize = size;
    }

    private void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            shutdown = true;
        }
    }

    private void log(String format, Object... params) {
        System.out.printf(format + "\n", params);
    }

    @Override
    public void run() {
        boolean shouldExit = false;
        while (!shutdown && !shouldExit) {
            synchronized (syncPoint) {
                shouldExit = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastGet) > 5;
            }
            long start = System.currentTimeMillis();
            if (active) {
                BufferedImage image = source.get();
                setScaledImage(ImageUtils.copyAspectScaled(image, targetSize));
            }
            long delay = RUN_DELAY - (System.currentTimeMillis() - start);
            if (delay > 0) {
                delay(delay);
            }
        }
        synchronized (syncPoint) {
            workerThread = null;
        }
    }

    public void setActive(boolean b) {
        this.active = b;
    }

    protected void setScaledImage(BufferedImage img) {
        scaledImage = img;
    }
}
