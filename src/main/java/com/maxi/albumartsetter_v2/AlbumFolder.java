package com.maxi.albumartsetter_v2;

import java.io.File;

public class AlbumFolder {


    private final File albumDir;
    private String name = null;

    public AlbumFolder(File albumDir, String name) {
        this.albumDir = albumDir;
        this.name = name;
    }

    public AlbumFolder(File albumDir) {
        this.albumDir = albumDir;
    }

    public String getName() {
        if (name == null)
            return albumDir.getName();
        return name;
    }

    @Override
    public String toString() {
        return "Album: " + getName();
    }
}
