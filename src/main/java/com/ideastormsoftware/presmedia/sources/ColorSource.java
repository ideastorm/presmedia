package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ImageUtils;
import com.ideastormsoftware.presmedia.util.ColorUtil;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * @author Phillip
 */
public class ColorSource extends ImageSource {

    private final BufferedImage image;
    private Color color;

    public ColorSource() {
        this(Color.black);
    }

    public ColorSource(Color color) {
        image = ImageUtils.emptyImage();
        setColor(color);
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
}
