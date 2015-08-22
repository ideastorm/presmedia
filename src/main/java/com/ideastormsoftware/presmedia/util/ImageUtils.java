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
package com.ideastormsoftware.presmedia.util;

import com.ideastormsoftware.presmedia.sources.ImageSource;
import com.ideastormsoftware.presmedia.sources.ScaledSource;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import org.imgscalr.Scalr;

public final class ImageUtils {
    
    public static void log(String format, Object... params) {
        System.out.printf(format+"\n", params);
    }

    public static BufferedImage copy(BufferedImage img) {
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        copy.getGraphics().drawImage(img, 0, 0, null);
        return copy;
    }

    public static Dimension aspectScaledSize(int sourceWidth, int sourceHeight, int destWidth, int destHeight) {
        double sourceRatio = 1.0 * sourceHeight / sourceWidth;
        double destRatio = 1.0 * destHeight / destWidth;
        if (destRatio > sourceRatio) //dest is narrower than source
        {
            return new Dimension(destWidth, (int) (destWidth * sourceRatio));
        } else {
            return new Dimension((int) (destHeight / sourceRatio), destHeight);
        }
    }

    public static BufferedImage copyAspectScaled(BufferedImage img, int width, int height) {
        if (img == null) {
            return emptyImage();
        }
        if (img.getWidth() == width && img.getHeight() == height) {
            return img;
        }
        BufferedImage copy = new BufferedImage(width, height, img.getType());
        Graphics2D g = copy.createGraphics();
        drawAspectScaled(g, img, width, height);
        return copy;
    }

    public static BufferedImage emptyImage() {
        return new BufferedImage(320, 240, BufferedImage.TYPE_4BYTE_ABGR);
    }

    public static BufferedImage emptyImage(Dimension size) {
        return new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR);
    }

    private static final Deque<Long> last30 = new ArrayDeque<>(32);
    private static Scalr.Method scaleMethod = Scalr.Method.BALANCED;

    private static void reportScaleTime(long nanos) {
        last30.addLast(nanos);
        while (last30.size() > 30) {
            last30.removeFirst();
        }
        double total = 0;
        for (Long reading : last30) {
            total += reading;
        }
        double avg = total / last30.size();
        if (avg > 20_000_000 && scaleMethod != Scalr.Method.SPEED) {
            log("Scaling method: speed");
            scaleMethod = Scalr.Method.SPEED;
        } else if (avg > 10_000_000 && scaleMethod != Scalr.Method.BALANCED) {
            log("Scaling method: balanced");
            scaleMethod = Scalr.Method.BALANCED;
        } else if (avg <= 10_000_000 && scaleMethod != Scalr.Method.QUALITY) {
            log("Scaling method: quality");
            scaleMethod = Scalr.Method.QUALITY;
        }
    }

    public static BufferedImage copyAspectScaled(BufferedImage img, Dimension size) {
        if (img == null) {
            return emptyImage(size);
        }

        long nanoStart = System.nanoTime();
        try {
            return Scalr.resize(img, scaleMethod, size.width, size.height);
        } finally {
            reportScaleTime(System.nanoTime() - nanoStart);
        }
    }

    public static void drawAspectScaled(Graphics2D g, BufferedImage img, Dimension size) {
        drawAspectScaled(g, img, size.width, size.height);
    }

    public static void drawAspectScaled(Graphics2D g, BufferedImage img, int width, int height) {
        Object interpolationType;
        switch (scaleMethod) {
            case BALANCED:
            default:
                interpolationType = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                break;
            case QUALITY:
                interpolationType = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                break;
            case SPEED:
                interpolationType = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                break;
        }
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationType);
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);
        Dimension scaledSize = aspectScaledSize(img.getWidth(), img.getHeight(), width, height);
        Point offset = new Point((width - scaledSize.width) / 2, (height - scaledSize.height) / 2);
        long nanoStart = System.nanoTime();
        g.drawImage(img, offset.x, offset.y, scaledSize.width, scaledSize.height, null);
        reportScaleTime(System.nanoTime() - nanoStart);
    }

    public static ScaledSource scaleSource(final ImageSource source) {
        if (source instanceof ScaledSource) {
            return (ScaledSource) source;
        }
        return (Dimension size) -> copyAspectScaled(source.getCurrentImage(), size);
    }

    private ImageUtils() {
    }

}
