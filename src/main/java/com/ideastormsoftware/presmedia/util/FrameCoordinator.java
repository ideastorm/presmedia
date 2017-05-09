/*
 * Copyright 2017 Phil Hayward<phil@pjhayward.net>.
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
package com.ideastormsoftware.presmedia.util;

import com.ideastormsoftware.presmedia.sources.ImageSource;
import com.ideastormsoftware.presmedia.ui.ImagePainter;

/**
 *
 * @author Phil Hayward<phil@pjhayward.net>
 */
public class FrameCoordinator {

    private static ImageSource activeSource;
    private static int defaultFrameRate = 30;

    public static void setFrameRate(int defaultFrameRate) {
        FrameCoordinator.defaultFrameRate = defaultFrameRate;
    }

    private FrameCoordinator() {
    }

    public static void setActiveSource(ImageSource source) {
        activeSource = source;
    }

    public static double getSourceFPS() {
        if (activeSource != null) {
            return Math.max(activeSource.getFps(), 2); 
        }
        return defaultFrameRate;
    }

    public static void notify(Object source) {
        if (source == activeSource) {
            synchronized (FrameCoordinator.class) {
                FrameCoordinator.class.notifyAll();
            }
        }
    }

    public static void waitForFrame() throws InterruptedException {
        synchronized (FrameCoordinator.class) {
            FrameCoordinator.class.wait();
        }
    }

    public static void waitForFrame(long timeout) throws InterruptedException {
        synchronized (FrameCoordinator.class) {
            FrameCoordinator.class.wait(timeout);
        }
    }
}
