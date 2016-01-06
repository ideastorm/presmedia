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
import com.ideastormsoftware.presmedia.util.ImageUtils;
import com.ideastormsoftware.presmedia.sources.ScaledSource;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;

public class RenderPane extends JPanel {

    private final Timer timer;
    private final Object imageLock = new Object();
    private BufferedImage nextImage;

    private static void log(String format, Object... params) {
        System.out.printf(format + "\n", params);
    }

    public RenderPane(ScaledSource source) throws HeadlessException {
        setBackground(Color.black);
        setSize(320, 240);
        setDoubleBuffered(false);
        this.timer = new Timer(1000 / 40, (ActionEvent ae) -> {
            long runStart = System.nanoTime();
            BufferedImage image = source.getScaled(new Dimension(getWidth(), getHeight()));
            long getScaledImageTime = System.nanoTime();

            Border border = getBorder();
            if (border != null) {
                int w = getWidth();
                int h = getHeight();
//                BufferedImage workImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
//                Graphics2D grphcs = workImage.createGraphics();
                Graphics2D grphcs = image.createGraphics();
                border.paintBorder(this, grphcs, 0, 0, w, h);
//                Insets borderInsets = border.getBorderInsets(this);
//                w -= borderInsets.left + borderInsets.right;
//                h -= borderInsets.top + borderInsets.bottom;
//                grphcs.translate(borderInsets.left, borderInsets.top);
//
//                if (w < 1 || h < 1) {
//                    return;
//                }
//
//                ImageUtils.drawAspectScaled(grphcs, image, w, h);
//                image = workImage;
            }
            long prepBorderTime = System.nanoTime();
            synchronized (imageLock) {
                nextImage = image;
            }
            long setImageTime = System.nanoTime();
            repaint(1);
            Toolkit.getDefaultToolkit().sync();
            long syncTime = System.nanoTime();
            if (syncTime - runStart > 30_000_000) {
                log("image: %01.2f, border: %01.2f, setImage: %01.2f, sync: %01.2f, total: %01.2f",
                        (getScaledImageTime - runStart) / 1_000_000f,
                        (prepBorderTime - getScaledImageTime) / 1_000_000f,
                        (setImageTime - prepBorderTime) / 1_000_000f,
                        (syncTime - setImageTime) / 1_000_000f,
                        (syncTime - runStart) / 1_000_000f);
            }
        });
        timer.start();
    }

    @Override
    public void paint(Graphics grphcs) {
        BufferedImage img;
        synchronized (imageLock) {
            img = nextImage;
        }

        if (img != null) {
            grphcs.drawImage(img, 0, 0, this);
        }
    }
}
