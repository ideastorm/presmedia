package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ConfigurationContext;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * @author Phillip
 */
public class ColorSource implements ImageSource {

    private final BufferedImage image;
    private Color color;

    public ColorSource() {
        image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        setColor(Color.black);
    }

    public final void setColor(Color color) {
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.Src); //ignore current contents, even if the incoming color has an alpha component
        g.setColor(color);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        this.color = color;
    }

    public Color getColor() {
        return color;
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
