package com.ideastormsoftware.presmedia;

import java.awt.EventQueue;
import org.opencv.core.Core;

public class Presenter {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            Camera camera = new Camera(0);
            camera.start();
            Preview preview = new Preview("Camera 0", camera);
            preview.setVisible(true);
        });
    }
}
