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

import com.ideastormsoftware.presmedia.util.Stats;
import com.ideastormsoftware.presmedia.util.ImageUtils;
import com.ideastormsoftware.presmedia.filters.ImageOverlay;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.imgscalr.Scalr;

public class CrossFadeProxySource extends ScaledSource {

    private Supplier<Optional<BufferedImage>> fadeIntoSource;
    private static final double fadeDuration = 0.5;
    private long fadeStartTime;
    private final Stats stats = new Stats();
    private final List<ImageOverlay> postScaleOverlays = new ArrayList<>();
    private Optional<BufferedImage> lastImage = Optional.empty();
    private final Stats duplicates = new Stats();

    public void appendOverlay(ImageOverlay overlay) {
        synchronized (postScaleOverlays) {
            postScaleOverlays.add(overlay);
        }
    }

    public void removeOverlay(ImageOverlay overlay) {
        synchronized (postScaleOverlays) {
            postScaleOverlays.remove(overlay);
        }
    }

    @Override
    public ScaledSource setSource(Supplier<Optional<BufferedImage>> source) {
//        reportDuplicates();
        duplicates.reset();
        stats.reset();
        if (source instanceof Startable) {
            ((Startable) source).start();
        }
        if (fadeIntoSource != null) {
            if (getSource() instanceof CleanCloseable) {
                ((CleanCloseable) getSource()).close();
            }
            super.setSource(fadeIntoSource);
        }
        setFadeSourceInternal(source);
        return this;
    }

    public CrossFadeProxySource setSourceNoFade(Supplier<Optional<BufferedImage>> source) {
        if (source instanceof Startable) {
            ((Startable) source).start();
        }
        if (getSource() instanceof CleanCloseable) {
            ((CleanCloseable) getSource()).close();
        }
        setSource(source);
        fadeIntoSource = null;
        return this;
    }

    private void setFadeSourceInternal(Supplier<Optional<BufferedImage>> source) {
        fadeIntoSource = source;
        fadeStartTime = System.nanoTime();
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
    protected void drawScaled(Graphics2D g, Optional<BufferedImage> img, Dimension targetSize, Optional<Scalr.Method> quality) {
        if (img == lastImage) {
            duplicates.addValue(1);
        }
        lastImage = img;
        long startTime = System.nanoTime();
        if (!img.isPresent()) {
            log("no source image");
        }
        ImageUtils.drawAspectScaled(g, img, targetSize, quality);
        if (fadeIntoSource != null) {
            Optional<BufferedImage> overlayImage = ImageUtils.copyAspectScaled(fadeIntoSource.get(), targetSize, quality);
            float alpha = findAlpha();
            if (alpha >= 1) {
                alpha = 1;
                if (getSource() instanceof CleanCloseable) {
                    ((CleanCloseable) getSource()).close();
                }
                setSource(fadeIntoSource);
                fadeIntoSource = null;
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            ImageUtils.drawAspectScaled(g, overlayImage, targetSize, quality);
        }
        for (ImageOverlay overlay : postScaleOverlays) {
            overlay.apply(g, targetSize);
        }
        stats.addValue(System.nanoTime() - startTime);
    }

    public double getFps() {
        return stats.getRate();
    }

    public void reportDuplicates() {
        duplicates.report("Duplicate frames entering cross fade proxy");
    }
}
