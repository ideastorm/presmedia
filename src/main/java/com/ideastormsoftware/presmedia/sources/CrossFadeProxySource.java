/*
 * Copyright 2015 Phillip Hayward <phil@pjhayward.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.filters.AbstractFilter;
import com.ideastormsoftware.presmedia.util.ImageUtils;
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
        if (this.delegate instanceof OnDemandSource) {
            ((OnDemandSource) this.delegate).stop();
        }

        if (delegate instanceof OnDemandSource) {
            ((OnDemandSource) delegate).start();
        }

        if (this.delegate instanceof AbstractFilter) {
            ((AbstractFilter) this.delegate).setSource(delegate);
        } else {
            if (fadeIntoDelegate != null) {
                this.delegate = fadeIntoDelegate;
            }
            setFadeDelegateInternal(delegate);
        }
    }

    public void setDelegateNoFade(ImageSource delegate) {
        if (this.delegate instanceof OnDemandSource) {
            ((OnDemandSource) this.delegate).stop();
        }

        if (delegate instanceof OnDemandSource) {
            ((OnDemandSource) delegate).start();
        }
        if (this.delegate instanceof AbstractFilter) {
            ((AbstractFilter) this.delegate).setSource(delegate);
        } else {
            this.delegate = delegate;
            fadeIntoDelegate = null;
        }
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
        BufferedImage baseImage = ImageUtils.emptyImage(finalSize);
        Graphics2D g = baseImage.createGraphics();
        ImageUtils.drawAspectScaled(g, ImageUtils.scaleSource(delegate).getCurrentImage(finalSize), finalSize);
        if (fadeIntoDelegate != null) {
            BufferedImage overlayImage = ImageUtils.scaleSource(fadeIntoDelegate).getCurrentImage(finalSize);
            float alpha = findAlpha();
            if (alpha >= 1) {
                alpha = 1;
                delegate = fadeIntoDelegate;
                fadeIntoDelegate = null;
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            ImageUtils.drawAspectScaled(g, overlayImage, finalSize);
        }
        return baseImage;
    }
}
