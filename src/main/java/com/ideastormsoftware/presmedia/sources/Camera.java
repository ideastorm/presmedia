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

import com.ideastormsoftware.presmedia.util.Stats;
import com.ideastormsoftware.presmedia.util.ImageUtils;
import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Camera implements ImageSource, PropertyChangeListener {

    private static final ConcurrentHashMap<Integer, Camera> cameras = new ConcurrentHashMap<>();
    private static final List<DSFilterInfo> cameraInfo = new ArrayList<>();

    public static Camera getCamera(int cameraIndex) {
        return cameras.computeIfAbsent(cameraIndex, Camera::new);
    }

    public static void closeAllExcept(Set<Integer> cameraIndices) {
        Set<Camera> active = new HashSet<>(cameras.values());
        for (Camera cam : active) {
            if (!cameraIndices.contains(cam.cameraIndex)) {
                cam.close();
            }
        }
    }

    private final DSFilterInfo deviceInfo;
    private final Stats captureStats = new Stats();
    private final DSCapture capture;
    private BufferedImage currentImage = ImageUtils.emptyImage();

    static {
        DSEnvironment.unlockDLL("phil@pjhayward.net", 753608, 1702561, 0);
    }
    private final Integer cameraIndex;

    private void log(String format, Object... params) {
        System.out.printf(format + "\n", params);
    }

    public static int availableCameras() {
        if (cameraInfo.isEmpty()) {
            DSFilterInfo[][] filterInfo = DSCapture.queryDevices();
            for (DSFilterInfo capInfo : filterInfo[0]) {
                if (!"none".equals(capInfo.getCLSID())) {
                    cameraInfo.add(capInfo);
                }
            }
        }
        return cameraInfo.size();
    }

    private Camera(Integer cameraIndex) {
        this.cameraIndex = cameraIndex;
        deviceInfo = cameraInfo.get(cameraIndex);
        System.out.printf("Attempting to start %s\n", deviceInfo.toString());
        capture = new DSCapture(DSFiltergraph.JAVA_POLL | DSFiltergraph.FRAME_CALLBACK, deviceInfo, false, DSFilterInfo.doNotRender(), this);
    }

    public Set<Integer> supportedDialogs() {
        Set<Integer> dialogs = new HashSet<>();
        int supported = capture.getActiveVideoDevice().getSupportedDialogs();
        int check = 1;
        for (int i = 0; i < 10; i++) {
            if ((supported & check) == check) {
                dialogs.add(check);
            }
            check *= 2;
        }
        return dialogs;
    }

    public String getDialogName(int dialogId) {
        switch (dialogId) {
            case 1:
                return "VFW Source";
            case 2:
                return "VFW Format";
            case 8:
                return "WDM Device";
            case 16:
                return "WDM Capture";
            case 32:
                return "WDM Preview";
            case 64:
                return "Crossbar 1";
            case 128:
                return "Crossbar 2";
            case 256:
                return "TV Video";
            case 512:
                return "TV Audio";
        }
        return "Unknown";
    }

    public void showConfig(int dialogId) {
        DSCapture.CaptureDevice device = capture.getActiveVideoDevice();
        captureStats.report("Image capture");
        device.showDialog(dialogId);
        captureStats.reset();
    }

    public void close() {
        captureStats.report("Image capture");
        capture.dispose();
        cameras.remove(cameraIndex);
    }

    @Override
    public BufferedImage get() {
        return currentImage;
    }

    @Override
    public double getFps() {
        return captureStats.getRate();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (Integer.parseInt(evt.getNewValue().toString())) {
            case DSFiltergraph.GRAPH_EVENT:
            case DSFiltergraph.KF_NOTIFY:
            case DSFiltergraph.FRAME_NOTIFY:
                long start = System.nanoTime();
                this.currentImage = capture.getImage();
                captureStats.addValue(System.nanoTime() - start);
                break;
            default:
                System.out.println("Got event for value " + evt.getNewValue().toString());
        }
    }

    public static void closeAll() {
        Set<Camera> cleanSet = new HashSet<>(cameras.values());
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
