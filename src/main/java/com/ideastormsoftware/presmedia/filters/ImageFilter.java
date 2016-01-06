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

package com.ideastormsoftware.presmedia.filters;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import com.ideastormsoftware.presmedia.sources.ScaledSource;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public abstract class ImageFilter extends ImageSource implements ScaledSource {
    private ImageSource source;

    protected abstract BufferedImage filter(BufferedImage source, Dimension targetSize);

    @Override
    public BufferedImage getScaled(Dimension targetSize) {
        return filter(source.get(), targetSize);
    }

    public <T extends ImageFilter> T setSource(ImageSource delegate) {
        this.source = delegate;
        return (T) this;
    }

    public ImageSource getSource() {
        return this.source;
    }
}