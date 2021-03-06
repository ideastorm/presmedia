package com.ideastormsoftware.presmedia.filters;

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
import java.util.Optional;
import javax.imageio.ImageIO;

public class Slideshow implements ImageOverlay {

    private List<File> files = Collections.EMPTY_LIST;
    private String title = "";
    private boolean randomize;
    private int perSlideDelay = 5000;
    private transient int index = -1;
    private transient long lastTransition = 0;
    private transient Optional<BufferedImage> lastImage = Optional.empty();
    private transient Optional<BufferedImage> fadeImage = Optional.empty();
    private transient final List<File> unseenImages = new ArrayList<>();

    private void log(String format, Object... params) {
        System.out.printf(format + "\n", params);
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
    public void apply(Graphics2D graphics, Dimension targetSize) {
        long now = System.currentTimeMillis();
        //if it's time to transition, this will become true
        //  lastTransition is set to now to break out of the loop once we've
        //  got a new file to transition to.
        while (now - lastTransition > perSlideDelay) {
            if (files == null || files.isEmpty()) {
                lastImage = Optional.empty();
                lastTransition = now;
            } else {
                File nextFile = randomize ? pickRandomFile() : nextFile();
                try {
                    fadeImage = Optional.of(ImageIO.read(nextFile));
                    lastTransition = now;
                } catch (IOException ex) {
                    files.remove(nextFile);
                }
            }
        }
        float fadeDelay = Math.min(perSlideDelay * 0.2f, 5_000);
        float alpha = (float) Math.sin(Math.PI/2 * (now - lastTransition) / fadeDelay); 

        Optional<BufferedImage> compositeImage = Optional.of(ImageUtils.emptyImage(targetSize));

        if (now - lastTransition > fadeDelay) {
            if (fadeImage != null) {
                lastImage = fadeImage;
                fadeImage = null;
            }
            compositeImage = lastImage;
        } else {
            Graphics2D g = compositeImage.get().createGraphics();
            ImageUtils.drawAspectScaled(g, lastImage, targetSize);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            ImageUtils.drawAspectScaled(g, fadeImage, targetSize);
        }
        ImageUtils.drawAspectScaled(graphics, compositeImage, targetSize);
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
