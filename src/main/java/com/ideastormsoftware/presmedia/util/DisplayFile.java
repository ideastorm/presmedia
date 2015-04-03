package com.ideastormsoftware.presmedia.util;

import java.io.File;
import java.net.URI;

public class DisplayFile extends File{

    public DisplayFile(String pathname) {
        super(pathname);
    }

    public DisplayFile(String parent, String child) {
        super(parent, child);
    }

    public DisplayFile(File parent, String child) {
        super(parent, child);
    }

    public DisplayFile(URI uri) {
        super(uri);
    }

    @Override
    public String toString() {
        String displayPath = super.toString();
        int index = displayPath.lastIndexOf(File.separator);
        if (index < 0)
            return displayPath;
        return displayPath.substring(index+1);
    }
}
