package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.util.ImageUtils;
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
import com.ideastormsoftware.presmedia.util.FrameCoordinator;
import com.ideastormsoftware.presmedia.util.RollingAverage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.hyperic.sigar.Mem;
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

    private Supplier<Optional<BufferedImage>> backgroundSource = new ColorSource(Color.black);
    private Color backgroundColor = Color.black;
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
    private RollingAverage heapAvg = new RollingAverage(5);
    private final ImageSource mediaSource = new ImageSource() {
        @Override
        public double getFps() {
            if (activeMedia != null) {
                return activeMedia.getFps();
            }
            return 0;
        }

        @Override
        public Optional<BufferedImage> get() {
            if (activeMedia != null) {
                return activeMedia.get();
            }
            return Optional.empty();
        }
    };

    /**
     * Creates new form ControlView
     */
    public ControlView() {
        initComponents();
        inputPreviews.setLayout(new SpringLayout());
        source = new CrossFadeProxySource().setSource(backgroundSource);
        projector = new Projector(source);
        projector.setFrameCallback((fps) -> {
            try {
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
                cpuLabel.setText(String.format("CPU: %1.1f%%", cpuAvg.addValue(sigar.getCpuPerc().getCombined() * 100)));
                cpuGraph.setValue((int) cpuAvg.get());
                Mem mem = sigar.getMem();
                memLabel.setText(String.format("SysMem: %1.1f%%", memAvg.addValue(mem.getActualUsed() * 100.0 / mem.getTotal())));
                memGraph.setValue((int) memAvg.get());
                Runtime runtime = Runtime.getRuntime();
                long used = runtime.totalMemory() - runtime.freeMemory();
                heapLabel.setText(String.format("Heap: %1.1f%%", heapAvg.addValue(used * 100.0 / runtime.maxMemory())));
                heapGraph.setValue((int) heapAvg.get());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        });
        RenderPane controlPreview = new RenderPane(source, source::getFps);
        outputContainer.add(controlPreview);
        RenderPane mediaPreview = new RenderPane(mediaSource);
        mediaPreviewContainer.add(mediaPreview);
        mediaListModel = new DefaultListModel<>();
        mediaList.setModel(mediaListModel);
        mediaList.setTransferHandler(new FileListHandler(mediaListModel));
        mediaList.setDropMode(DropMode.INSERT);
        mediaList.setDragEnabled(true);

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

        jSplitPane5 = new javax.swing.JSplitPane();
        jSplitPane4 = new javax.swing.JSplitPane();
        jPanel8 = new javax.swing.JPanel();
        displayMedia = new javax.swing.JToggleButton();
        addMedia = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        mediaList = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        mediaPreviewContainer = new javax.swing.JPanel();
        mediaProgress = new javax.swing.JProgressBar();
        loopMedia = new javax.swing.JCheckBox();
        removeMedia = new javax.swing.JButton();
        seekRev = new javax.swing.JButton();
        seekFwd = new javax.swing.JButton();
        togglePause = new javax.swing.JButton();
        jSplitPane3 = new javax.swing.JSplitPane();
        jPanel6 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        colorSelector = new javax.swing.JButton();
        imageBgSelector = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        configureCameras = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        inputPreviews = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        displayCamera = new javax.swing.JToggleButton();
        deinterlaceCamera = new javax.swing.JCheckBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
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
        heapLabel = new javax.swing.JLabel();
        heapGraph = new javax.swing.JProgressBar();
        outputContainer = new javax.swing.JPanel();
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

        setTitle("Presmedia Control");
        setMinimumSize(new java.awt.Dimension(924, 633));

        displayMedia.setText("Play Selected Media Files");
        displayMedia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayMediaActionPerformed(evt);
            }
        });

        addMedia.setText("Add...");
        addMedia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMediaActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        mediaList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        mediaList.setDragEnabled(true);
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane4)
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jLabel3.setText("Media Files");

        mediaPreviewContainer.setBackground(new java.awt.Color(0, 0, 0));
        mediaPreviewContainer.setPreferredSize(new java.awt.Dimension(0, 0));
        mediaPreviewContainer.setLayout(new java.awt.BorderLayout());

        mediaProgress.setMaximum(1000);
        mediaProgress.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mediaProgressMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mediaProgress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mediaPreviewContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(mediaPreviewContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mediaProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        loopMedia.setText("Loop Selected Media Files");

        removeMedia.setText("Remove");
        removeMedia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMediaActionPerformed(evt);
            }
        });

        seekRev.setText("<<");
        seekRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seekRevActionPerformed(evt);
            }
        });

        seekFwd.setText(">>");
        seekFwd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seekFwdActionPerformed(evt);
            }
        });

        togglePause.setText("| |");
        togglePause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togglePauseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(seekRev)
                        .addGap(27, 27, 27)
                        .addComponent(togglePause, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(29, 29, 29)
                        .addComponent(seekFwd))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(addMedia, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeMedia, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(displayMedia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loopMedia, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(seekRev)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(seekFwd)
                        .addComponent(togglePause)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loopMedia)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addMedia)
                    .addComponent(removeMedia))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayMedia)
                .addContainerGap())
        );

        jSplitPane4.setRightComponent(jPanel8);

        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jLabel11.setText("Background");

        colorSelector.setText("Select Color");
        colorSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorSelectorActionPerformed(evt);
            }
        });

        imageBgSelector.setText("Select Image");
        imageBgSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageBgSelectorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(colorSelector)
                    .addComponent(imageBgSelector))
                .addGap(0, 63, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(colorSelector)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imageBgSelector))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane3.setLeftComponent(jPanel6);

        configureCameras.setText("Configure...");
        configureCameras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureCamerasActionPerformed(evt);
            }
        });

        jScrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        inputPreviews.setLayout(null);
        jScrollPane3.setViewportView(inputPreviews);

        jLabel1.setText("Live Inputs");

        displayCamera.setText("Display Selected Input");
        displayCamera.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayCameraActionPerformed(evt);
            }
        });

        deinterlaceCamera.setText("Deinterlace Video");
        deinterlaceCamera.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deinterlaceCameraActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(configureCameras, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(displayCamera, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(deinterlaceCamera)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deinterlaceCamera)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configureCameras)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayCamera)
                .addContainerGap())
        );

        jSplitPane3.setRightComponent(jPanel7);

        jSplitPane4.setLeftComponent(jSplitPane3);

        jSplitPane5.setLeftComponent(jSplitPane4);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        crossfadeFps.setText("Crossfade Proxy FPS");

        jLabel8.setText("Image Scaling Method");

        scalingMethodPicker.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SPEED", "BALANCED", "AUTOMATIC", "QUALITY", "ULTRA_QUALITY" }));
        scalingMethodPicker.setSelectedItem("SPEED");
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

        heapLabel.setText("Heap");

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
                        .addComponent(scalingMethodPicker, 0, 159, Short.MAX_VALUE))
                    .addComponent(projectorFps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(28, 28, 28)
                        .addComponent(audioBufferFill, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(crossfadeFps)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(memLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cpuLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(29, 29, 29))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(heapLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(40, 40, 40)))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(heapGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                            .addComponent(videoBufferFill, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                            .addComponent(cpuGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                            .addComponent(memGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(heapLabel)
                    .addComponent(heapGraph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane2.setLeftComponent(jPanel3);

        outputContainer.setLayout(new javax.swing.BoxLayout(outputContainer, javax.swing.BoxLayout.LINE_AXIS));
        jSplitPane2.setRightComponent(outputContainer);

        jSplitPane1.setRightComponent(jSplitPane2);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        jLabel5.setText("Names");

        nameList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        nameList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        nameList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                nameListValueChanged(evt);
            }
        });
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
                        .addComponent(editName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(9, 9, 9)
                        .addComponent(removeName))
                    .addComponent(displayName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addSong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editSong, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(9, 9, 9)
                        .addComponent(removeSong))
                    .addComponent(showSlides, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addSlideshow)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editSlideshow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(9, 9, 9)
                        .addComponent(removeSlideshow))
                    .addComponent(jScrollPane5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(showLyrics)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(advanceLyrics))
                    .addComponent(jLabel7))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
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
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addSlideshow)
                    .addComponent(editSlideshow)
                    .addComponent(removeSlideshow))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showSlides)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jSplitPane5.setRightComponent(jSplitPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane5)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane5)
        );

        setSize(new java.awt.Dimension(972, 670));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private class PreviewHeightSpring extends Spring {

        private final Camera input;

        PreviewHeightSpring(Camera input) {
            this.input = input;
        }

        @Override
        public int getMinimumValue() {
            return getValue();
        }

        @Override
        public int getPreferredValue() {
            return getValue();
        }

        @Override
        public int getMaximumValue() {
            return getValue();
        }

        @Override
        public int getValue() {
            return (int) (inputPreviews.getWidth() / input.getAspectRatio());
        }

        @Override
        public void setValue(int value) {
        }
    }

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
        Component lastPreview = null;
        SpringLayout layout = (SpringLayout) inputPreviews.getLayout();
        String TOP = SpringLayout.NORTH;
        String BOTTOM = SpringLayout.SOUTH;
        String LEFT = SpringLayout.WEST;
        String RIGHT = SpringLayout.EAST;
        for (Integer cameraIndex : cameras) {
            final Camera camera = Camera.getCamera(cameraIndex);
            final RenderPane preview = new RenderPane(camera);
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
                    deinterlaceCamera.setSelected(camera.isDeinterlaced());
                }
            });
            //TODO Correct the size and positioning using a layout manager so it'll rsize and reposition automatically
            if (lastPreview == null) {
                layout.putConstraint(TOP, preview, 0, TOP, inputPreviews);
            } else {
                layout.putConstraint(BOTTOM, preview, 0, TOP, lastPreview);
            }
            inputPreviews.add(preview);
            layout.putConstraint(LEFT, preview, 0, LEFT, inputPreviews);
            layout.putConstraint(RIGHT, preview, 0, RIGHT, inputPreviews);
            layout.putConstraint(BOTTOM, preview, new PreviewHeightSpring(camera), TOP, preview);
            lastPreview = preview;
        }
        layout.layoutContainer(inputPreviews);
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
            source.removeOverlay(selectedLyrics);
            selectedLyrics = lyricsListModel.elementAt(songList.getSelectedIndex());
            if (showLyrics.isSelected()) {
                source.appendOverlay(selectedLyrics);
            }
        }
    }//GEN-LAST:event_songListValueChanged

    private void removeSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSongActionPerformed
        if (selectedLyrics != null) {
            source.removeOverlay(selectedLyrics);
            lyricsListModel.removeElement(selectedLyrics);
            if (selectedLyrics.equals(activeLyrics)) {
                activeLyrics = null;
            }
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
            selectedLyrics.reset();
            source.appendOverlay(selectedLyrics);
        } else {
            source.removeOverlay(activeLyrics);
        }
    }//GEN-LAST:event_showLyricsActionPerformed

    private void mediaListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_mediaListValueChanged

    }//GEN-LAST:event_mediaListValueChanged

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
            source.removeOverlay(selectedSlides);
            slideListModel.removeElementAt(slideList.getSelectedIndex());
        }
    }//GEN-LAST:event_removeSlideshowActionPerformed

    private void showSlidesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSlidesActionPerformed
        if (showSlides.isSelected()) {
            source.appendOverlay(selectedSlides);
        } else {
            source.removeOverlay(selectedSlides);
        }

    }//GEN-LAST:event_showSlidesActionPerformed

    private void slideListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_slideListValueChanged
        if (slideList.getSelectedIndex() >= 0) {
            source.removeOverlay(selectedSlides);
            selectedSlides = slideListModel.getElementAt(slideList.getSelectedIndex());
            if (showSlides.isSelected()) {
                source.appendOverlay(selectedSlides);
            }
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
            source.removeOverlay(nameListModel.elementAt(nameList.getSelectedIndex()));
            nameListModel.removeElementAt(nameList.getSelectedIndex());
        }
    }//GEN-LAST:event_removeNameActionPerformed

    private void displayNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayNameActionPerformed
        if (displayName.isSelected()) {
            if (nameList.getSelectedIndex() >= 0) {
                source.appendOverlay(nameListModel.get(nameList.getSelectedIndex()));
                return;
            }
        }
        source.removeOverlay(nameListModel.get(nameList.getSelectedIndex()));
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
            } catch (AvException | InterruptedException ex) {
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
            } catch (AvException | InterruptedException ex) {
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
            } catch (AvException | InterruptedException ex) {
                warn("Unable to seek within media", ex);
            }
        }
    }//GEN-LAST:event_seekFwdActionPerformed

    private void deinterlaceCameraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deinterlaceCameraActionPerformed
        if (selectedCamera != null) {
            selectedCamera.setDeinterlace(deinterlaceCamera.isSelected());
        }
    }//GEN-LAST:event_deinterlaceCameraActionPerformed

    private void nameListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_nameListValueChanged
        source.removeOverlay(nameListModel.elementAt(nameList.getSelectedIndex()));
        displayName.setSelected(false);
    }//GEN-LAST:event_nameListValueChanged

    private void colorSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorSelectorActionPerformed
        backgroundColor = JColorChooser.showDialog(rootPane, "Select a background color", backgroundColor);
        backgroundSource = new ColorSource(backgroundColor);
        updatePreview();
    }//GEN-LAST:event_colorSelectorActionPerformed

    private void imageBgSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageBgSelectorActionPerformed
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showDialog(rootPane, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage image = ImageIO.read(chooser.getSelectedFile());
                backgroundSource = () -> Optional.of(image);
                updatePreview();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_imageBgSelectorActionPerformed

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
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            e.printStackTrace();
        });

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
    private javax.swing.JButton colorSelector;
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
    private javax.swing.JProgressBar heapGraph;
    private javax.swing.JLabel heapLabel;
    private javax.swing.JButton imageBgSelector;
    private javax.swing.JPanel inputPreviews;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPane4;
    private javax.swing.JSplitPane jSplitPane5;
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
        FrameCoordinator.setActiveSource(null);
        try {
            loopMedia.setEnabled(!displayMedia.isSelected());
            if (displayMedia.isSelected()) {
                if (activeMedia == null) {
                    List<File> selectedMedia = mediaList.getSelectedValuesList();
                    Runnable callback = new Runnable() {
                        int mediaIndex = 0;

                        @Override
                        public void run() {
                            List<File> selectedMedia = mediaList.getSelectedValuesList();
                            try {
                                mediaIndex++;
                                if (mediaIndex >= selectedMedia.size()) {
                                    if (loopMedia.isSelected() && !selectedMedia.isEmpty()) {
                                        mediaIndex = 0;
                                    } else {
                                        displayMedia.setSelected(false);
                                        updatePreview();
                                        return;
                                    }
                                }
                                activeMedia = new Media(selectedMedia.get(mediaIndex).getAbsolutePath(), this);
                                FrameCoordinator.setActiveSource(activeMedia);
                                source.setSourceNoFade(activeMedia);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                activeMedia = null;
                                ex.printStackTrace();
                                source.setSource(backgroundSource);
                            }
                        }
                    };
                    if (!selectedMedia.isEmpty()) {
                        activeMedia = new Media(selectedMedia.get(0).getAbsolutePath(), callback);
                        FrameCoordinator.setActiveSource(activeMedia);
                        source.setSource(activeMedia);
                    }
                }
            } else {
                activeMedia = null;
                if (displayCamera.isSelected() && selectedCamera != null) {
                    FrameCoordinator.setActiveSource(selectedCamera);
                    source.setSource(selectedCamera);
                } else {
                    source.setSource(backgroundSource);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            source.setSource(backgroundSource);
        }
    }
}
