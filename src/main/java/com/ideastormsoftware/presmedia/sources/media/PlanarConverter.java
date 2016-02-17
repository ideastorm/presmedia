/*
 * Copyright 2016 Phil Hayward <phil@pjhayward.net>.
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
import java.nio.ByteBuffer;

/**
 *
 * @author Phil Hayward <phil@pjhayward.net>
 */
public class PlanarConverter extends AudioConverter {
    final int bitSampleSize;
    final int channels;

    public PlanarConverter(int bitSampleSize, int channels) {
        this.bitSampleSize = bitSampleSize;
        this.channels = channels;
    }

    @Override
    public byte[] prepareSamplesForPlayback(Buffer[] samples) {
        int sampleByteSize = (bitSampleSize + 7) / 8;
        int frameSize = channels * sampleByteSize;
        int frames = ((ByteBuffer) samples[0]).limit() / sampleByteSize;
        byte[] buffer = new byte[frames * frameSize];
        for (int frame = 0; frame < frames; frame++) {
            int frameOffset = frame * frameSize;
            for (int channel = 0; channel < channels; channel++) {
                int channelOffset = channel * sampleByteSize;
                ((ByteBuffer) samples[channel]).get(buffer, frameOffset + channelOffset, sampleByteSize);
            }
        }
        return buffer;
    }
    
}
