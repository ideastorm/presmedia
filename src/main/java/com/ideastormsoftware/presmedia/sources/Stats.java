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
package com.ideastormsoftware.presmedia.sources;

public class Stats {

    private long min = Long.MAX_VALUE;
    private long max = 0;
    private long sum = 0;
    private int count = 0;

    public void addValue(long value) {
        if (value < min) {
            min = value;
        }
        if (value > max) {
            max = value;
        }
        sum += value;
        count++;
    }

    public int getCount() {
        return count;
    }

    public long getMax() {
        return max;
    }

    public long getMin() {
        return min;
    }

    public long getSum() {
        return sum;
    }

    public long getAverage() {
        return sum / count;
    }

}
