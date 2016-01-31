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
    private Long startTimestamp = null;

    public void reset() {
        min = Long.MAX_VALUE;
        max = 0;
        sum = 0;
        count = 0;
        startTimestamp = null;
    }

    public void addValue(long value) {
        if (startTimestamp == null)
            startTimestamp = System.currentTimeMillis();
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
    
    public double getRate() {
        if (startTimestamp == null)
            return 0;
        long currentTime = System.currentTimeMillis();
        long totalTime = currentTime - startTimestamp;
        return count / (totalTime * 0.001);
    }
    
    public void report(String title)
    {
        boolean saturated = 1_000_000_000 / getRate() <= getAverage();
        System.out.printf("%s:\n\tmin: %d\n\tmax: %d\n\tavg: %d\n\tcount: %d\n\trate: %01.2f\n\tsaturated: %s\n", title, getMin(), getMax(), getAverage(), getCount(), getRate(), String.valueOf(saturated));
    }
}