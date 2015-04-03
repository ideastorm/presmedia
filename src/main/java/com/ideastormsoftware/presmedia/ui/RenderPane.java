package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.ImageUtils;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import org.opencv.core.Size;

public class RenderPane extends JPanel {

    private ImageSource source;
    private final Timer timer;
    private boolean showOverlay = true;
    private List<Long> lastTenFrames = new ArrayList<>(10);

    public RenderPane(ImageSource source) throws HeadlessException {
        setBackground(Color.black);
        setSize(320, 240);
        setDoubleBuffered(false);
        this.source = source;
        this.timer = new Timer(1000 / 30, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                repaint(1);
                Toolkit.getDefaultToolkit().sync();
            }
        });
        timer.start();
    }

    public void setShowOverlay(boolean showOverlay) {
        this.showOverlay = showOverlay;
    }

    public void setSource(ImageSource source) {
        this.source = source;
    }

    @Override
    public void paint(Graphics grphcs) {
        lastTenFrames.add(System.nanoTime());
        if (lastTenFrames.size() > 10) {
            lastTenFrames.remove(0);
        }
        int w = getWidth();
        int h = getHeight();
        if (getBorder() != null) {
            Border border = getBorder();
            border.paintBorder(this, grphcs, 0, 0, w, h);
            Insets borderInsets = border.getBorderInsets(this);
            w -= borderInsets.left + borderInsets.right;
            h -= borderInsets.top + borderInsets.bottom;
            grphcs.translate(borderInsets.left, borderInsets.top);
        }
        if (w < 1 || h < 1) {
            return;
        }
        BufferedImage img = source.getCurrentImage();
        if (img != null) {
            Size targetSize = ImageUtils.aspectScaledSize(img.getWidth(), img.getHeight(), w, h);
            BufferedImage copy = ImageUtils.copyScaled(img, (int) targetSize.width, (int) targetSize.height);
            if (showOverlay) {
                Graphics g2 = copy.getGraphics();
                g2.setColor(new Color(0, 0, 0, 127));
                g2.fillRect(0, 0, 120, 26);
                g2.setColor(Color.white);
                g2.setFont(g2.getFont().deriveFont(10f));
                g2.drawString(String.format("Source: %d x %d", img.getWidth(), img.getHeight()), 10, 10);
                long delta = System.nanoTime() - lastTenFrames.get(0);
                double fps = lastTenFrames.size() * 1_000_000_000.0 / delta;
                g2.drawString(String.format("FPS: %01.2f", fps), 10, 23);
            }
            int offsetWidth = (int) (w - targetSize.width) / 2;
            int offsetHeight = (int) (h - targetSize.height) / 2;
            VolatileImage fastImage = createVolatileImage(w, h);
            final Graphics2D volatileGraphics = fastImage.createGraphics();
            volatileGraphics.setColor(Color.black);
            volatileGraphics.fillRect(0, 0, w, h);
            volatileGraphics.drawImage(copy, offsetWidth, offsetHeight, (int) targetSize.width, (int) targetSize.height, this);
            grphcs.drawImage(fastImage, 0, 0, this);
        }
    }

}
