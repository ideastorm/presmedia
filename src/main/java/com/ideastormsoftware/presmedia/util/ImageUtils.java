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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Optional;
import org.imgscalr.Scalr;

public final class ImageUtils {

    private static Scalr.Method method = Scalr.Method.SPEED; //.Method.valueOf(System.getProperty("scalr.method", "BALANCED"));

    public static void setScalingMethod(Scalr.Method newMethod) {
        method = newMethod;
    }

//    public static BufferedImage copy(BufferedImage img) {
//        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
//        copy.getGraphics().drawImage(img, 0, 0, null);
//        return copy;
//    }
//    private static BufferedImage toABGR(BufferedImage image) {
//        if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
//            return image;
//        }
//        BufferedImage workImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
//        workImage.getGraphics().drawImage(image, 0, 0, null);
//        return workImage;
//    }
//
//    private static BufferedImage toByteGrayscale(BufferedImage image) {
//        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
//            return image;
//        }
//        BufferedImage workImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
//        workImage.getGraphics().drawImage(image, 0, 0, null);
//        return workImage;
//    }
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

    public static BufferedImage copyAspectScaled(BufferedImage img, int width, int height, Optional<Scalr.Method> quality) {
        if (img == null) {
            return emptyImage();
        }
        if (img.getWidth() == width && img.getHeight() == height) {
            return img;
        }
        return Scalr.resize(img, quality.orElse(method), width, height);
    }

    public static BufferedImage emptyImage() {
        return new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
    }

    public static BufferedImage emptyImage(Dimension size) {
        size = new Dimension(size.width > 0 ? size.width : 1, size.height > 0 ? size.height : 1);
        return new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
    }

    public static Optional<BufferedImage> copyAspectScaled(Optional<BufferedImage> img, Dimension size, Optional<Scalr.Method> quality) {
        if (!img.isPresent()) {
            return Optional.empty();
        }
        if (size.width < 1 || size.height < 1) {
            return Optional.empty();
        }
        return Optional.of(Scalr.resize(img.get(), quality.orElse(method), size.width, size.height));
    }

    public static void drawIgnoringAspect(Graphics2D g, Optional<BufferedImage> img, Dimension size) {
        drawIgnoringAspect(g, img, size, Optional.of(Scalr.Method.SPEED));
    }

    public static void drawIgnoringAspect(Graphics2D g, Optional<BufferedImage> img, Dimension size, Optional<Scalr.Method> quality) {
        setQualityHint(g, quality);
        if (img.isPresent()) {
            g.drawImage(img.get(), 0, 0, size.width, size.height, null);
        } else {
            g.setColor(Color.black);
            g.fillRect(0, 0, size.width, size.height);
        }
    }

    public static void drawAspectScaled(Graphics2D g, Optional<BufferedImage> img, Dimension size) {
        drawAspectScaled(g, img, size, Optional.empty());
    }

    public static void drawAspectScaled(Graphics2D g, Optional<BufferedImage> img, Dimension size, Optional<Scalr.Method> quality) {
        drawAspectScaled(g, img, size.width, size.height, quality);
    }

    public static void drawAspectScaled(Graphics2D g, Optional<BufferedImage> img, int width, int height, Optional<Scalr.Method> quality) {
        setQualityHint(g, quality);
        g.setColor(Color.black);
        if (img.isPresent()) {
            BufferedImage image = img.get();
            Dimension scaledSize = aspectScaledSize(image.getWidth(), image.getHeight(), width, height);
            Point offset = new Point((width - (int) scaledSize.width) / 2, (height - (int) scaledSize.height) / 2);
            if (offset.x == 0) {
                //need horizontal bars
                g.fillRect(0, 0, width, offset.y);
                g.fillRect(0, height - offset.y, width, offset.y);
            } else {
                //need vertical bars
                g.fillRect(0, 0, offset.x, height);
                g.fillRect(width - offset.x, 0, offset.x, height);
            }
            g.drawImage(image, offset.x, offset.y, (int) scaledSize.width, (int) scaledSize.height, null);
        } else {
            g.fillRect(0, 0, width, height);
        }
    }

    private static void setQualityHint(Graphics2D g, Optional<Scalr.Method> quality) {
        Object qualityHint;
        switch (quality.orElse(method)) {
            case SPEED:
                qualityHint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                break;
            case ULTRA_QUALITY:
                qualityHint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                break;
            default:
                qualityHint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                break;
        }
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, qualityHint);
    }

    public static boolean needsScaling(BufferedImage img, Dimension targetSize) {
        return img.getWidth() != targetSize.getWidth() || img.getHeight() != targetSize.getHeight();
    }

    private ImageUtils() {
    }

}
