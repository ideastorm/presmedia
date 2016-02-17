package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.util.ImageUtils;
import com.ideastormsoftware.presmedia.filters.Deinterlace;
import com.ideastormsoftware.presmedia.sources.ColorSource;
import com.ideastormsoftware.presmedia.sources.CrossFadeProxySource;
import com.ideastormsoftware.presmedia.filters.Lyrics;
import com.ideastormsoftware.presmedia.filters.Name;
import com.ideastormsoftware.presmedia.filters.Slideshow;
import com.ideastormsoftware.presmedia.sources.Camera;
import com.ideastormsoftware.presmedia.sources.ImageSource;
import com.ideastormsoftware.presmedia.sources.Media;
import com.ideastormsoftware.presmedia.sources.media.AvException;
import com.ideastormsoftware.presmedia.util.DisplayFile;
import com.ideastormsoftware.presmedia.util.RollingAverage;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.hyperic.sigar.Sigar;
import org.imgscalr.Scalr;

public class ControlView extends javax.swing.JFrame {
    
    static {
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
    private final DefaultListModel<File> mediaListModel;
    private final DefaultListModel<Slideshow> slideListModel;
    private final DefaultListModel<Lyrics> lyricsListModel;
    private final DefaultListModel<Name> nameListModel;
    private Lyrics selectedLyrics;
    private Lyrics activeLyrics;
    private Slideshow selectedSlides;
    private Media activeMedia = null;
    private Sigar sigar = new Sigar();
    private RollingAverage cpuAvg = new RollingAverage(5);
    private RollingAverage memAvg = new RollingAverage(5);
    private final ImageSource mediaSource = new ImageSource() {
        
        @Override
        public double getFps() {
            if (activeMedia != null) {
                return activeMedia.getFps();
            }
            return 0;
        }
        
        @Override
        public BufferedImage get() {
            if (activeMedia != null) {
                return activeMedia.get();
            }
            return ImageUtils.emptyImage();
        }
    };

    /**
     * Creates new form ControlView
     */
    public ControlView() {
        initComponents();
        source = new CrossFadeProxySource().setSourceNoFade(new ColorSource());
        projector = new Projector(source);
        projector.setFrameCallback((fps) -> {
            projectorFps.setText(String.format("Projector FPS: %01.1f", fps));
            crossfadeFps.setText(String.format("Crossfade FPS: %01.1f", source.getFps()));
            if (activeMedia != null) {
                double progress = activeMedia.getMediaPosition() * 1000.0 / activeMedia.getMediaDuration();
                mediaProgress.setValue((int) progress);
                audioBufferFill.setValue(activeMedia.getAudioBufferLoad());
                videoBufferFill.setValue(activeMedia.getVideoBufferLoad());
            } else {
                mediaProgress.setValue(0);
                audioBufferFill.setValue(0);
                videoBufferFill.setValue(0);
            }
            try {
                cpuLabel.setText(String.format("CPU: %1.1f%%", cpuAvg.addValue(sigar.getCpuPerc().getCombined() * 100)));
                cpuGraph.setValue((int) cpuAvg.get());
                memLabel.setText(String.format("MEM: %1.1f%%", memAvg.addValue(sigar.getMem().getUsedPercent())));
                memGraph.setValue((int) memAvg.get());
                
            } catch (Throwable ex) {
            }
        });
        projector.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                source.setTargetSize(projector.getRenderSize());
            }
        });
        RenderPane controlPreview = new RenderPane(source);
        outputContainer.add(controlPreview);
        RenderPane mediaPreview = new RenderPane(mediaSource);
        mediaPreviewContainer.add(mediaPreview);
        mediaPreview.setSize(201, 134);
        mediaListModel = new DefaultListModel<>();
        mediaList.setModel(mediaListModel);
        lyricsListModel = new DefaultListModel<>();
        songList.setModel(lyricsListModel);
        slideListModel = new DefaultListModel<>();
        slideList.setModel(slideListModel);
        nameListModel = new DefaultListModel<>();
        nameList.setModel(nameListModel);
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
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        mediaList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        addMedia = new javax.swing.JButton();
        removeMedia = new javax.swing.JButton();
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
        jScrollPane5 = new javax.swing.JScrollPane();
        slideList = new javax.swing.JList();
        jLabel7 = new javax.swing.JLabel();
        showSlides = new javax.swing.JToggleButton();
        addSlideshow = new javax.swing.JButton();
        editSlideshow = new javax.swing.JButton();
        removeSlideshow = new javax.swing.JButton();
        displayCamera = new javax.swing.JToggleButton();
        displayMedia = new javax.swing.JToggleButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        inputPreviews = new javax.swing.JPanel();
        saveSettings = new javax.swing.JButton();
        loadSettings = new javax.swing.JButton();
        loopMedia = new javax.swing.JCheckBox();
        deinterlaceCamera = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        crossfadeFps = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        scalingMethodPicker = new javax.swing.JComboBox();
        projectorFps = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        audioBufferFill = new javax.swing.JProgressBar();
        jLabel10 = new javax.swing.JLabel();
        videoBufferFill = new javax.swing.JProgressBar();
        cpuLabel = new javax.swing.JLabel();
        cpuGraph = new javax.swing.JProgressBar();
        memLabel = new javax.swing.JLabel();
        memGraph = new javax.swing.JProgressBar();
        jLabel2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        mediaPreviewContainer = new javax.swing.JPanel();
        mediaProgress = new javax.swing.JProgressBar();
        seekRev = new javax.swing.JButton();
        togglePause = new javax.swing.JButton();
        seekFwd = new javax.swing.JButton();

        setTitle("Presmedia Control");
        setMinimumSize(new java.awt.Dimension(924, 633));

        outputContainer.setLayout(new javax.swing.BoxLayout(outputContainer, javax.swing.BoxLayout.LINE_AXIS));

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        mediaList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        mediaList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                mediaListValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(mediaList);

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

        jLabel3.setText("Media Files");

        addMedia.setText("Add...");
        addMedia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMediaActionPerformed(evt);
            }
        });

        removeMedia.setText("Remove");
        removeMedia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMediaActionPerformed(evt);
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
        nameList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(nameList);

        addName.setText("Add...");
        addName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNameActionPerformed(evt);
            }
        });

        removeName.setText("Remove");
        removeName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeNameActionPerformed(evt);
            }
        });

        editName.setText("Edit...");
        editName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editNameActionPerformed(evt);
            }
        });

        displayName.setText("Show Selected Name");
        displayName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayNameActionPerformed(evt);
            }
        });

        jLabel6.setText("Lyrics");

        songList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        songList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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

        slideList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        slideList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        slideList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                slideListValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(slideList);

        jLabel7.setText("Slide shows");

        showSlides.setText("Play Slideshow");
        showSlides.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSlidesActionPerformed(evt);
            }
        });

        addSlideshow.setText("Add...");
        addSlideshow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSlideshowActionPerformed(evt);
            }
        });

        editSlideshow.setText("Edit...");
        editSlideshow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSlideshowActionPerformed(evt);
            }
        });

        removeSlideshow.setText("Remove");
        removeSlideshow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSlideshowActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeName))
                    .addComponent(displayName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addSong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editSong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeSong))
                    .addComponent(showSlides, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addSlideshow)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editSlideshow)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeSlideshow))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(showLyrics)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(advanceLyrics))
                            .addComponent(jLabel7))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addSong)
                    .addComponent(editSong)
                    .addComponent(removeSong))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showLyrics)
                    .addComponent(advanceLyrics))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addSlideshow)
                    .addComponent(editSlideshow)
                    .addComponent(removeSlideshow))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showSlides)
                .addContainerGap())
        );

        displayCamera.setText("Display Selected Input");
        displayCamera.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayCameraActionPerformed(evt);
            }
        });

        displayMedia.setText("Play Selected Media Files");
        displayMedia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayMediaActionPerformed(evt);
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

        loopMedia.setText("Loop Selected Media Files");

        deinterlaceCamera.setText("Deinterlace Video");

        crossfadeFps.setText("Crossfade Proxy FPS");

        jLabel8.setText("Image Scaling Method");

        scalingMethodPicker.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SPEED", "BALANCED", "AUTOMATIC", "QUALITY", "ULTRA_QUALITY" }));
        scalingMethodPicker.setSelectedItem("BALANCED");
        scalingMethodPicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scalingMethodPickerActionPerformed(evt);
            }
        });

        projectorFps.setText("Projector FPS: 30.0");

        jLabel9.setText("Media Audio Buffer");

        jLabel10.setText("Media Video Buffer");

        cpuLabel.setText("CPU");

        memLabel.setText("Memory");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(scalingMethodPicker, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(projectorFps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(28, 28, 28)
                        .addComponent(audioBufferFill, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(crossfadeFps)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(memLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cpuLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(videoBufferFill, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cpuGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(memGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(scalingMethodPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(crossfadeFps)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(projectorFps)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(audioBufferFill, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(videoBufferFill, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cpuLabel))
                    .addComponent(cpuGraph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(memLabel)
                    .addComponent(memGraph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(156, Short.MAX_VALUE))
        );

        jLabel2.setText("Output Preview");

        mediaPreviewContainer.setPreferredSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout mediaPreviewContainerLayout = new javax.swing.GroupLayout(mediaPreviewContainer);
        mediaPreviewContainer.setLayout(mediaPreviewContainerLayout);
        mediaPreviewContainerLayout.setHorizontalGroup(
            mediaPreviewContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 201, Short.MAX_VALUE)
        );
        mediaPreviewContainerLayout.setVerticalGroup(
            mediaPreviewContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 134, Short.MAX_VALUE)
        );

        mediaProgress.setMaximum(1000);
        mediaProgress.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mediaProgressMouseClicked(evt);
            }
        });

        seekRev.setText("<<");
        seekRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seekRevActionPerformed(evt);
            }
        });

        togglePause.setText("| |");
        togglePause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togglePauseActionPerformed(evt);
            }
        });

        seekFwd.setText(">>");
        seekFwd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seekFwdActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(seekRev)
                .addGap(28, 28, 28)
                .addComponent(togglePause, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(28, 28, 28)
                .addComponent(seekFwd))
            .addComponent(mediaProgress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mediaPreviewContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(mediaPreviewContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mediaProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(seekRev, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(seekFwd)
                        .addComponent(togglePause))))
        );

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
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(displayCamera, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deinterlaceCamera))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(loopMedia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addMedia, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(removeMedia, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(displayMedia, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(outputContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveSettings)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loadSettings))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane3)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(8, 8, 8)
                                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(loopMedia)
                                    .addComponent(deinterlaceCamera))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(configureCameras)
                                    .addComponent(addMedia)
                                    .addComponent(removeMedia))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(displayCamera)
                                    .addComponent(displayMedia)))
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(outputContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(loadSettings)
                            .addComponent(saveSettings))))
                .addContainerGap())
        );

        setSize(new java.awt.Dimension(940, 670));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void configureCamerasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureCamerasActionPerformed
        if (displayCamera.isSelected()) {
            displayCamera.setSelected(false);
            if (!displayMedia.isSelected()) {
                updatePreview();
            }
        }
        final CameraPicker cameraPicker = new CameraPicker();
        cameraPicker.setVisible(true);
        Set<Integer> cameras = cameraPicker.getSelectedCameras();
        Camera.closeAllExcept(cameras);
        inputPreviews.removeAll();
        inputPreviews.repaint();
        int index = 0;
        for (Integer cameraIndex : cameras) {
            final Camera camera = Camera.getCamera(cameraIndex);
            final RenderPane preview = new RenderPane(ImageUtils.scaleSource(camera), camera::getFps);
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
                    if (fireUpdate && !displayMedia.isSelected()) {
                        updatePreview();
                    }
                }
            });
            preview.setSize(160, (int) (160 / camera.getAspectRatio()));
            preview.setLocation(0, 125 * index++);
            inputPreviews.add(preview);
        }
    }//GEN-LAST:event_configureCamerasActionPerformed

    private void displayCameraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayCameraActionPerformed
        if (!displayMedia.isSelected()) {
            updatePreview();
        }
    }//GEN-LAST:event_displayCameraActionPerformed

    private void addMediaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMediaActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        int closeAction = fc.showOpenDialog(this);
        if (closeAction == JFileChooser.APPROVE_OPTION) {
            for (File file : fc.getSelectedFiles()) {
                mediaListModel.addElement(new DisplayFile(file.getAbsolutePath()));
            }
        }
    }//GEN-LAST:event_addMediaActionPerformed

    private void removeMediaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMediaActionPerformed
        int[] indices = mediaList.getSelectedIndices();
        File[] elements = new File[indices.length];
        for (int i = 0; i < indices.length; i++) {
            elements[i] = mediaListModel.getElementAt(indices[i]);
        }
        for (File element : elements) {
            mediaListModel.removeElement(element);
        }
    }//GEN-LAST:event_removeMediaActionPerformed

    private void displayMediaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayMediaActionPerformed
        updatePreview();
    }//GEN-LAST:event_displayMediaActionPerformed

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
        advanceLyrics.setEnabled(showLyrics.isSelected());
        if (showLyrics.isSelected()) {
            activeLyrics = selectedLyrics;
            activeLyrics.reset();
        } else {
            activeLyrics = null;
        }
        source.setOverlay(activeLyrics);
    }//GEN-LAST:event_showLyricsActionPerformed

    private void mediaListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_mediaListValueChanged

    }//GEN-LAST:event_mediaListValueChanged

    private void saveSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSettingsActionPerformed
        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setFileFilter(new FileNameExtensionFilter("Presmedia settings (*.presmedia)", "presmedia"));
        int result = saveChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String selectedFile = saveChooser.getSelectedFile().getPath();
            if (!selectedFile.endsWith(".presmedia")) {
                selectedFile = selectedFile + ".presmedia";
            }
            saveSettingsToFile(new File(selectedFile));
        }
    }//GEN-LAST:event_saveSettingsActionPerformed

    private void loadSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSettingsActionPerformed
        JFileChooser openChooser = new JFileChooser();
        openChooser.setFileFilter(new FileNameExtensionFilter("Presmedia settings (*.presmedia)", "presmedia"));
        int result = openChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                loadSettingsFromFile(openChooser.getSelectedFile());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Failed to load file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_loadSettingsActionPerformed

    private void addSlideshowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSlideshowActionPerformed
        Slideshow newShow = new Slideshow();
        new SlideshowEditor(newShow, () -> {
            slideListModel.addElement(newShow);
        }).setVisible(true);
    }//GEN-LAST:event_addSlideshowActionPerformed

    private void editSlideshowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSlideshowActionPerformed
        if (slideList.getSelectedIndex() >= 0) {
            new SlideshowEditor(slideListModel.get(slideList.getSelectedIndex()), null).setVisible(true);
        }
    }//GEN-LAST:event_editSlideshowActionPerformed

    private void removeSlideshowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSlideshowActionPerformed
        if (slideList.getSelectedIndex() >= 0) {
            slideListModel.removeElementAt(slideList.getSelectedIndex());
        }
    }//GEN-LAST:event_removeSlideshowActionPerformed

    private void showSlidesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSlidesActionPerformed
        if (showSlides.isSelected()) {
            source.setOverlay(selectedSlides);
        } else {
            source.setOverlay(null);
        }

    }//GEN-LAST:event_showSlidesActionPerformed

    private void slideListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_slideListValueChanged
        if (slideList.getSelectedIndex() >= 0) {
            selectedSlides = slideListModel.getElementAt(slideList.getSelectedIndex());
        }
    }//GEN-LAST:event_slideListValueChanged

    private void addNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNameActionPerformed
        Name name = new Name();
        new NameEditor(name, () -> {
            nameListModel.addElement(name);
        }).setVisible(true);

    }//GEN-LAST:event_addNameActionPerformed

    private void editNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editNameActionPerformed
        if (nameList.getSelectedIndex() >= 0) {
            new NameEditor(nameListModel.get(nameList.getSelectedIndex()), null).setVisible(true);
        }
    }//GEN-LAST:event_editNameActionPerformed

    private void removeNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeNameActionPerformed
        if (nameList.getSelectedIndex() >= 0) {
            nameListModel.removeElementAt(nameList.getSelectedIndex());
        }
    }//GEN-LAST:event_removeNameActionPerformed

    private void displayNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayNameActionPerformed
        if (displayName.isSelected()) {
            if (nameList.getSelectedIndex() >= 0) {
                source.setOverlay(nameListModel.get(nameList.getSelectedIndex()));
                return;
            }
        }
        source.setOverlay(null);
    }//GEN-LAST:event_displayNameActionPerformed

    private void scalingMethodPickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scalingMethodPickerActionPerformed
        try {
            Scalr.Method scalingMethod = Scalr.Method.valueOf(scalingMethodPicker.getSelectedItem().toString());
            ImageUtils.setScalingMethod(scalingMethod);
        } catch (IllegalArgumentException e) {
            warn(scalingMethodPicker.getSelectedItem().toString() + " is not a valid scalr method", e);
        }
    }//GEN-LAST:event_scalingMethodPickerActionPerformed

    private void mediaProgressMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mediaProgressMouseClicked
        if (activeMedia != null) {
            try {
                double percent = evt.getX() * 1.0 / mediaProgress.getWidth();
                long target = (long) (activeMedia.getMediaDuration() * percent);
                activeMedia.seekTo(target);
            } catch (AvException ex) {
                warn("Unable to seek within media", ex);
            }
        }
    }//GEN-LAST:event_mediaProgressMouseClicked

    private void togglePauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togglePauseActionPerformed
        if (activeMedia != null) {
            activeMedia.togglePaused();
            if (activeMedia.isPaused()) {
                togglePause.setText(">");
            } else {
                togglePause.setText("| |");
            }
        }
    }//GEN-LAST:event_togglePauseActionPerformed

    private void seekRevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seekRevActionPerformed
        if (activeMedia != null) {
            try {
                long current = activeMedia.getMediaPosition();
                long target = current - TimeUnit.SECONDS.toMicros(5);
                if (target < 0) {
                    target = 0;
                }
                activeMedia.seekTo(target);
            } catch (AvException ex) {
                warn("Unable to seek within media", ex);
            }
        }
    }//GEN-LAST:event_seekRevActionPerformed

    private void seekFwdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seekFwdActionPerformed
        if (activeMedia != null) {
            try {
                long current = activeMedia.getMediaPosition();
                long target = current + TimeUnit.SECONDS.toMicros(5);
                if (target > activeMedia.getMediaDuration()) {
                    target = activeMedia.getMediaDuration();
                }
                activeMedia.seekTo(target);
            } catch (AvException ex) {
                warn("Unable to seek within media", ex);
            }
        }
    }//GEN-LAST:event_seekFwdActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
