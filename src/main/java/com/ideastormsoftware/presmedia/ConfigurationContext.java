package com.ideastormsoftware.presmedia;

import com.ideastormsoftware.presmedia.sources.ColorSource;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.opencv.core.Size;

/**
 *
 * @author Phillip
 */
public class ConfigurationContext implements ListModel<ImageSource> {

    private final List<ImageSource> configuredSources = new ArrayList<>();
    private final List<ListDataListener> listeners = new ArrayList<>();

    public void addSource(ImageSource source) {
        int index = configuredSources.size();
        configuredSources.add(source);
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
}
