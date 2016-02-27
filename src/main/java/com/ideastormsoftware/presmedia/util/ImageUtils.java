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

import com.ideastormsoftware.presmedia.sources.ScaledSource;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;
import org.imgscalr.Scalr;

public final class ImageUtils {

    private static Scalr.Method method = Scalr.Method.BALANCED; //.Method.valueOf(System.getProperty("scalr.method", "BALANCED"));

    public static void setScalingMethod(Scalr.Method newMethod) {
        method = newMethod;
    }

    public static BufferedImage copy(BufferedImage img) {
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        copy.getGraphics().drawImage(img, 0, 0, null);
        return copy;
    }

    private static BufferedImage toABGR(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
            return image;
        }
        BufferedImage workImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        workImage.getGraphics().drawImage(image, 0, 0, null);
        return workImage;
    }

    private static BufferedImage toByteGrayscale(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return image;
        }
        BufferedImage workImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        workImage.getGraphics().drawImage(image, 0, 0, null);
        return workImage;
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
        return Scalr.resize(img, method, width, height);
    }

    public static BufferedImage emptyImage() {
        return new BufferedImage(320, 240, BufferedImage.TYPE_4BYTE_ABGR);
    }

    public static BufferedImage emptyImage(Dimension size) {
        size = new Dimension(size.width > 0 ? size.width : 1, size.height > 0 ? size.height : 1);
        return new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR);
    }

    public static BufferedImage copyAspectScaled(BufferedImage img, Dimension size) {
        if (img == null) {
            return emptyImage(size);
        }
        if (size.width < 1 || size.height < 1) {
            return emptyImage();
        }
        return Scalr.resize(img, method, size.width, size.height);
    }

    public static void drawAspectScaled(Graphics2D g, BufferedImage img, Dimension size) {
        drawAspectScaled(g, img, size.width, size.height);
    }

    public static void drawAspectScaled(Graphics2D g, BufferedImage img, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);
        if (img != null) {
            Dimension scaledSize = aspectScaledSize(img.getWidth(), img.getHeight(), width, height);
            Point offset = new Point((width - (int) scaledSize.width) / 2, (height - (int) scaledSize.height) / 2);
            g.drawImage(img, offset.x, offset.y, (int) scaledSize.width, (int) scaledSize.height, null);
        }
    }

    public static boolean needsScaling(BufferedImage img, Dimension targetSize) {
        return img.getWidth() != targetSize.getWidth() || img.getHeight() != targetSize.getHeight();
    }

    private ImageUtils() {
    }

}
