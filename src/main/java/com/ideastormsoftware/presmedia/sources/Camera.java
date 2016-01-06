/*
 * Copyright 2015 Phillip Hayward <phil@pjhayward.net>.
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

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Camera extends ImageSource implements PropertyChangeListener {
    private static final Set<Camera> activeCameras = new HashSet<>();

    public static int availableCameras() {
        return DSCapture.queryDevices()[0].length;
    }
    
    private DSCapture capture;

    private Camera() {
        this(0);
    }
    
    public Camera(Integer cameraIndex) {
        DSFilterInfo[][] filterInfo = DSCapture.queryDevices();
        capture = new DSCapture(DSFiltergraph.JAVA_POLL | DSFiltergraph.FRAME_CALLBACK, filterInfo[0][cameraIndex], false, DSFilterInfo.doNotRender(), this);
        activeCameras.add(this);
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
        capture.dispose();
        activeCameras.remove(this);
    }

    @Override
    public BufferedImage get() {
        return currentImage;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Integer.parseInt(evt.getNewValue().toString()) == DSFiltergraph.FRAME_NOTIFY)
        {
            long start = System.nanoTime();
            currentImage = capture.getImage();
            long end = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(end - start);
            if (duration > 2)
                System.out.printf("Capture getImage took %d ms\n", duration);
        }
    }

    public static void closeAll()
    {
        Set<Camera> cleanSet = new HashSet<>(activeCameras);
        for (Camera camera : cleanSet) {
            camera.deactivate();
        }
    }
}
