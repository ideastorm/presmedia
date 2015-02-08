package com.ideastormsoftware.presmedia.filters;

import com.ideastormsoftware.presmedia.ConfigurationContext;
import com.ideastormsoftware.presmedia.ImageUtils;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.opencv.core.Size;

/**
 * @author Phillip
 */
public class Overlay extends AbstractFilter {

    private ImageSource base;
    private ImageSource overlay;
    private double opacity;
    private Size size;
    private Point origin;

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

    public ImageSource getBase() {
        return base;
    }

    public void setBase(ImageSource base) {
        this.base = base;
    }

    public ImageSource getOverlay() {
        return overlay;
    }

    public void setOverlay(ImageSource overlay) {
        this.overlay = overlay;
    }

    @Override
    public JPanel getConfigurationPanel(ConfigurationContext context) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        if (base != null && base.equals(source))
            return true;
        return (overlay != null && overlay.equals(source));
    }

    @Override
    public void replaceSource(ImageSource source, ImageSource replacement) {
        if (base != null && base.equals(source))
            base = replacement;
        if (overlay != null && overlay.equals(source))
            overlay = replacement;
    }
}
