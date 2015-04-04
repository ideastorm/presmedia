package com.ideastormsoftware.presmedia.filters;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import com.ideastormsoftware.presmedia.sources.ScaledSource;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public abstract class AbstractFilter extends ImageSource implements ScaledSource {

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
        return filter(ImageUtils.scaleSource(source).getCurrentImage(targetScreenSize), targetScreenSize);
    }

}
