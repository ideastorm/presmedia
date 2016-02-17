/*
 * Copyright 2016 Clerk.
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
import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;

public class dsjSimple extends JFrame implements PropertyChangeListener {

    private Stats captureStats = new Stats();

    private dsjSimple(DSFilterInfo cameraInfo) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(640, 480);
        final DSCapture capture = new DSCapture(DSFiltergraph.OVERLAY | DSFiltergraph.FRAME_CALLBACK, cameraInfo, false, DSFilterInfo.doNotRender(), this);
        add(capture);
        capture.setLocation(0, 0);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                capture.dispose();
                captureStats.report("image capture");
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        DSEnvironment.unlockDLL("phil@pjhayward.net", 753608, 1702561, 0);
        EventQueue.invokeLater(() -> {
            DSFilterInfo[][] filterInfo = DSCapture.queryDevices();
            new dsjSimple(filterInfo[0][1]).setVisible(true);
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (Integer.parseInt(evt.getNewValue().toString())) {
            case DSFiltergraph.GRAPH_EVENT:
            case DSFiltergraph.KF_NOTIFY:
            case DSFiltergraph.FRAME_NOTIFY:
                captureStats.addValue(1);
                break;
            default:
                System.out.println("Got event for value " + evt.getNewValue().toString());
        }
    }

}
