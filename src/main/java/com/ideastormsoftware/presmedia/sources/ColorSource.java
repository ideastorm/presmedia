package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ConfigurationContext;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
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
    public JPanel getConfigurationPanel(ConfigurationContext context) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        return false;
    }

    @Override
    public void replaceSource(ImageSource source, ImageSource replacement) {
    }
    
}
