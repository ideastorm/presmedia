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
import java.nio.IntBuffer;

/**
 *
 * @author Phil Hayward <phil@pjhayward.net>
 */
public class InterleavedIntConverter extends InterleavedConverter {

    @Override
    public byte[] prepareSamplesForPlayback(Buffer[] samples) {
        IntBuffer buffer = (IntBuffer) samples[0];
        Buffer[] convertedSamples = {convertIntToShortBuffer(buffer)};
        return super.prepareSamplesForPlayback(convertedSamples);
    }
    
}
