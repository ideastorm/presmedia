package com.ideastormsoftware.presmedia.ui;

import com.ideastormsoftware.presmedia.filters.Lyrics;
import com.ideastormsoftware.presmedia.filters.Name;
import com.ideastormsoftware.presmedia.filters.Slideshow;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;

class Settings {

    private final DefaultListModel<File> videoModel;
    private final DefaultListModel<Lyrics> lyricsModel;
    private final DefaultListModel<Slideshow> slidesModel;
    private final DefaultListModel<Name> nameModel;

    public Settings(
            DefaultListModel<File> videoListModel,
            DefaultListModel<Lyrics> lyricsListModel,
            DefaultListModel<Slideshow> slides,
            DefaultListModel<Name> names) {
        videoModel = videoListModel;
        lyricsModel = lyricsListModel;
        slidesModel = slides;
        nameModel = names;
    }

    private <T> List<T> enumerationToList(Enumeration<T> enumeration) {
        List<T> list = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    private List<String> filePaths(List<File> files) {
        List<String> paths = new ArrayList<>(files.size());
        for (File file : files) {
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    void saveToFile(File selectedFile) {
        List<String> videos = filePaths(enumerationToList(videoModel.elements()));
        List<Lyrics> songs = enumerationToList(lyricsModel.elements());
        List<Slideshow> slides = enumerationToList(slidesModel.elements());
        List<Name> names = enumerationToList(nameModel.elements());
        Map<String, List> settings = new HashMap<>(3);
        settings.put("videos", videos);
        settings.put("songs", songs);
        settings.put("slides", slides);
        settings.put("names", names);

//        saveMapAsJson(settings, selectedFile);
    }

    void loadFromFile(File selectedFile) {
    }
}
