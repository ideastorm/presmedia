package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ConfigurationContext;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Video extends ImageSource {

    private File sourceFile;

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public BufferedImage getCurrentImage() {
//        if (grabber != null) {
//            try {
//                return AWTUtil.toBufferedImage(grabber.getNativeFrame());
//            } catch (IOException ex) {
//            }
//        }
        return new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        return false;
    }

    @Override
    public void replaceSource(ImageSource source, ImageSource replacement) {
    }

    @Override
    protected String sourceDescription() {
        return "Video";
    }

}
