package com.ideastormsoftware.presmedia;

import com.ideastormsoftware.presmedia.sources.ImageSource;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.Timer;
import org.opencv.core.Size;

/**
 *
 * @author Phillip
 */
class Preview extends JFrame {

    private ImageSource source;
    private final Timer timer;
    private Insets insets;

    public Preview(String title, ImageSource source) throws HeadlessException {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(title);
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

    @Override
    public void setVisible(boolean bln) {
        super.setVisible(bln); //To change body of generated methods, choose Tools | Templates.
        if (bln) {
            this.insets = getInsets();
        }
    }

    int getClientWidth() {
        return getWidth() - insets.left - insets.right;
    }

    int getClientHeight() {
        return getHeight() - insets.top - insets.bottom;
    }
    
    public void setSource(ImageSource source) {
        this.source = source;
    }

    @Override
    public void paint(Graphics grphcs) {
        int w = getClientWidth();
        int h = getClientHeight();
        if (w < 1 || h < 1) {
            return;
        }
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
            grphcs.drawImage(copy, insets.left + offsetWidth, insets.top + offsetHeight, (int) targetSize.width, (int) targetSize.height, this);
        }
    }
}
