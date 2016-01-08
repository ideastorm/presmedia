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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

public class CrossFadeProxySource extends ScaledSource {

    private Supplier<BufferedImage> fadeIntoSource;
    private static final double fadeDuration = 0.5;
    private long fadeStartTime;

    @Override
    public <T extends ScaledSource> T setSource(Supplier<BufferedImage> source) {
        if (source instanceof Startable) {
            ((Startable) source).start();
        }
        if (this.source instanceof ImageFilter) {
            ((ImageFilter) this.source).setSource(source);
        } else {
            if (fadeIntoSource != null) {
                if (this.source instanceof CleanCloseable) {
                    ((CleanCloseable) this.source).close();
                }
                this.source = fadeIntoSource;
            }
            setFadeSourceInternal(source);
        }
        return (T) this;
    }

    public CrossFadeProxySource setSourceNoFade(Supplier<BufferedImage> source) {
        if (source instanceof Startable) {
            ((Startable) source).start();
        }
        if (this.source instanceof ImageFilter) {
            ((ImageFilter) this.source).setSource(source);
        } else {
            if (this.source instanceof CleanCloseable) {
                ((CleanCloseable) this.source).close();
            }
            this.source = source;
            fadeIntoSource = null;
        }
        return this;
    }

    private void setFadeSourceInternal(Supplier<BufferedImage> source) {
        fadeIntoSource = source;
        fadeStartTime = System.nanoTime();
    }

    public void setOverlay(ImageFilter overlay) {
        Supplier<BufferedImage> baseSource = source;
        if (baseSource instanceof ImageFilter) {
            baseSource = ((ImageFilter) baseSource).getSource();
        }
        if (overlay == null) {
            setFadeSourceInternal(baseSource);
        } else {
            overlay.setSource(baseSource);
            setFadeSourceInternal(overlay);
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
    protected void setScaledImage(BufferedImage img) {
        BufferedImage baseImage = ImageUtils.emptyImage(targetSize);
        Graphics2D g = baseImage.createGraphics();
        BufferedImage original = img;
        ImageUtils.drawAspectScaled(g, original, targetSize);
        if (fadeIntoSource != null) {
            BufferedImage overlayImage = ImageUtils.copyAspectScaled(fadeIntoSource.get(), targetSize);
            float alpha = findAlpha();
            if (alpha >= 1) {
                alpha = 1;
                if (this.source instanceof CleanCloseable) {
                    ((CleanCloseable) this.source).close();
                }
                source = fadeIntoSource;
                fadeIntoSource = null;
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            ImageUtils.drawAspectScaled(g, overlayImage, targetSize);
        }
        super.setScaledImage(baseImage);
    }
}
