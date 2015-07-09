package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.filters.Slideshow;
import com.ideastormsoftware.presmedia.util.DisplayFile;
import java.io.File;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SlideshowEditor extends javax.swing.JFrame {

    private final DefaultListModel<File> fileListModel = new DefaultListModel<>();
    private final Slideshow slideshow;
    private final Runnable callback;

    public SlideshowEditor(Slideshow slideshow, Runnable callback) {
        this.slideshow = slideshow;
        initComponents();
        fileList.setModel(fileListModel);
        titleField.setText(slideshow.getTitle());
        randomize.setSelected(slideshow.isRandomize());
        slideDelayEditor.setValue(slideshow.getSlideDelay()*0.001);
        for (File file : slideshow.getFiles()) {
            fileListModel.addElement(new DisplayFile(file.getAbsolutePath()));
        }
        this.callback = callback;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        titleField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fileList = new javax.swing.JList();
        cancelButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        addFiles = new javax.swing.JButton();
        remove = new javax.swing.JButton();
        randomize = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        slideDelayEditor = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Slideshow Editor");

        jLabel1.setText("Title");

        jLabel2.setText("Image Files");

        fileList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(fileList);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        addFiles.setText("Add Files...");
        addFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFilesActionPerformed(evt);
            }
        });

        remove.setText("Remove");
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });

        randomize.setText("Display images in random order");

        jLabel3.setText("Slide display duration in seconds");

        slideDelayEditor.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(5.0d), Double.valueOf(0.001d), null, Double.valueOf(0.1d)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                            .addComponent(jLabel1)
                            .addComponent(titleField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(addFiles)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(remove))
                    .addComponent(randomize, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(slideDelayEditor)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addFiles)
                    .addComponent(remove))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(randomize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(slideDelayEditor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(saveButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFilesActionPerformed
        JFileChooser openChooser = new JFileChooser();
        openChooser.setAcceptAllFileFilterUsed(false);
        openChooser.addChoosableFileFilter(new FileNameExtensionFilter("Supported Images", "png","jpg","jpeg"));
        openChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG Images (*.png)", "png"));
        openChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPG Images (*.jpg, *.jpeg)", "jpg", "jpeg"));
        openChooser.setMultiSelectionEnabled(true);
        if (openChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            for (File selectedFile : openChooser.getSelectedFiles()) {
                fileListModel.addElement(new DisplayFile(selectedFile.getAbsolutePath()));
            }
        }
    }//GEN-LAST:event_addFilesActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        List<File> selectedFiles = fileList.getSelectedValuesList();
        for (File file : selectedFiles) {
            fileListModel.removeElement(file);
        }
    }//GEN-LAST:event_removeActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        slideshow.setTitle(titleField.getText());
        slideshow.setFiles(fileListModel.elements());
        slideshow.setRandomize(randomize.isSelected());
        slideshow.setSlideDelay((int) (1000*(Double)slideDelayEditor.getValue()));
        setVisible(false);
        if (callback != null) {
            callback.run();
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFiles;
    private javax.swing.JButton cancelButton;
    private javax.swing.JList fileList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox randomize;
    private javax.swing.JButton remove;
    private javax.swing.JButton saveButton;
    private javax.swing.JSpinner slideDelayEditor;
    private javax.swing.JTextField titleField;
    // End of variables declaration//GEN-END:variables
}
