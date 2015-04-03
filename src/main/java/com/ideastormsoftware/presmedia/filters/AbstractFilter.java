package com.ideastormsoftware.presmedia.filters;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public interface AbstractFilter {

    public BufferedImage filter(BufferedImage original, Dimension targetScreenSize);
}
