package com.ideastormsoftware.presmedia.sources;

import java.awt.image.BufferedImage;
import java.io.File;

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
