package com.ideastormsoftware.presmedia.filters;

import com.ideastormsoftware.presmedia.sources.ScaledSource;
import com.ideastormsoftware.presmedia.util.ImageUtils;
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
import java.util.function.Supplier;
import javax.imageio.ImageIO;

public class Slideshow extends ImageFilter {

    private List<File> files = Collections.EMPTY_LIST;
    private String title = "";
    private boolean randomize;
    private int perSlideDelay = 5000;
    private transient int index = -1;
    private transient long lastTransition = 0;
    private transient BufferedImage lastImage = ImageUtils.emptyImage();
    private transient BufferedImage fadeImage;
    private transient final List<File> unseenImages = new ArrayList<>();

    private void log(String format, Object... params) {
        System.out.printf(format + "\n", params);
    }

    @Override
    public <T extends ScaledSource> T setSource(Supplier<BufferedImage> source) {
        return super.setSource(ImageUtils::emptyImage);
    }

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
    protected BufferedImage filter(BufferedImage img) {
        long now = System.currentTimeMillis();
        //if it's time to transition, this will become true
        //  lastTransition is set to now to break out of the loop once we've
        //  got a new file to transition to.
        while (now - lastTransition > perSlideDelay) {
            if (files == null || files.isEmpty()) {
                lastImage = ImageUtils.emptyImage();
                lastTransition = now;
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
        float fadeDelay = perSlideDelay * 0.2f;
        float alpha = (now - lastTransition) / fadeDelay;
        
        BufferedImage compositeImage = img;

        if (now - lastTransition > fadeDelay) {
            if (fadeImage != null) {
                lastImage = fadeImage;
                fadeImage = null;
            }
            compositeImage = ImageUtils.copyAspectScaled(lastImage, targetSize);
        } else {
            Graphics2D g = compositeImage.createGraphics();
            ImageUtils.drawAspectScaled(g, lastImage, targetSize);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            ImageUtils.drawAspectScaled(g, fadeImage, targetSize);
        }
        return compositeImage;
    }
    
    public String getTitle() {
        return title;
    }

    public List<File> getFiles() {
        return files;
    }

    public int getSlideDelay() {
        return perSlideDelay;
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

    public void setSlideDelay(int slideDelay) {
        perSlideDelay = slideDelay;
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
