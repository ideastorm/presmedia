package com.ideastormsoftware.presmedia;

import com.ideastormsoftware.presmedia.sources.ColorSource;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import com.ideastormsoftware.presmedia.ui.RenderPane;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import javax.swing.JFrame;

public class Projector extends JFrame {

    RenderPane renderPane;

    public Projector() throws HeadlessException {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        renderPane = new RenderPane(new ColorSource());
        renderPane.setShowOverlay(false);
        add(renderPane);
        setTitle("Presmedia Projector");
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            moveToSecondaryScreen();
        }
        super.setVisible(visible);
    }

    public void setSource(ImageSource source) {
        renderPane.setSource(source);
    }

    private void moveToSecondaryScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        boolean multiMonitor = gd.length > 1;
        if (multiMonitor) {
            DisplayMode currentMode = gd[1].getDisplayMode();
            Dimension primaryMonitorSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(primaryMonitorSize.width, 0);
            setSize(currentMode.getWidth(), currentMode.getHeight());
        } else {
            setLocation(0, 0);
            setSize(640, 480);
        }
        setUndecorated(multiMonitor);
        setAlwaysOnTop(multiMonitor);
    }

}
