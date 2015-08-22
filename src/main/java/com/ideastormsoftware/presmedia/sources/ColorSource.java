/*
 * Copyright 2015 Phillip Hayward <phil@pjhayward.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ColorSource extends ImageSource {

    private final BufferedImage image;
    private Color color;

    public ColorSource() {
        this(Color.black);
    }

    public ColorSource(Color color) {
        image = ImageUtils.emptyImage();
        setColor(color);
    }

    public final void setColor(Color color) {
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.Src); //ignore current contents, even if the incoming color has an alpha component
        g.setColor(color);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public BufferedImage getCurrentImage() {
        return image;
    }
}
