package com.ideastormsoftware.presmedia;

import com.ideastormsoftware.presmedia.sources.ColorSource;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Phillip
 */
public class ConfigurationContext extends ImageSource implements ListModel<ImageSource>, ImageSource.Listener {

    private final List<ImageSource> configuredSources = new ArrayList<>();
    private final List<ListDataListener> listeners = new ArrayList<>();

    public void addSource(ImageSource source) {
        int index = configuredSources.size();
        configuredSources.add(source);
        source.addListener(this);
        notifyListeners(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index));
    }

    public List<ImageSource> getSources() {
        return Collections.unmodifiableList(configuredSources);
    }

    public void removeSource(ImageSource source) {
        int index = configuredSources.indexOf(source);
        if (index >= 0) {
            configuredSources.remove(source);
            for (ImageSource configSource : configuredSources) {
                if (configSource.dependsOn(source)) {
                    configSource.replaceSource(source, new ColorSource());
                }
            }
            notifyListeners(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index));
        }
    }

    private void notifyListeners(ListDataEvent event) {
        for (ListDataListener listener : listeners) {
            listener.contentsChanged(event);
        }
    }

    @Override
    public int getSize() {
        return configuredSources.size();
    }

    @Override
    public ImageSource getElementAt(int i) {
        return configuredSources.get(i);
    }

    @Override
    public void addListDataListener(ListDataListener ll) {
        listeners.add(ll);
    }

    @Override
    public void removeListDataListener(ListDataListener ll) {
        listeners.remove(ll);
    }

    @Override
    public void sourceChanged(ImageSource source) {
        int index = configuredSources.indexOf(source);
        if (index >= 0) {
            notifyListeners(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index));
        }
    }

    @Override
    public BufferedImage getCurrentImage() {
        if (configuredSources.isEmpty())
            return new BufferedImage(1,1,BufferedImage.TYPE_3BYTE_BGR);
        return configuredSources.get(configuredSources.size()-1).getCurrentImage();
    }

    @Override
    public JPanel getConfigurationPanel(ConfigurationContext context) {
        return new JPanel();
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        return configuredSources.contains(source);
    }

    @Override
    public void replaceSource(final ImageSource source, final ImageSource replacement) {
        configuredSources.replaceAll((ImageSource t) -> t.equals(source) ? replacement : t);
    }

    @Override
    protected String sourceDescription() {
        return "Filter Graph";
    }
}
