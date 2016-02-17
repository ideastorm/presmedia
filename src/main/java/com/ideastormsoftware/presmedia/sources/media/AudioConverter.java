/*
 * Copyright 2016 Phil Hayward <phil@pjhayward.net>
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
package com.ideastormsoftware.presmedia.sources.media;

import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.bytedeco.javacpp.ShortPointer;

public abstract class AudioConverter {

    public abstract byte[] prepareSamplesForPlayback(Buffer[] samples);

    protected static Buffer convertDblToShortBuffer(DoubleBuffer buffer) {
        double[] bufferData;
        if (buffer.hasArray()) {
            bufferData = buffer.array();
        } else {
            bufferData = new double[buffer.limit()];
            buffer.get(bufferData);
        }
        ShortBuffer shortBuffer = ShortBuffer.allocate(bufferData.length);
        for (double c : bufferData) {
            shortBuffer.put((short) (c * Short.MAX_VALUE));
        }
        shortBuffer.rewind();
        return new ShortPointer(shortBuffer).asByteBuffer();
    }

    protected static Buffer convertFltToShortBuffer(FloatBuffer buffer) {
        float[] bufferData;
        if (buffer.hasArray()) {
            bufferData = buffer.array();
        } else {
            bufferData = new float[buffer.limit()];
            buffer.get(bufferData);
        }
        ShortBuffer shortBuffer = ShortBuffer.allocate(bufferData.length);
        for (double c : bufferData) {
            shortBuffer.put((short) (c * Short.MAX_VALUE));
        }
        shortBuffer.rewind();
        return new ShortPointer(shortBuffer).asByteBuffer();
    }

    protected static Buffer convertIntToShortBuffer(IntBuffer buffer) {
        int[] bufferData;
        if (buffer.hasArray()) {
            bufferData = buffer.array();
        } else {
            bufferData = new int[buffer.limit()];
            buffer.get(bufferData);
        }
        ShortBuffer shortBuffer = ShortBuffer.allocate(bufferData.length);
        for (double c : bufferData) {
            shortBuffer.put((short) (((double) c) / Integer.MAX_VALUE * Short.MAX_VALUE));
        }
        shortBuffer.rewind();
        return new ShortPointer(shortBuffer).asByteBuffer();
    }

}
