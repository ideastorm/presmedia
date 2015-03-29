package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ConfigurationContext;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author Phillip
 */
public abstract class ImageSource {

    private final List<Listener> listeners = new ArrayList<>();

    public abstract BufferedImage getCurrentImage();

    public abstract JPanel getConfigurationPanel(ConfigurationContext context);

    public abstract boolean dependsOn(ImageSource source);

    public abstract void replaceSource(ImageSource source, ImageSource replacement);

    protected abstract String sourceDescription();

    @Override
    public String toString() {
        return sourceDescription();
    }

    protected void warn(String title, String format, Object... items) {
        JOptionPane.showMessageDialog(null, String.format(format, items), title, JOptionPane.WARNING_MESSAGE);
    }
    
    protected void fireChanged() {
        for (Listener l : listeners) {
            l.sourceChanged(this);
        }
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    public interface Listener {

        public void sourceChanged(ImageSource source);
    }
}
