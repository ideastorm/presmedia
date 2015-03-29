package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.ImageUtils;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.opencv.core.Size;

public class RenderPane extends JPanel{
        private ImageSource source;
    private final Timer timer;

    public RenderPane(ImageSource source) throws HeadlessException {
        setBackground(Color.black);
        setSize(320, 240);
        this.source = source;
        this.timer = new Timer(30, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                repaint(1);
            }
        });
        timer.start();
    }

    public void setSource(ImageSource source) {
        this.source = source;
    }

    @Override
    public void paint(Graphics grphcs) {
        int w = getWidth();
        int h = getHeight();
        if (w < 1 || h < 1) {
            return;
        }
        grphcs.setColor(Color.black);
        grphcs.fillRect(0, 0, w, h);
        BufferedImage img = source.getCurrentImage();
        if (img != null) {
            Size targetSize = ImageUtils.aspectScaledSize(img.getWidth(), img.getHeight(), w, h);
            BufferedImage copy = ImageUtils.copyScaled(img, (int)targetSize.width, (int)targetSize.height);
            Graphics g2 = copy.getGraphics();
            g2.setColor(new Color(0,0,0,127));
            g2.fillRect(0,0,120,13);
            g2.setColor(Color.white);
            g2.setFont(g2.getFont().deriveFont(10f));
            g2.drawString(String.format("Source: %d x %d", img.getWidth(), img.getHeight()), 10, 10);

            int offsetWidth = (int) (w - targetSize.width) / 2;
            int offsetHeight = (int) (h - targetSize.height) / 2;
            grphcs.drawImage(copy, offsetWidth, offsetHeight, (int) targetSize.width, (int) targetSize.height, this);
        }
    }

}
