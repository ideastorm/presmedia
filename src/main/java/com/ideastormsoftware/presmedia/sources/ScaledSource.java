package com.ideastormsoftware.presmedia.sources;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public interface ScaledSource {

    public BufferedImage getCurrentImage(Dimension size);
}
