package com.ideastormsoftware.presmedia.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.ideastormsoftware.presmedia.filters.Lyrics;
import com.ideastormsoftware.presmedia.filters.Name;
import com.ideastormsoftware.presmedia.filters.Slideshow;
import com.ideastormsoftware.presmedia.util.DisplayFile;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;

class Settings {

    private static final Gson gson = new Gson();
    private static final JsonParser parser = new JsonParser();
    private static final Type stringList = new TypeToken<List<String>>() {
    }.getType();
    private static final Type lyricList = new TypeToken<List<Lyrics>>() {
    }.getType();
    private static final Type slideList = new TypeToken<List<Slideshow>>() {
    }.getType();
    private static final Type nameList = new TypeToken<List<Name>>() {
    }.getType();

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

    private List<File> filesFromPaths(List<String> paths) {
        List<File> files = new ArrayList<>(paths.size());
        paths.stream().forEach((path) -> {
            files.add(new DisplayFile(path));
        });
        return files;
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

        String json = gson.toJson(settings);

        try (FileWriter writer = new FileWriter(selectedFile)) {
            writer.append(json);
        } catch (IOException ex) {
        }
    }

    void loadFromFile(File selectedFile) throws IOException {
        JsonObject root;
        try (FileReader reader = new FileReader(selectedFile)) {
            root = parser.parse(reader).getAsJsonObject();
        }
        List<String> videoPaths = gson.fromJson(root.get("videos"), stringList);
        List<Lyrics> lyrics = gson.fromJson(root.get("songs"), lyricList);
        List<Slideshow> slideshows = gson.fromJson(root.get("slides"), slideList);
        List<Name> names = gson.fromJson(root.get("names"), nameList);
        List<File> videos = filesFromPaths(videoPaths);

        populateModel(videoModel, videos);
        populateModel(lyricsModel, lyrics);
        populateModel(slidesModel, slideshows);
        populateModel(nameModel, names);
    }

    private <T> void populateModel(DefaultListModel<T> model, List<T> contents) {
        for (T content : contents) {
            model.addElement(content);
        }
    }

}
