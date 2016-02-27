package com.ideastormsoftware.presmedia.filters;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

public class Lyrics implements ImageOverlay {

    private String title;
    private List<String> lines;
    private transient int index = 0;
    private transient long transitionStartTs;

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
        index = 0;
    }

    public void reset() {
        index = 0;
    }

    public void advance() {
        if (lines.size() < 3) {
            return;
        }
        if (index < lines.size() - 3) {
            transitionStartTs = System.currentTimeMillis();
        }
    }

    private float transitionShift() {
        float delta = (System.currentTimeMillis() - transitionStartTs) / 1000.0f;
        if (delta < 0) {
            return 0;
        } else if (delta > 1) {
            if (delta < 1.5) {
                transitionStartTs = 0;
                index++;
            }
            return 0;
        } else {
            return delta;
        }
    }

    @Override
    public void apply(Graphics2D graphics, Dimension targetSize){
        //Start by finding ow wide we can be to fit in a 4:3 aspect ratio screen
        int w = targetSize.width;
        if (targetSize.width * 3 > targetSize.height * 4) {
            w = targetSize.height * 4 / 3;
        }
        int aspectOffset = (targetSize.width - w) / 2;
        int offset = w/10; 
        
        int shadowWidth = w - offset;
        w = w * 8 / 10;
       
        float estimatedCharacterHeight = w / 40 * 2.5f;
        int totalHeight = (int) (estimatedCharacterHeight * 3.2f);
        int vOffset = (int) (targetSize.height - totalHeight);

        Font font = Font.decode("Verdana");
        font = font.deriveFont(estimatedCharacterHeight * 72 / 96);

        graphics.setColor(new Color(0, 0, 0, 127));
        graphics.fillRect(aspectOffset + offset / 2, vOffset - offset / 2, shadowWidth, totalHeight + offset / 2);
        BufferedImage text = new BufferedImage(w, totalHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D t = text.createGraphics();
        t.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        t.setFont(font);
        t.setComposite(AlphaComposite.Clear);
        t.fillRect(0, 0, w, totalHeight);
        t.setComposite(AlphaComposite.Src);
        t.setColor(Color.white);
        float lineOffset = estimatedCharacterHeight - transitionShift() * estimatedCharacterHeight;
        for (int i = index; i < lines.size() && i < index + 4; i++) {
            t.drawString(lines.get(i), 0, lineOffset);
            lineOffset += estimatedCharacterHeight;
        }
        graphics.drawImage(text, aspectOffset + offset, vOffset - offset / 4, null);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
