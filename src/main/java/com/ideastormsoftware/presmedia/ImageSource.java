package com.ideastormsoftware.presmedia;

import java.awt.image.BufferedImage;

/**
 * @author Phillip
 */
public interface ImageSource {

    public BufferedImage getCurrentImage();

    public void togglePaused();
    public void setPaused(boolean paused);
}
