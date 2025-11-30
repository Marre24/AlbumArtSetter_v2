package com.maxi.albumartsetter_v2;

import java.io.File;

public class AlbumFolder {


    private final File albumDir;
    private final String name;
    private final String artist;

    public AlbumFolder(File albumDir, String name, String artist) {
        this.albumDir = albumDir;
        this.name = name;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public File getAlbumDir() {
        return albumDir;
    }

    @Override
    public String toString() {
        return "Album: " + getName() + " by " + artist;
    }
}