//        System.setProperty("sun.java2d.opengl", "true");
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
    private javax.swing.JButton addMedia;
    private javax.swing.JButton addName;
    private javax.swing.JButton addSlideshow;
    private javax.swing.JButton addSong;
    private javax.swing.JButton advanceLyrics;
    private javax.swing.JProgressBar audioBufferFill;
    private javax.swing.JButton configureCameras;
    private javax.swing.JProgressBar cpuGraph;
    private javax.swing.JLabel cpuLabel;
    private javax.swing.JLabel crossfadeFps;
    private javax.swing.JCheckBox deinterlaceCamera;
    private javax.swing.JToggleButton displayCamera;
    private javax.swing.JToggleButton displayMedia;
    private javax.swing.JToggleButton displayName;
    private javax.swing.JButton editName;
    private javax.swing.JButton editSlideshow;
    private javax.swing.JButton editSong;
    private javax.swing.JPanel inputPreviews;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JButton loadSettings;
    private javax.swing.JCheckBox loopMedia;
    private javax.swing.JList mediaList;
    private javax.swing.JPanel mediaPreviewContainer;
    private javax.swing.JProgressBar mediaProgress;
    private javax.swing.JProgressBar memGraph;
    private javax.swing.JLabel memLabel;
    private javax.swing.JList nameList;
    private javax.swing.JPanel outputContainer;
    private javax.swing.JLabel projectorFps;
    private javax.swing.JButton removeMedia;
    private javax.swing.JButton removeName;
    private javax.swing.JButton removeSlideshow;
    private javax.swing.JButton removeSong;
    private javax.swing.JButton saveSettings;
    private javax.swing.JComboBox scalingMethodPicker;
    private javax.swing.JButton seekFwd;
    private javax.swing.JButton seekRev;
    private javax.swing.JToggleButton showLyrics;
    private javax.swing.JToggleButton showSlides;
    private javax.swing.JList slideList;
    private javax.swing.JList songList;
    private javax.swing.JButton togglePause;
    private javax.swing.JProgressBar videoBufferFill;
    // End of variables declaration//GEN-END:variables

    private void updatePreview() {
        try {
            loopMedia.setEnabled(!displayMedia.isSelected());
            if (displayMedia.isSelected()) {
                List<File> selectedMedia = mediaList.getSelectedValuesList();
                Runnable callback = new Runnable() {
                    int mediaIndex = 0;
                    
                    @Override
                    public void run() {
                        try {
                            mediaIndex++;
                            if (mediaIndex >= selectedMedia.size()) {
                                if (loopMedia.isSelected()) {
                                    mediaIndex = 0;
                                } else {
                                    displayMedia.setSelected(false);
                                    updatePreview();
                                    return;
                                }
                            }
                            activeMedia = new Media(selectedMedia.get(mediaIndex).getAbsolutePath(), this);
                            source.setSource(activeMedia);
                        } catch (Exception ex) {
                            activeMedia = null;
                            ex.printStackTrace();
                            source.setSource(new ColorSource());
                        }
                    }
                };
                if (!selectedMedia.isEmpty()) {
                    activeMedia = new Media(selectedMedia.get(0).getAbsolutePath(), callback);
                    source.setSource(activeMedia);
                }
            } else {
                activeMedia = null;
                if (displayCamera.isSelected() && selectedCamera != null) {
                    if (deinterlaceCamera.isSelected()) {
                        source.setSource(new Deinterlace().setSource(selectedCamera));
                    } else {
                        source.setSource(selectedCamera);
                    }
                } else {
                    source.setSource(new ColorSource());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            source.setSource(new ColorSource());
        }
    }
    
    private void saveSettingsToFile(File selectedFile) {
        new Settings(mediaListModel, lyricsListModel, slideListModel, nameListModel).saveToFile(selectedFile);
    }
    
    private void loadSettingsFromFile(File selectedFile) throws IOException {
        new Settings(mediaListModel, lyricsListModel, slideListModel, nameListModel).loadFromFile(selectedFile);
    }
}
