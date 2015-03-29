package com.ideastormsoftware.presmedia.filters;

import com.ideastormsoftware.presmedia.ConfigurationContext;
import com.ideastormsoftware.presmedia.ImageUtils;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.opencv.core.Size;

/**
 * @author Phillip
 */
public class Overlay extends AbstractFilter {
    
    public ImageSource base;
    public ImageSource overlay;
    double opacity = 1.0;
    Size size;
    Point origin = new Point(0, 0);

    @Override
    public BufferedImage getCurrentImage() {
        BufferedImage image = base != null ? ImageUtils.copy(base.getCurrentImage()) : ImageUtils.emptyImage();
        BufferedImage overlayImage = overlay != null ? overlay.getCurrentImage() : ImageUtils.emptyImage();
        Size localSize = size;
        if (localSize == null)
            localSize = new Size(overlayImage.getWidth(), overlayImage.getHeight());
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, (float) opacity));
        g.drawImage(overlayImage, (int) origin.getX(), (int) origin.getY(), (int) localSize.width, (int) localSize.height, null);
        return image;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
        fireChanged();
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
        fireChanged();
    }

    public Point getOrigin() {
        return origin;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
        fireChanged();
    }

    public ImageSource getBase() {
        return base;
    }

    public void setBase(ImageSource base) {
        this.base = base;
        fireChanged();
    }

    public ImageSource getOverlay() {
        return overlay;
    }

    public void setOverlay(ImageSource overlay) {
        this.overlay = overlay;
        fireChanged();
    }
    
    private void bindSelector(JComboBox<ImageSource> selector, String fieldName) {
        try {
            final Field field = getClass().getField(fieldName);
            selector.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                        try {
                            field.set(Overlay.this, e.getItem());
                            fireChanged();
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (NoSuchFieldException | SecurityException ex) {
                        ex.printStackTrace();
        }
    }

    @Override
    public JPanel getConfigurationPanel(ConfigurationContext context) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2, 5, 2));
        panel.add(new JLabel("Base source"));
        JComboBox<ImageSource> baseSelector = new JComboBox<>(context.getSources().toArray(new ImageSource[0]));
        bindSelector(baseSelector, "base");
        panel.add(baseSelector);
        panel.add(new JLabel("Overlay source"));
        JComboBox<ImageSource> overlaySelector = new JComboBox<>(context.getSources().toArray(new ImageSource[0]));
        bindSelector(overlaySelector, "overlay");
        panel.add(overlaySelector);
        panel.add(new JLabel("Opacity (0..1)"));
        panel.add(new JTextField(5));
        panel.add(new JLabel("Overlay width"));
        panel.add(new JTextField(5));
        panel.add(new JLabel("Overlay height"));
        panel.add(new JTextField(5));
        panel.add(new JLabel("Overlay left offset"));
        panel.add(new JTextField(5));
        panel.add(new JLabel("Overlay top offset"));
        panel.add(new JTextField(5));
        return panel;
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        if (base != null && base.equals(source)) {
            return true;
        }
        return (overlay != null && overlay.equals(source));
    }

    @Override
    public void replaceSource(ImageSource source, ImageSource replacement) {
        if (base != null && base.equals(source)) {
            base = replacement;
        }
        if (overlay != null && overlay.equals(source)) {
            overlay = replacement;
        }
        fireChanged();
    }

    @Override
    protected String sourceDescription() {
        String overlayName = overlay != null ? overlay.toString() : "-empty-";
        String baseName = base != null ? base.toString() : "-empty-r";
        return String.format("Overlay %s on %s", overlayName, baseName);
    }
}
