package com.ideastormsoftware.presmedia.filters;

import com.ideastormsoftware.cvutils.filters.AbstractFilter;
import com.ideastormsoftware.cvutils.util.ImageUtils;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.imageio.ImageIO;

public class Slideshow extends AbstractFilter {

    private static final int SLIDE_DELAY = 5000;
    private static final float FADE_DELAY = 1000.0f;

    private List<File> files = Collections.EMPTY_LIST;
    private String title = "";
    private boolean randomize;
    private transient int index = -1;
    private transient long lastTransition = 0;
    private transient BufferedImage lastImage = ImageUtils.emptyImage();
    private transient BufferedImage fadeImage;
    private transient final List<File> unseenImages = new ArrayList<>();

    private File pickRandomFile() {
        if (unseenImages.isEmpty()) {
            unseenImages.addAll(files);
        }
        int randomIndex = (int) (Math.random() * unseenImages.size());
        return unseenImages.remove(randomIndex);
    }

    private File nextFile() {
        index++;
        if (index >= files.size()) {
            index = 0;
        }
        return files.get(index);
    }

    @Override
    protected BufferedImage filter(BufferedImage original, Dimension targetScreenSize) {
        long now = System.currentTimeMillis();
        BufferedImage compositeImage;
        while (now - lastTransition > SLIDE_DELAY) {
            if (files == null || files.isEmpty()) {
                lastImage = ImageUtils.emptyImage();
            } else {
                File nextFile = randomize ? pickRandomFile() : nextFile();
                try {
                    fadeImage = ImageIO.read(nextFile);
                    lastTransition = now;
                } catch (IOException ex) {
                    files.remove(nextFile);
                }
            }
        }
        float alpha = (now - lastTransition) / FADE_DELAY;
        if (now - lastTransition > FADE_DELAY) {
            if (fadeImage != null) {
                lastImage = fadeImage;
                fadeImage = null;
            }
            compositeImage = ImageUtils.copyAspectScaled(lastImage, targetScreenSize);
        } else {
            compositeImage = ImageUtils.copyAspectScaled(lastImage, targetScreenSize);
            Graphics2D g = compositeImage.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            ImageUtils.drawAspectScaled(g, fadeImage, targetScreenSize);
        }
        return compositeImage;
    }

    public String getTitle() {
        return title;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void setFiles(Enumeration<File> files) {
        List<File> fileList = new ArrayList<>();
        while (files.hasMoreElements()) {
            fileList.add(files.nextElement());
        }
        setFiles(fileList);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRandomize() {
        return randomize;
    }

    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }

    @Override
    public String toString() {
        return title != null && !title.isEmpty() ? title : "Untitled Slideshow";
    }

}
