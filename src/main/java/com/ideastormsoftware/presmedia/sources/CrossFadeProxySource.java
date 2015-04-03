package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ImageUtils;
import com.ideastormsoftware.presmedia.filters.AbstractFilter;
import com.ideastormsoftware.presmedia.ui.Projector;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class CrossFadeProxySource extends ImageSource {

    private ImageSource delegate;
    private ImageSource fadeIntoDelegate;
    private AbstractFilter overlay;
    private static final double fadeDuration = 0.5;
    private long fadeStartTime;
    private final Projector projector;

    public CrossFadeProxySource(ImageSource delegate, Projector projector) {
        this.delegate = delegate;
        this.projector = projector;
    }

    public void setDelegate(ImageSource delegate) {
        if (this.delegate instanceof Video) {
            ((Video) this.delegate).stop();
        }

        if (delegate instanceof Video) {
            ((Video) delegate).start();
        }
        if (fadeIntoDelegate != null) {
            this.delegate = fadeIntoDelegate;
        }
        fadeIntoDelegate = delegate;
        fadeStartTime = System.nanoTime();
    }

    public void setDelegateNoFade(ImageSource delegate) {
        if (this.delegate instanceof Video) {
            ((Video) this.delegate).stop();
        }
        if (delegate instanceof Video) {
            ((Video) delegate).start();
        }
        this.delegate = delegate;
        fadeIntoDelegate = null;
    }

    public void setOverlay(AbstractFilter overlay) {
        this.overlay = overlay;
    }

    private float findAlpha() {
        long currentStepTime = System.nanoTime();
        double fadeProgress = (currentStepTime - fadeStartTime) / 1_000_000_000.0;
        return (float) (fadeProgress / fadeDuration);
    }

    private void log(String message) {
        System.out.println(message);
    }

    @Override
    public BufferedImage getCurrentImage() {
        BufferedImage baseImage = delegate.getCurrentImage();
        if (fadeIntoDelegate != null) {
            BufferedImage overlayImage = fadeIntoDelegate.getCurrentImage();
            Dimension finalSize = projector.getRenderSize();
            baseImage = ImageUtils.copyAspectScaled(baseImage, finalSize.width, finalSize.height);
            overlayImage = ImageUtils.copyAspectScaled(overlayImage, finalSize.width, finalSize.height);
            Graphics2D g = baseImage.createGraphics();
            float alpha = findAlpha();
            if (alpha >= 1) {
                alpha = 1;
                delegate = fadeIntoDelegate;
                fadeIntoDelegate = null;
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.drawImage(overlayImage, 0, 0, null);
        }
        if (overlay != null)
            return overlay.filter(baseImage);
        return baseImage;
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        return Objects.equals(delegate, source);
    }

    @Override
    public void replaceSource(ImageSource source, ImageSource replacement) {
        if (Objects.equals(delegate, source)) {
            delegate = replacement;
        }
    }

    @Override
    protected String sourceDescription() {
        return String.format("Proxy for %s", delegate.sourceDescription());
    }

}
