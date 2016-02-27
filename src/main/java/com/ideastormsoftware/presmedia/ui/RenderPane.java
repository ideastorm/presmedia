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

import com.ideastormsoftware.presmedia.sources.ImageSource;
import com.ideastormsoftware.presmedia.sources.ScaledSource;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class RenderPane extends JPanel {

    private final ImagePainter painter;
    
    public RenderPane(Supplier<BufferedImage> imgSupplier, Supplier<Double> fpsSupplier)
    {
        this(new ScaledSource().setSource(imgSupplier), fpsSupplier);
    }
    
    public RenderPane(ImageSource source) {
        this(new ScaledSource().setSource(source), source::getFps);
    }
    
    public RenderPane(ScaledSource source, Supplier<Double> fpsSrc) throws HeadlessException {
        setBackground(Color.black);
        setSize(320, 240);
        setDoubleBuffered(true);
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentHidden(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
                painter.setSize(getSize());
            }
        });

        painter = new ImagePainter(getSize());
        painter.setup(source, fpsSrc, () -> {
            repaint(1);
        });
    }

    @Override
    public void paint(Graphics grphcs) {
        painter.paint((Graphics2D) grphcs);
        Border border = getBorder();
        if (border != null) {
            int w = getWidth();
            int h = getHeight();
            border.paintBorder(this, grphcs, 0, 0, w, h);
        }
    }
}
