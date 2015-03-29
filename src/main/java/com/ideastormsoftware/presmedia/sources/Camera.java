package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ImageUtils;
import java.awt.image.BufferedImage;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 *
 * @author Phillip
 */
public class Camera extends ImageSource {

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
    
    public Camera(int cameraIndex)
    {
        selectCamera(cameraIndex);
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        return false;
    }

    @Override
    public void replaceSource(ImageSource source, ImageSource replacement) {
    }

    public final void selectCamera(int cameraIndex) {
        selectedCamera = cameraIndex;
        startCamera(cameraIndex);
        fireChanged();
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
        int i = -1;
        do {
            i++;
            startCamera(i);
        } while (cameraThreads[i].isActive());
        cameraThreads[i].close();
        return i;
    }

    private static class CameraThread extends Thread {

        private final VideoCapture capture;
        private BufferedImage currentImage;
        private double targetFps;
        private volatile boolean paused;

        public CameraThread(int cameraIndex) {
            super("Camera " + cameraIndex);
            setDaemon(true);

            capture = new VideoCapture(cameraIndex);
            targetFps = 29.97;
            paused = false;
        }

        boolean isActive() {
            return capture.isOpened();
        }

        private void loop() {
            if (capture.isOpened()) {
                Mat mat = new Mat();

                capture.read(mat);
                BufferedImage image = ImageUtils.convertToImage(mat);

                setCurrentImage(image);
            }
        }

        private void delay(long nanos) {
            long millis = nanos / 1_000_000;
            int nanoDelay = (int) (nanos % 1_000_000);
            try {
                Thread.sleep(millis, nanoDelay);
            } catch (InterruptedException ex) {
            }
        }

        @Override
        public void run() {
            while (!interrupted()) {
                long targetTime = (long) (1_000_000_000 / targetFps); //nanos/sec / frames/sec = nanos/frame
                long nanoTime = System.nanoTime();
                if (!paused) {
                    loop();
                }
                long remainder = nanoTime - System.nanoTime() + targetTime;
                if (remainder > 0) {
                    delay(remainder);
                }
            }
        }

        public void close() {
            interrupt();
            try {
                join();
                capture.release();
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

        public void setCaptureSize(Size selectedSize) {
            capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, selectedSize.height);
            capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, selectedSize.width);
        }
    }

    @Override
    protected String sourceDescription() {
        return String.format("WebCam %d", selectedCamera);
    }
}
