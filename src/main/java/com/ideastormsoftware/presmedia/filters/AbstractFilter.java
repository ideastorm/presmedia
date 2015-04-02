package com.ideastormsoftware.presmedia.filters;

import com.ideastormsoftware.presmedia.sources.ImageSource;
import java.awt.image.BufferedImage;

public abstract class AbstractFilter extends ImageSource {
    public abstract BufferedImage filter(BufferedImage original);
}
