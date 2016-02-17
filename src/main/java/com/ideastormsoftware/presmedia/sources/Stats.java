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

import java.util.ArrayDeque;
import java.util.Queue;

public class Stats {

    private long min = Long.MAX_VALUE;
    private long max = 0;
    private long sum = 0;
    private int count = 0;
    private final Queue<Long> timestamps = new ArrayDeque<>(32);

    public void reset() {
        min = Long.MAX_VALUE;
        max = 0;
        sum = 0;
        count = 0;
        timestamps.clear();
    }

    public void addValue(long value) {
        timestamps.offer(System.currentTimeMillis());
        if (timestamps.size() > 30)
            timestamps.poll();
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
        if (count == 0)
            return 0;
        return sum / count;
    }
    
    public double getRate() {
        if (timestamps.isEmpty())
            return 0;
        long currentTime = System.currentTimeMillis();
        long totalTime = currentTime - timestamps.peek();
        return timestamps.size() / (totalTime * 0.001);
    }
    
    public void report(String title)
    {
        long saturationDuration = (long) (1_000_000_000 / getRate());
        boolean saturated = saturationDuration <= getAverage();
        System.out.printf("%s:\n\tmin: %d\n\tmax: %d\n\tavg: %d\n\tcount: %d\n\trate: %01.2f\n\tsaturated: %s\n\tsaturation avg: %d\n", title, getMin(), getMax(), getAverage(), getCount(), getRate(), String.valueOf(saturated), saturationDuration);
    }
}