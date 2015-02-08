package com.ideastormsoftware.presmedia.sources;

import java.awt.image.BufferedImage;

/**
 * @author Phillip
 */
public interface ImageSource {

    public BufferedImage getCurrentImage();

    public void togglePaused();
    public void setPaused(boolean paused);
}
