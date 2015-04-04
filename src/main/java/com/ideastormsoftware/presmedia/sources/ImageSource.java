package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;

public abstract class ImageSource {

    public BufferedImage getCurrentImage() {
        return ImageUtils.emptyImage();
    }

    protected void warn(String title, String format, Object... items) {
        JOptionPane.showMessageDialog(null, String.format(format, items), title, JOptionPane.WARNING_MESSAGE);
    }
}
