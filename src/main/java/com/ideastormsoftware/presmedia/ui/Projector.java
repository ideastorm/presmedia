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

import com.ideastormsoftware.presmedia.sources.ScaledSource;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferStrategy;
import java.util.Optional;
import java.util.function.Consumer;

public class Projector extends Window {

    private final ImagePainter painter;
    private Consumer<Double> frameCallback;

    public Projector(ScaledSource source) throws HeadlessException {
        super(null);
        this.painter = new ImagePainter(this.getSize());
        painter.setup(source, null, Optional.empty(), () -> {
            Toolkit.getDefaultToolkit().sync();
            BufferStrategy strategy = getBufferStrategy();
            if (strategy != null) {
                do {
                    do {
                        Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
                        painter.paint(graphics);
                        graphics.dispose();
                    } while (strategy.contentsRestored());
                    strategy.show();
                } while (strategy.contentsLost());
            }
            frameCallback.accept(painter.getFps());
        });
    }
    
    public void setFrameCallback(Consumer<Double> callback)
    {
        this.frameCallback = callback;
    }
    
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            moveToSecondaryScreen();
        }
        super.setVisible(visible);
        if (visible) {
            createBufferStrategy(2);
            BufferStrategy strategy = getBufferStrategy();
            System.out.println("sun.java2d.opengl: " + System.getProperty("sun.java2d.opengl"));
            System.out.println("Page flipping: " + strategy.getCapabilities().isPageFlipping());
            System.out.println("Multiple Buffers: " + strategy.getCapabilities().isMultiBufferAvailable());
            System.out.println("FSEM Required: " + strategy.getCapabilities().isFullScreenRequired());
            System.out.println("Backbuffer accelerated: " + strategy.getCapabilities().getBackBufferCapabilities().isAccelerated());
            System.out.println("Frontbuffer accelerated: " + strategy.getCapabilities().getFrontBufferCapabilities().isAccelerated());
        }
    }

    private void moveToSecondaryScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        boolean multiMonitor = gd.length > 1;
        if (multiMonitor) {
            DisplayMode currentMode = gd[1].getDisplayMode();
            Dimension primaryMonitorSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(primaryMonitorSize.width, 0);
            setSize(currentMode.getWidth(), currentMode.getHeight());
            ImagePainter.setFrameRate(currentMode.getRefreshRate());
        } else {
            setLocation(0, 0);
            DisplayMode currentMode = gd[0].getDisplayMode();
            setSize(currentMode.getWidth(), currentMode.getHeight());
            this.toBack();
            ImagePainter.setFrameRate(currentMode.getRefreshRate());
        }
        painter.setSize(getSize());
        setAlwaysOnTop(multiMonitor);
    }

    public Dimension getRenderSize() {
        return getSize();
    }

}
