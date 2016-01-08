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

import com.ideastormsoftware.presmedia.sources.ScaledSource;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

public abstract class ImageFilter extends ScaledSource {

    protected abstract BufferedImage filter(BufferedImage source);

    @Override
    protected void setScaledImage(BufferedImage img) {
        super.setScaledImage(filter(img));
    }

    public Supplier<BufferedImage> getSource() {
        return this.source;
    }
}