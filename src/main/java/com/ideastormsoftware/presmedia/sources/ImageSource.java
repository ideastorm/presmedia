package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ConfigurationContext;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * @author Phillip
 */
public interface ImageSource {

    public BufferedImage getCurrentImage();

    public JPanel getConfigurationPanel(ConfigurationContext context);

    public boolean dependsOn(ImageSource source);

    public void replaceSource(ImageSource source, ImageSource replacement);
}
