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
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import javax.swing.JFrame;

public class Projector extends JFrame {

    RenderPane renderPane;

    public Projector(ScaledSource source) throws HeadlessException {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        renderPane = new RenderPane(source);
        add(renderPane);
        setTitle("Presmedia Projector");
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            moveToSecondaryScreen();
        }
        super.setVisible(visible);
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
        } else {
            setLocation(0, 0);
            setSize(640, 480);
        }
        setUndecorated(multiMonitor);
        setAlwaysOnTop(multiMonitor);
    }
    
    public Dimension getRenderSize()
    {
        return renderPane.getSize();
    }           

}
