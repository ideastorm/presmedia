package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import com.ideastormsoftware.presmedia.sources.ScaledSource;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Insets;
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

    public RenderPane(ScaledSource source) throws HeadlessException {
        setBackground(Color.black);
        setSize(320, 240);
        setDoubleBuffered(false);
        this.timer = new Timer(1000 / 40, (ActionEvent ae) -> {
            BufferedImage image = source.getCurrentImage(new Dimension(getWidth(), getHeight()));

            if (getBorder() != null) {
                int w = getWidth();
                int h = getHeight();
                BufferedImage workImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D grphcs = workImage.createGraphics();
                Border border = getBorder();
                border.paintBorder(this, grphcs, 0, 0, w, h);
                Insets borderInsets = border.getBorderInsets(this);
                w -= borderInsets.left + borderInsets.right;
                h -= borderInsets.top + borderInsets.bottom;
                grphcs.translate(borderInsets.left, borderInsets.top);

                if (w < 1 || h < 1) {
                    return;
                }

                ImageUtils.drawAspectScaled(grphcs, image, w, h);
                image = workImage;
            }
            synchronized (imageLock) {
                nextImage = image;
            }
            repaint(1);
            Toolkit.getDefaultToolkit().sync();
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
