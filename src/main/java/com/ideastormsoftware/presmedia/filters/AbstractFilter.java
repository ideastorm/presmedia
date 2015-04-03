package com.ideastormsoftware.presmedia.filters;

import com.ideastormsoftware.presmedia.ImageUtils;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public abstract class AbstractFilter extends ImageSource {

    private ImageSource source;

    public void setSource(ImageSource source) {
        this.source = source;
    }

    public ImageSource getSource() {
        return source;
    }

    protected abstract BufferedImage filter(BufferedImage original, Dimension targetScreenSize);

    @Override
    public BufferedImage getCurrentImage() {
        return ImageUtils.emptyImage();
    }

    @Override
    public BufferedImage getCurrentImage(Dimension targetScreenSize) {
        return filter(source.getCurrentImage(targetScreenSize), targetScreenSize);
    }

}
