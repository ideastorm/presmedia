package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ConfigurationContext;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.swing.JPanel;

public class ProxySource extends ImageSource{
    private ImageSource delegate;
    
    public ProxySource(ImageSource delegate)
    {
        this.delegate = delegate;
    }
    
    public void setDelegate(ImageSource delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public BufferedImage getCurrentImage() {
        return delegate.getCurrentImage();
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        return Objects.equals(delegate, source);
    }

    @Override
    public void replaceSource(ImageSource source, ImageSource replacement) {
        if (Objects.equals(delegate, source))
            delegate = replacement;
    }

    @Override
    protected String sourceDescription() {
        return String.format("Proxy for %s", delegate.sourceDescription());
    }
    
}
