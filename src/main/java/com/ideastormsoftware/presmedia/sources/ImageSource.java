package com.ideastormsoftware.presmedia.sources;

import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;

public abstract class ImageSource {

    public abstract BufferedImage getCurrentImage();

    protected void warn(String title, String format, Object... items) {
        JOptionPane.showMessageDialog(null, String.format(format, items), title, JOptionPane.WARNING_MESSAGE);
    }
}
