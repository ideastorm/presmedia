/*
 * Copyright 2017 philj.
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
package com.ideastormsoftware.presmedia.util;

import com.ideastormsoftware.presmedia.sources.Media;
import com.ideastormsoftware.presmedia.sources.ScaledSource;
import com.ideastormsoftware.presmedia.ui.Projector;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @author philj
 */
public class MoviePlayer implements Runnable {

    public static void main(String[] args) {
        List<String> movieFiles = Arrays.asList(args).stream().map(File::new).filter(File::exists).map(File::getPath).collect(Collectors.toList());
        if (!movieFiles.isEmpty()) {
            new MoviePlayer(movieFiles).run();
        } else {
            System.out.println("No media specified");
        }
    }
    private final List<String> movieFiles;
    private int index;
    private final ScaledSource source;
    private final Projector projector;

    private MoviePlayer(List<String> movieFiles) {
        this.movieFiles = movieFiles;
        this.source = new ScaledSource();
        this.projector = new Projector(source);

        projector.setVisible(true);
    }

    @Override
    public void run() {
        if (index >= movieFiles.size()) {
            return;
        }
        Media mediaSource = new Media(movieFiles.get(index++), this);
        source.setSource(mediaSource);
        mediaSource.start();
        new Thread(()->{
            try {
                TimeUnit.SECONDS.sleep(20);
                mediaSource.close();
                TimeUnit.SECONDS.sleep(1);
                System.exit(0);
            } catch (InterruptedException ex) {
            }
        }).start();
    }
}
