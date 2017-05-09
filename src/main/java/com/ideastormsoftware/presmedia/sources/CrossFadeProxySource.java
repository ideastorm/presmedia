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
    private static final double FADE_DURATION = 1;
    private long fadeStartTime;
    private final Stats stats = new Stats();
    private final List<ImageOverlay> postScaleOverlays = new ArrayList<>();
    private final Object fadeSourceMutex = new Object();

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
    public CrossFadeProxySource setSource(Supplier<Optional<BufferedImage>> source) {
        stats.reset();
        log("Starting fade");
        if (source instanceof Startable) {
            try {
                log("starting source");
                ((Startable) source).start(() -> {
                    log("source started, starting fade");
                    setFadeSourceInternal(source);
                });
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } else {
            log("non-startable source, starting fade");
            setFadeSourceInternal(source);
        }
        return this;
    }

    public CrossFadeProxySource setSourceNoFade(Supplier<Optional<BufferedImage>> source) {
        stats.reset();
        synchronized (fadeSourceMutex) {
            fadeIntoSource = null;
        }
        if (getSource() instanceof CleanCloseable) {
            log("Closing existing closeable source");
            ((CleanCloseable) getSource()).close();
        }

        if (source instanceof Startable) {
            try {
                log("starting source");
                ((Startable) source).start(() -> {
                    log("source started, setting base source");
                    super.setSource(source);
                });
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } else {
            log("non-startable source, setting source");
            super.setSource(source);
        }
        return this;
    }

    private void setFadeSourceInternal(Supplier<Optional<BufferedImage>> source) {
        synchronized (fadeSourceMutex) {
            if (fadeIntoSource != null) {
                log("fade source already exists");
                if (getSource() instanceof CleanCloseable) {
                    log("Closing existing closeable source");
                    ((CleanCloseable) getSource()).close();
                }
                log("Setting fade source as base source");
                super.setSource(fadeIntoSource);
            }
            log("setting fade source");
            fadeIntoSource = source;
            fadeStartTime = System.nanoTime();
            log("fade source set, fade start time set");
        }
    }

    private float findAlpha() {
        long currentStepTime = System.nanoTime();
        double fadeProgress = (currentStepTime - fadeStartTime) / 1_000_000_000.0;
        return (float) (fadeProgress / FADE_DURATION);
    }

    private void log(String message) {
        System.out.println(message);
    }

    @Override
    protected void drawScaled(Graphics2D g, Optional<BufferedImage> img, Dimension targetSize, Optional<Scalr.Method> quality) {
        long startTime = System.nanoTime();
        if (getSource().getClass().isAnnotationPresent(AspectAgnostic.class)) {
            ImageUtils.drawIgnoringAspect(g, img, targetSize, quality);
        } else {
            ImageUtils.drawAspectScaled(g, img, targetSize, quality);
        }
        synchronized (fadeSourceMutex) {
            if (fadeIntoSource != null) {
                Optional<BufferedImage> overlayImage;
                if (fadeIntoSource.getClass().isAnnotationPresent(AspectAgnostic.class)) {
                    overlayImage = ImageUtils.copyScaled(fadeIntoSource.get(), targetSize, quality);
                } else {
                    overlayImage = ImageUtils.copyAspectScaled(fadeIntoSource.get(), targetSize, quality);
                }
                float alpha = findAlpha();
                if (alpha >= 1) {
                    alpha = 1;
                    if (getSource() instanceof CleanCloseable) {
                        log("closing old base source");
                        ((CleanCloseable) getSource()).close();
                    }
                    log("Finalizing fade");
                    super.setSource(fadeIntoSource);
                    fadeIntoSource = null;
                } else {
                    log("Fading using alpha " + alpha);
                }
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                ImageUtils.drawAspectScaled(g, overlayImage, targetSize, quality);
            }
        }
        for (ImageOverlay overlay : postScaleOverlays) {
            overlay.apply(g, targetSize);
        }
        stats.addValue(System.nanoTime() - startTime);
    }

    public double getFps() {
        return stats.getRate();
    }
}
