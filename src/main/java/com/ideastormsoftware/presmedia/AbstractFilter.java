package com.ideastormsoftware.presmedia;

import java.util.Arrays;
import java.util.List;

/**
 * @author Phillip
 */
public abstract class AbstractFilter implements ImageSource {

    private boolean paused = false;
    private final List<ImageSource> sources;

    public AbstractFilter(ImageSource... sources) {
        this.sources = Arrays.asList(sources);
    }

    @Override
    public void togglePaused() {
        setPaused(!paused);
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
        for (ImageSource source : sources) {
            source.setPaused(paused);
        }
    }
}
