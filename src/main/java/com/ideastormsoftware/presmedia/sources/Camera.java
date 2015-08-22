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

import com.github.sarxos.webcam.Webcam;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;

public class Camera extends ImageSource {
    
    private static void log(String format, Object... params) {
        System.out.printf(format+"\n", params);
    }

    private static final CameraThread[] cameraThreads = new CameraThread[16];

    public static void closeAll() {
        for (CameraThread cameraThread : cameraThreads) {
            if (cameraThread != null) {
                cameraThread.close();
            }
        }
    }

    private int selectedCamera;

    public Camera() {
        this(0);
    }

    public Camera(int cameraIndex) {
        selectCamera(cameraIndex);
    }

    public final void selectCamera(int cameraIndex) {
        selectedCamera = cameraIndex;
        startCamera(cameraIndex);
    }

    private static void startCamera(int cameraIndex) {
        CameraThread camera = cameraThreads[cameraIndex];
        if (camera == null || !camera.isAlive()) {
            camera = new CameraThread(cameraIndex);
            cameraThreads[cameraIndex] = camera;
            camera.start();
        }
    }

    @Override
    public BufferedImage getCurrentImage() {
        if (cameraThreads[selectedCamera] != null) {
            return cameraThreads[selectedCamera].getCurrentImage();
        }
        return null;
    }

    public static int availableCameras() {
        return CameraThread.webcams.size();
    }

    private static class CameraThread extends Thread {

        private static final List<Webcam> webcams = Webcam.getWebcams();

        private final Webcam capture;
        private BufferedImage currentImage;
        private double targetFps;
        private volatile boolean paused;

        public CameraThread(int cameraIndex) {
            super("Camera " + cameraIndex);
            setDaemon(true);

            capture = webcams.get(cameraIndex);
            targetFps = 29.97;
            paused = false;
        }

        boolean isActive() {
            return capture.isOpen();
        }

        private void loop() {
            if (capture.isOpen()) {
                setCurrentImage(capture.getImage());
            }
        }

        private void delay(long nanos) throws InterruptedException {
            long millis = nanos / 1_000_000;
            int nanoDelay = (int) (nanos % 1_000_000);
            Thread.sleep(millis, nanoDelay);
        }

        @Override
        public void run() {
            capture.open();
            try {
                while (!interrupted()) {
                    long targetTime = (long) (1_000_000_000 / targetFps); //nanos/sec / frames/sec = nanos/frame
                    long nanoTime = System.nanoTime();
                    if (!paused) {
                        loop();
                    }
                    long remainder = nanoTime - System.nanoTime() + targetTime;
                    if (remainder > 0) {
                        delay(remainder);
                    } else  {
                        log("slow capture: %01.3f ms over", remainder * 0.000_001);
                    }
                }
            } catch (InterruptedException ex) {
                return;
            } finally {
                capture.close();
            }
        }

        public void close() {
            try {
                while (isAlive()) {
                    interrupt();
                    join(50);
                }
            } catch (InterruptedException ex) {
            }
        }

        public void setTargetFps(double targetFps) {
            this.targetFps = targetFps;
        }

        private synchronized void setCurrentImage(BufferedImage image) {
            this.currentImage = image;
        }

        public synchronized BufferedImage getCurrentImage() {
            return this.currentImage;
        }

        public void setCaptureSize(Dimension selectedSize) {
            capture.setViewSize(selectedSize);
        }
    }
}
