package com.ideastormsoftware.presmedia.filters;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class Name implements ImageOverlay {

    private String name = "";
    private String subtext = "";

    @Override
    public void apply(Graphics2D graphics, Dimension targetScreenSize) {
        //Start by finding ow wide we can be to fit in a 4:3 aspect ratio screen
        int w = targetScreenSize.width;
        if (targetScreenSize.width * 3 > targetScreenSize.height * 4) {
            w = targetScreenSize.height * 4 / 3;
        }
        int aspectOffset = (targetScreenSize.width - w) / 2;
        int shadowWidth = w * 9 / 10;
        w = w * 8 / 10;
        int offset = w / 8;
        float estimatedCharacterHeight = w / 40 * 2.5f;
        int totalHeight = (int) (estimatedCharacterHeight * 2.2f);
        int vOffset = (int) (targetScreenSize.height - totalHeight);

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
        t.drawLine(0, 0, w, 0);
        t.drawString(name, 0, estimatedCharacterHeight);
        t.setFont(font.deriveFont(font.getSize() * 0.8f));
        t.drawString(subtext, 0, estimatedCharacterHeight * 2);
        graphics.drawImage(text, aspectOffset + offset, vOffset - offset / 4, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtext() {
        return subtext;
    }

    public void setSubtext(String subtext) {
        this.subtext = subtext;
    }

    @Override
    public String toString() {
        return String.format("%s / %s", getName(), getSubtext());
    }
}
