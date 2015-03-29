package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ConfigurationContext;
import com.ideastormsoftware.presmedia.util.ColorUtil;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Phillip
 */
public class ColorSource extends ImageSource {

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
        fireChanged();
    }

    public Color getColor() {
        return color;
    }

    @Override
    public BufferedImage getCurrentImage() {
        return image;
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        return false;
    }

    @Override
    public void replaceSource(ImageSource source, ImageSource replacement) {
    }

    @Override
    protected String sourceDescription() {
        return String.format("Color %s", ColorUtil.colorToHex(color));
    }
}
