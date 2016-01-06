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
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;


public class Deinterlace extends ImageFilter{

    @Override
    protected BufferedImage filter(BufferedImage source, Dimension targetScreenSize) {
        int w = source.getWidth();
        WritableRaster raster = source.getRaster();
        SampleModel sampleModel = raster.getSampleModel();
        for (int i = 0; i < source.getHeight()-2; i+=2) {
            for (int b = 0; b < sampleModel.getNumBands(); b++) {
                int[] topRow = raster.getSamples(0, i, w, 1, b, new int[w]);
                int[] bottomRow = raster.getSamples(0, i+2, w, 1, b, new int[w]);
                raster.setSamples(0, i+1, w, 1, b, mergeRows(topRow, bottomRow));
            }
        }
        return ImageUtils.copyAspectScaled(source, targetScreenSize);
    }

    private int[] mergeRows(int[] firstRow, int[] secondRow)
    {
        int[] merged = new int[firstRow.length];
        for (int i = 0; i < merged.length; i++)
            merged[i] = (int) (firstRow[i]*0.5 + secondRow[i]*0.5);
        return merged;
    }
}
