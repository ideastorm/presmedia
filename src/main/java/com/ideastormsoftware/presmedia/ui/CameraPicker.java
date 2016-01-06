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

package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import com.ideastormsoftware.presmedia.sources.Camera;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.border.LineBorder;

public class CameraPicker extends JDialog {

    private final Set<String> selectedCameras = new HashSet<>();

    private static void log(String message) {
        System.out.println(message);
    }

    public CameraPicker() {
        LayoutInfo windowInfo = calculateSize();
        setTitle("Live Input Selector");
        setSize(windowInfo.size);
        setLocation(windowInfo.topLeft);
        setLayout(null);
        setResizable(false);
        setModal(true);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent we) {
                Insets insets = getInsets();
                layoutPreviews(insets);
            }

        });
    }

    private static Dimension getGridDimensions(int cameraCount) {
        int hc = 1;
        int vc = 1;
        switch (cameraCount) {
            case 0:
                break;
            case 1:
                hc = 1;
                vc = 1;
                break;
            case 2:
                hc = 2;
                vc = 1;
                break;
            case 3:
            case 4:
                hc = 2;
                vc = 2;
                break;
            case 5:
            case 6:
                hc = 3;
                vc = 2;
                break;
            case 7:
            case 8:
            case 9:
                hc = 3;
                vc = 3;
                break;
        }
        return new Dimension(hc, vc);
    }

    private LayoutInfo calculateSize() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screen.getWidth();
        int height = (int) screen.getHeight();
        if (width > 800) {
            width = (int) (0.8 * width);
        }
        if (height > 600);
        height = (int) (0.8 * height);
        int offsetX = (int) ((screen.getWidth() - width) / 2);
        int offsetY = (int) ((screen.getHeight() - height) / 2);
        return new LayoutInfo(new Dimension(width, height), new Point(offsetX, offsetY));
    }

    public Set<Integer> getSelectedCameras() {
        Set<Integer> cameras = new HashSet<>();
        for (String selectedCamera : selectedCameras) {
            cameras.add(Integer.parseInt(selectedCamera));
        }
        return cameras;
    }

    private void layoutPreviews(Insets insets) {
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;
        int cameraCount = Camera.availableCameras();
        log(String.format("got %d cameras", cameraCount));
        int cameraIndex = 0;
        Dimension gridLayout = getGridDimensions(cameraCount);
        int previewWidth = width / gridLayout.width - 10;
        int previewHeight = height / gridLayout.height - 10;
        log("Laying out previews");
        layoutLoop:
        for (int x = 0; x < gridLayout.width; x++) {
            for (int y = 0; y < gridLayout.height; y++) {
                if (cameraIndex >= cameraCount) {
                    break layoutLoop;
                }
                final Camera camera = new Camera(cameraIndex);
                final RenderPane pane = new RenderPane(ImageUtils.scaleSource(camera));
                pane.setName(Integer.toString(cameraIndex++));
                pane.setBorder(new LineBorder(Color.black, 3));
                pane.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        if (selectedCameras.contains(pane.getName())) {
                            selectedCameras.remove(pane.getName());
                            pane.setBorder(new LineBorder(Color.black, 3));
                        } else {
                            selectedCameras.add(pane.getName());
                            pane.setBorder(new LineBorder(Color.red, 3));
                        }
                    }
                });
                add(pane);
                pane.setSize(previewWidth, previewHeight);
                pane.setLocation(5 + (previewWidth + 5) * x, 5 + (previewHeight + 5) * y);
            }
        }
    }

    private static class LayoutInfo {

        Dimension size;
        Point topLeft;

        public LayoutInfo(Dimension size, Point topLeft) {
            this.size = size;
            this.topLeft = topLeft;
        }

    }

}
