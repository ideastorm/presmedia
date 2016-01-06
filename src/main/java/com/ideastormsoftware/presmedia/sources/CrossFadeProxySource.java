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

import com.ideastormsoftware.presmedia.util.ImageUtils;
import com.ideastormsoftware.presmedia.filters.ImageFilter;
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
        delegate.activate();

        if (this.delegate instanceof ImageFilter) {
            ((ImageFilter) this.delegate).setSource(delegate);
        } else {
            if (fadeIntoDelegate != null) {
                this.delegate = fadeIntoDelegate;
            }
            setFadeDelegateInternal(delegate);
        }
    }

    public void setDelegateNoFade(ImageSource delegate) {
        this.delegate.deactivate();
        delegate.activate();
        if (this.delegate instanceof ImageFilter) {
            ((ImageFilter) this.delegate).setSource(delegate);
        } else {
            this.delegate = delegate;
            fadeIntoDelegate = null;
        }
    }

    private void setFadeDelegateInternal(ImageSource source) {
        fadeIntoDelegate = source;
        fadeStartTime = System.nanoTime();
    }

    public void setOverlay(ImageFilter overlay) {
        ImageSource baseSource = delegate;
        if (baseSource instanceof ImageFilter) {
            baseSource = ((ImageFilter) baseSource).getSource();
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
    public BufferedImage getScaled(Dimension finalSize) {
        long start = System.currentTimeMillis();
        BufferedImage baseImage = ImageUtils.emptyImage(finalSize);
        Graphics2D g = baseImage.createGraphics();
        long prepBaseTime = System.currentTimeMillis();
        ImageUtils.drawAspectScaled(g, ImageUtils.copyAspectScaled(delegate.get(), finalSize), finalSize);
        long scaleDelegateTime = System.currentTimeMillis();
        if (fadeIntoDelegate != null) {
            BufferedImage overlayImage = ImageUtils.copyAspectScaled(fadeIntoDelegate.get(),finalSize);
            float alpha = findAlpha();
            if (alpha >= 1) {
                alpha = 1;
                delegate.deactivate();
                delegate = fadeIntoDelegate;
                fadeIntoDelegate = null;
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            ImageUtils.drawAspectScaled(g, overlayImage, finalSize);
        }
        long fadeDelegateTime = System.currentTimeMillis();
        System.out.printf("base %d, scale %d, fade %d\n", prepBaseTime - start, scaleDelegateTime - prepBaseTime, fadeDelegateTime - scaleDelegateTime);
        return baseImage;
    }
}
