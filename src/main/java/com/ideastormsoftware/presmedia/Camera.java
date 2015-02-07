package com.ideastormsoftware.presmedia;

import java.awt.image.BufferedImage;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 *
 * @author Phillip
 */
public class Camera extends Thread implements ImageSource, ResizableSource {

    private final VideoCapture capture;
    private BufferedImage currentImage;
    private double targetFps;
    private volatile boolean paused;

    public Camera(int cameraIndex) {
        super("Camera " + cameraIndex);
        setDaemon(true);
        capture = new VideoCapture(cameraIndex);
        targetFps = 29.97;
        paused = false;
    }

    private void loop() {
        Mat mat = new Mat();

        capture.read(mat);
        BufferedImage image = ImageUtils.convertToImage(mat);

        setCurrentImage(image);
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
            if (!paused)
                loop();
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
    
    @Override
    public void togglePaused() {
        setPaused(!paused);
    }
    
    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    private synchronized void setCurrentImage(BufferedImage image) {
        this.currentImage = image;
    }

    @Override
    public synchronized BufferedImage getCurrentImage() {
        return this.currentImage;
    }

    @Override
    public void setCaptureSize(Size selectedSize) {
        capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, selectedSize.height);
        capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, selectedSize.width);
    }
}
