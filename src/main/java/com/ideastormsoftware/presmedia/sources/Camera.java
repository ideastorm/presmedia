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

import com.ideastormsoftware.presmedia.util.ImageUtils;
import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class Camera implements Supplier<BufferedImage>, PropertyChangeListener {

    private static final Set<Camera> activeCameras = new HashSet<>();
    private static final List<DSFilterInfo> cameraInfo = new ArrayList<>();
    private final DSFilterInfo deviceInfo;

    public static int availableCameras() {
        cameraInfo.clear();
        DSFilterInfo[][] filterInfo = DSCapture.queryDevices();
        for (DSFilterInfo capInfo : filterInfo[0]) {
            if (!"none".equals(capInfo.getCLSID())) {
                cameraInfo.add(capInfo);
            }
        }
        return cameraInfo.size();
    }

    private final DSCapture capture;
    private BufferedImage currentImage = ImageUtils.emptyImage();

    public Camera(Integer cameraIndex) {
        deviceInfo = cameraInfo.get(cameraIndex);
        System.out.printf("Attempting to start %s\n", deviceInfo.toString());
        capture = new DSCapture(DSFiltergraph.JAVA_POLL | DSFiltergraph.FRAME_CALLBACK, deviceInfo, false, DSFilterInfo.doNotRender(), this);
        activeCameras.add(this);
    }

    public void close() {
        capture.dispose();
        activeCameras.remove(this);
    }

    @Override
    public BufferedImage get() {
        return currentImage;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Integer.parseInt(evt.getNewValue().toString()) == DSFiltergraph.FRAME_NOTIFY) {
            this.currentImage = capture.getImage();
        }
    }

    public static void closeAll() {
        Set<Camera> cleanSet = new HashSet<>(activeCameras);
        for (Camera camera : cleanSet) {
            camera.close();
        }
    }

    public double getAspectRatio() {
        if (capture == null) {
            return 4 / 3d;
        }
        Dimension size = capture.getDisplaySize();
        return (double) size.width / (double) size.height;
    }
}
