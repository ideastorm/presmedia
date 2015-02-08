package com.ideastormsoftware.presmedia.sources;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import org.opencv.core.Size;

/**
 * @author Phillip
 */
public class ColorSource implements ImageSource {
    private final BufferedImage image;

    public ColorSource(Size size, Color color) {
        image = new BufferedImage((int)size.width, (int)size.height, BufferedImage.TYPE_3BYTE_BGR);
        setColor(color);
    }
    
    public final void setColor(Color color) {
        Graphics g = image.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
    } 
    
    @Override
    public BufferedImage getCurrentImage() {
        return image;
    }

    @Override
    public void togglePaused() {
    }

    @Override
    public void setPaused(boolean paused) {
    }
    
}
