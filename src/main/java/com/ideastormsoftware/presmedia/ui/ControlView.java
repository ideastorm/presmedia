package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import com.ideastormsoftware.presmedia.filters.Lyrics;
import com.ideastormsoftware.presmedia.sources.Camera;
import com.ideastormsoftware.presmedia.sources.ColorSource;
import com.ideastormsoftware.presmedia.sources.CrossFadeProxySource;
import com.ideastormsoftware.presmedia.sources.Video;
import com.ideastormsoftware.presmedia.util.DisplayFile;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.opencv.core.Core;

public class ControlView extends javax.swing.JFrame {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            FFmpegFrameGrabber.tryLoad();
        } catch (FrameGrabber.Exception ex) {
            System.err.println("Error loading FFmpeg library - videos will not work.");
            ex.printStackTrace();
        }
    }

    private final CrossFadeProxySource source;
    private final Projector projector;
    private Camera selectedCamera;
    private RenderPane selectedLiveInput;
    private final DefaultListModel<File> videoListModel;
    private final DefaultListModel<Lyrics> lyricsListModel;
    private Lyrics selectedLyrics;
    private Lyrics activeLyrics;

    /**
     * Creates new form ControlView
     */
    public ControlView() {
        initComponents();
        source = new CrossFadeProxySource(new ColorSource());
        projector = new Projector(source);
        RenderPane renderPane = new RenderPane(source);
        outputContainer.add(renderPane);
        videoListModel = new DefaultListModel<>();
        videoList.setModel(videoListModel);
        lyricsListModel = new DefaultListModel<>();
        songList.setModel(lyricsListModel);

    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        projector.setVisible(visible);
        if (!visible) {
            Camera.closeAll();
            System.exit(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outputContainer = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        videoList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        addVideos = new javax.swing.JButton();
        removeVideos = new javax.swing.JButton();
        configureCameras = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        nameList = new javax.swing.JList();
        addName = new javax.swing.JButton();
        removeName = new javax.swing.JButton();
        editName = new javax.swing.JButton();
        displayName = new javax.swing.JToggleButton();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        songList = new javax.swing.JList();
        addSong = new javax.swing.JButton();
        editSong = new javax.swing.JButton();
        removeSong = new javax.swing.JButton();
        showLyrics = new javax.swing.JToggleButton();
        advanceLyrics = new javax.swing.JButton();
        displayCamera = new javax.swing.JToggleButton();
        displayVideo = new javax.swing.JToggleButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        inputPreviews = new javax.swing.JPanel();
        saveSettings = new javax.swing.JButton();
        loadSettings = new javax.swing.JButton();
        loopVideos = new javax.swing.JCheckBox();

        setTitle("Presmedia Control");

        outputContainer.setLayout(new javax.swing.BoxLayout(outputContainer, javax.swing.BoxLayout.LINE_AXIS));

        jLabel2.setText("Output Preview");

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        videoList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        videoList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                videoListValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(videoList);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4)
        );

        jLabel1.setText("Live Inputs");

        jLabel3.setText("Videos");

        addVideos.setText("Add...");
        addVideos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addVideosActionPerformed(evt);
            }
        });

        removeVideos.setText("Remove");
        removeVideos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeVideosActionPerformed(evt);
            }
        });

        configureCameras.setText("Configure...");
        configureCameras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureCamerasActionPerformed(evt);
            }
        });

        jLabel4.setText("Overlays");

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        jLabel5.setText("Names");

        nameList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(nameList);

        addName.setText("Add...");

        removeName.setText("Remove");

        editName.setText("Edit...");

        displayName.setText("Show Selected Name");

        jLabel6.setText("Lyrics");

        songList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        songList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                songListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(songList);

        addSong.setText("Add...");
        addSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSongActionPerformed(evt);
            }
        });

        editSong.setText("Edit...");
        editSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSongActionPerformed(evt);
            }
        });

        removeSong.setText("Remove");
        removeSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSongActionPerformed(evt);
            }
        });

        showLyrics.setText("Show Selected Song");
        showLyrics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLyricsActionPerformed(evt);
            }
        });

        advanceLyrics.setText("Next Line");
        advanceLyrics.setEnabled(false);
        advanceLyrics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advanceLyricsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeName))
                    .addComponent(displayName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addSong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editSong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeSong))
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(showLyrics)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(advanceLyrics)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addName)
                    .addComponent(removeName)
                    .addComponent(editName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addSong)
                    .addComponent(editSong)
                    .addComponent(removeSong))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showLyrics)
                    .addComponent(advanceLyrics))
                .addContainerGap())
        );

        displayCamera.setText("Display Selected Input");
        displayCamera.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayCameraActionPerformed(evt);
            }
        });

        displayVideo.setText("Play Selected Videos");
        displayVideo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayVideoActionPerformed(evt);
            }
        });

        jScrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        inputPreviews.setLayout(null);
        jScrollPane3.setViewportView(inputPreviews);

        saveSettings.setText("Save settings...");
        saveSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSettingsActionPerformed(evt);
            }
        });

        loadSettings.setText("Load Settings...");
        loadSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSettingsActionPerformed(evt);
            }
        });

        loopVideos.setText("Loop Selected Videos");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(configureCameras, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 18, Short.MAX_VALUE))
                        .addComponent(displayCamera, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(displayVideo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(loopVideos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addVideos, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(removeVideos, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(saveSettings)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(loadSettings))
                    .addComponent(outputContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(loopVideos)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(configureCameras)
                            .addComponent(addVideos)
                            .addComponent(removeVideos))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(displayCamera)
                            .addComponent(displayVideo)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(outputContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(loadSettings)
                            .addComponent(saveSettings))))
                .addContainerGap())
        );

        setBounds(0, 0, 883, 518);
    }// </editor-fold>//GEN-END:initComponents

    private void configureCamerasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureCamerasActionPerformed
        if (displayCamera.isSelected()) {
            displayCamera.setSelected(false);
            updatePreview();
        }
        final CameraPicker cameraPicker = new CameraPicker();
        cameraPicker.setVisible(true);
        Set<Integer> cameras = cameraPicker.getSelectedCameras();
        Camera.closeAll();
        inputPreviews.removeAll();
        inputPreviews.repaint();
        int index = 0;
        for (Integer cameraIndex : cameras) {
            final Camera camera = new Camera(cameraIndex);
            final RenderPane preview = new RenderPane(ImageUtils.scaleSource(camera));
            preview.setBorder(new LineBorder(Color.black, 2));
            preview.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent me) {
                    if (selectedLiveInput != null) {
                        selectedLiveInput.setBorder(new LineBorder(Color.black, 2));
                    }
                    boolean fireUpdate = selectedCamera != camera;
                    selectedCamera = camera;
                    selectedLiveInput = preview;
                    preview.setBorder(new LineBorder(Color.red, 2));
                    if (fireUpdate) {
                        updatePreview();
                    }
                }
            });
            preview.setSize(160, 120);
            preview.setLocation(0, 125 * index++);
            inputPreviews.add(preview);
        }
    }//GEN-LAST:event_configureCamerasActionPerformed

    private void displayCameraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayCameraActionPerformed
        updatePreview();
    }//GEN-LAST:event_displayCameraActionPerformed

    private void addVideosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addVideosActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        int closeAction = fc.showOpenDialog(this);
        if (closeAction == JFileChooser.APPROVE_OPTION) {
            for (File file : fc.getSelectedFiles()) {
                videoListModel.addElement(new DisplayFile(file.getAbsolutePath()));
            }
        }
    }//GEN-LAST:event_addVideosActionPerformed

    private void removeVideosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeVideosActionPerformed
        int[] indices = videoList.getSelectedIndices();
        File[] elements = new File[indices.length];
        for (int i = 0; i < indices.length; i++) {
            elements[i] = videoListModel.getElementAt(indices[i]);
        }
        for (File element : elements) {
            videoListModel.removeElement(element);
        }
    }//GEN-LAST:event_removeVideosActionPerformed

    private void displayVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayVideoActionPerformed
        updatePreview();
    }//GEN-LAST:event_displayVideoActionPerformed

    private void addSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSongActionPerformed
        Lyrics lyrics = new Lyrics();
        new LyricsEditor(lyrics, () -> {
            lyricsListModel.addElement(lyrics);
        }).setVisible(true);
    }//GEN-LAST:event_addSongActionPerformed

    private void editSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSongActionPerformed
        if (selectedLyrics != null) {
            new LyricsEditor(selectedLyrics, null).setVisible(true);
        }
    }//GEN-LAST:event_editSongActionPerformed

    private void songListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_songListValueChanged
        if (songList.getSelectedIndex() >= 0) {
            selectedLyrics = lyricsListModel.elementAt(songList.getSelectedIndex());
        }
    }//GEN-LAST:event_songListValueChanged

    private void removeSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSongActionPerformed
        if (selectedLyrics != null) {
            lyricsListModel.removeElement(selectedLyrics);
        }
        selectedLyrics = null;
    }//GEN-LAST:event_removeSongActionPerformed

    private void advanceLyricsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advanceLyricsActionPerformed
        if (activeLyrics != null) {
            activeLyrics.advance();
        }
    }//GEN-LAST:event_advanceLyricsActionPerformed

    private void showLyricsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLyricsActionPerformed
        if (showLyrics.isSelected()) {
            activeLyrics = selectedLyrics;
            activeLyrics.reset();
        } else {
            activeLyrics = null;
        }
        source.setOverlay(activeLyrics);
    }//GEN-LAST:event_showLyricsActionPerformed

    private void videoListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_videoListValueChanged

    }//GEN-LAST:event_videoListValueChanged

    private void saveSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSettingsActionPerformed
        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setFileFilter(new FileNameExtensionFilter("Presmedia settings (*.presmedia)", "presmedia"));
        int result = saveChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            saveSettingsToFile(saveChooser.getSelectedFile());
        }
    }//GEN-LAST:event_saveSettingsActionPerformed

    private void loadSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSettingsActionPerformed
        JFileChooser openChooser = new JFileChooser();
        openChooser.setFileFilter(new FileNameExtensionFilter("Presmedia settings (*.presmedia)", "presmedia"));
        int result = openChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            loadSettingsFromFile(openChooser.getSelectedFile());
        }
    }//GEN-LAST:event_loadSettingsActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ControlView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ControlView().setVisible(true);
            }
        });
    }

    private void warn(String message, Throwable exception) {
        JOptionPane.showMessageDialog(rootPane, message + exception.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addName;
    private javax.swing.JButton addSong;
    private javax.swing.JButton addVideos;
    private javax.swing.JButton advanceLyrics;
    private javax.swing.JButton configureCameras;
    private javax.swing.JToggleButton displayCamera;
    private javax.swing.JToggleButton displayName;
    private javax.swing.JToggleButton displayVideo;
    private javax.swing.JButton editName;
    private javax.swing.JButton editSong;
    private javax.swing.JPanel inputPreviews;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton loadSettings;
    private javax.swing.JCheckBox loopVideos;
    private javax.swing.JList nameList;
    private javax.swing.JPanel outputContainer;
    private javax.swing.JButton removeName;
    private javax.swing.JButton removeSong;
    private javax.swing.JButton removeVideos;
    private javax.swing.JButton saveSettings;
    private javax.swing.JToggleButton showLyrics;
    private javax.swing.JList songList;
    private javax.swing.JList videoList;
    // End of variables declaration//GEN-END:variables

    private void updatePreview() {
        try {
            loopVideos.setEnabled(!displayVideo.isSelected());
            advanceLyrics.setEnabled(showLyrics.isSelected());
            if (displayVideo.isSelected()) {
                List<File> selectedVideos = videoList.getSelectedValuesList();
                Runnable callback = new Runnable() {
                    int videoIndex = 0;

                    @Override
                    public void run() {
                        try {
                            videoIndex++;
                            if (videoIndex >= selectedVideos.size()) {
                                if (loopVideos.isSelected()) {
                                    videoIndex = 0;
                                } else {
                                    displayVideo.setSelected(false);
                                    updatePreview();
                                    return;
                                }
                            }
                            source.setDelegate(new Video(selectedVideos.get(videoIndex).getAbsolutePath(), this));
                        } catch (FrameGrabber.Exception ex) {
                            ex.printStackTrace();
                            source.setDelegate(new ColorSource());
                        }
                    }
                };
                if (!selectedVideos.isEmpty()) {
                    source.setDelegate(new Video(selectedVideos.get(0).getAbsolutePath(), callback));
                }
            } else if (displayCamera.isSelected() && selectedCamera != null) {
                source.setDelegate(selectedCamera);
            } else {
                source.setDelegate(new ColorSource());
            }
        } catch (Throwable e) {
            e.printStackTrace();
            source.setDelegate(new ColorSource());
        }
    }

    private void saveSettingsToFile(File selectedFile) {
        new Settings(videoListModel, lyricsListModel).saveToFile(selectedFile);
    }

    private void loadSettingsFromFile(File selectedFile) {
        new Settings(videoListModel, lyricsListModel).loadFromFile(selectedFile);
    }
}
