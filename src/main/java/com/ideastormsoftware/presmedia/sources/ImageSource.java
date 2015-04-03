package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ImageUtils;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;

public abstract class ImageSource {

    public abstract BufferedImage getCurrentImage();

    //Override point for filters
    public BufferedImage getCurrentImage(Dimension size) {
        return ImageUtils.copyAspectScaled(getCurrentImage(), size.width, size.height);
    }

    protected void warn(String title, String format, Object... items) {
        JOptionPane.showMessageDialog(null, String.format(format, items), title, JOptionPane.WARNING_MESSAGE);
    }
}
