package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.filters.Lyrics;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.DefaultListModel;

class Settings {

    private final DefaultListModel<File> videos;
    private final DefaultListModel<Lyrics> lyrics;

    public Settings(DefaultListModel<File> videoListModel, DefaultListModel<Lyrics> lyricsListModel) {
        videos = videoListModel;
        lyrics = lyricsListModel;
    }

    void saveToFile(File selectedFile) {
        Enumeration<File> files = videos.elements();
        List<String> filePaths = new ArrayList<>(videos.size());
        while (files.hasMoreElements()) {
            filePaths.add(files.nextElement().getAbsolutePath());
        }
        Enumeration<Lyrics> songs = lyrics.elements();
        List<Lyrics> saveLyrics = new ArrayList<>(lyrics.size());
        while (songs.hasMoreElements()) {
            saveLyrics.add(songs.nextElement());
        }
        
    }

    void loadFromFile(File selectedFile) {
    }

}
