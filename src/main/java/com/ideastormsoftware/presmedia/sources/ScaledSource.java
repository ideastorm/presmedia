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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.function.Supplier;
import org.imgscalr.Scalr;

/**
 *
 * @author Phillip Hayward <phil@pjhayward.net>
 */
public class ScaledSource {

    public Supplier<Optional<BufferedImage>> source = new ColorSource();

    public ScaledSource setSource(Supplier<Optional<BufferedImage>> source) {
        if (source == null) {
            source = new ColorSource();
        }
        this.source = source;
        return this;
    }

    public void scaleInto(Graphics2D graphics, Dimension targetSize, Optional<Scalr.Method> quality) {
        if (graphics == null) {
            throw new IllegalArgumentException("graphics is null");
        }
        if (targetSize == null) {
            throw new IllegalArgumentException("targetSize is null");
        }
        drawScaled(graphics, source != null ? source.get() : Optional.empty(), targetSize, quality);
    }

    protected void drawScaled(Graphics2D graphics, Optional<BufferedImage> image, Dimension targetSize, Optional<Scalr.Method> quality) {
        ImageUtils.drawAspectScaled(graphics, image, targetSize, quality);
    }
}
