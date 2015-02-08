package com.ideastormsoftware.presmedia;

import com.ideastormsoftware.presmedia.forms.GraphBuilder;
import com.ideastormsoftware.presmedia.sources.Camera;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.opencv.core.Core;

public class Presenter extends JFrame {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            new Presenter().setVisible(true);
        });
    }

    public Presenter() throws HeadlessException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640,480);
        buildComponents();
    }

    private void buildComponents() {
        setLayout(new FlowLayout());
        JButton buildFilterButton  = new JButton("Build Filter Graph...");
        buildFilterButton.addActionListener((ActionEvent ae) -> {
            new GraphBuilder().setVisible(true);
        });
        add(buildFilterButton);
    }

}
