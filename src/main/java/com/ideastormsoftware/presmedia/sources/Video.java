package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.ConfigurationContext;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Phillip
 */
public class Video implements ImageSource {

    private File sourceFile;

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public BufferedImage getCurrentImage() {
        if (grabber != null) {
            try {
                return AWTUtil.toBufferedImage(grabber.getNativeFrame());
            } catch (IOException ex) {
            }
        }
        return new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
    }

    @Override
    public JPanel getConfigurationPanel(ConfigurationContext context) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JTextField filenameField = new JTextField();
        JButton pickButton = new JButton("Select File...");
        panel.add(filenameField);
        panel.add(pickButton);
        pickButton.addActionListener((ActionEvent ae) -> {
            JFileChooser jfc = new JFileChooser();
            int result = jfc.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                filenameField.setText(jfc.getSelectedFile().getAbsolutePath());
                try {
                    setSourceFile(jfc.getSelectedFile());
                } catch (IOException | JCodecException ex) {
                    JOptionPane.showMessageDialog(null, "Failed to load file: " + ex.getMessage());
                }
            }
        });
        return panel;
    }

    @Override
    public boolean dependsOn(ImageSource source) {
        return false;
    }

    @Override
    public void replaceSource(ImageSource source, ImageSource replacement) {
    }

}
