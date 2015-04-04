package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import com.ideastormsoftware.presmedia.filters.AbstractFilter;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class CrossFadeProxySource extends ImageSource implements ScaledSource {

    private ImageSource delegate;
    private ImageSource fadeIntoDelegate;
    private static final double fadeDuration = 0.5;
    private long fadeStartTime;

    public CrossFadeProxySource(ImageSource delegate) {
        this.delegate = delegate;
    }

    public void setDelegate(ImageSource delegate) {
        if (this.delegate instanceof Media) {
            ((Media) this.delegate).stop();
        }

        if (delegate instanceof Media) {
            ((Media) delegate).start();
        }
        if (fadeIntoDelegate != null) {
            this.delegate = fadeIntoDelegate;
        }
        setFadeDelegateInternal(delegate);
    }

    public void setDelegateNoFade(ImageSource delegate) {
        if (this.delegate instanceof Media) {
            ((Media) this.delegate).stop();
        }
        if (delegate instanceof Media) {
            ((Media) delegate).start();
        }
        this.delegate = delegate;
        fadeIntoDelegate = null;
    }

    private void setFadeDelegateInternal(ImageSource source) {
        fadeIntoDelegate = source;
        fadeStartTime = System.nanoTime();
    }

    public void setOverlay(AbstractFilter overlay) {
        ImageSource baseSource = delegate;
        if (baseSource instanceof AbstractFilter) {
            baseSource = ((AbstractFilter) baseSource).getSource();
        }
        if (overlay == null) {
            setFadeDelegateInternal(baseSource);
        } else {
            overlay.setSource(baseSource);
            setFadeDelegateInternal(overlay);
        }
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
    public BufferedImage getCurrentImage(Dimension finalSize) {
        BufferedImage baseImage = ImageUtils.scaleSource(delegate).getCurrentImage(finalSize);
        if (fadeIntoDelegate != null) {
            BufferedImage overlayImage = ImageUtils.scaleSource(fadeIntoDelegate).getCurrentImage(finalSize);
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
        return baseImage;
    }
}
