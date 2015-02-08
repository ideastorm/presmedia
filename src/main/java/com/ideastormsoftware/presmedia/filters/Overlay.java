package com.ideastormsoftware.presmedia.filters;

import com.ideastormsoftware.presmedia.ImageUtils;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import org.opencv.core.Size;

/**
 * @author Phillip
 */
public class Overlay extends AbstractFilter {

    private final ImageSource base;
    private final ImageSource overlay;
    private double opacity;
    private Size size;
    private Point origin;

    public Overlay(ImageSource base, ImageSource overlay) {
        super(base, overlay);
        this.base = base;
        this.overlay = overlay;
    }

    @Override
    public BufferedImage getCurrentImage() {
        BufferedImage image = ImageUtils.copy(base.getCurrentImage());
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, (float) opacity));
        g.drawImage(overlay.getCurrentImage(), (int) origin.getX(), (int) origin.getY(), (int) size.width, (int) size.height, null);
        return image;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Point getOrigin() {
        return origin;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

}
