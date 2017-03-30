/*
 * Copyright 2016 Phillip Hayward <phil@pjhayward.net>.
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
import java.util.Optional;
import java.util.function.Supplier;

@AspectAgnostic
public class ColorSource implements Supplier<Optional<BufferedImage>> {

    private Color color;
    private final BufferedImage currentImage;

    public ColorSource() {
        this(Color.black);
    }

    public ColorSource(Color color) {
        this.currentImage = ImageUtils.emptyImage();
        setColor(color);
    }

    public final Color getColor() {
        return color;
    }

    public final void setColor(Color color) {
        Graphics2D g = currentImage.createGraphics();
        g.setComposite(AlphaComposite.Src); //ignore current contents, even if the incoming color has an alpha component
        g.setColor(color);
        g.fillRect(0, 0, currentImage.getWidth(), currentImage.getHeight());
        this.color = color;
    }

    @Override
    public Optional<BufferedImage> get() {
        return Optional.of(currentImage);
    }
}
