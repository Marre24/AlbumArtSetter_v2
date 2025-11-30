package com.maxi.albumartsetter_v2;

import javafx.application.Application;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Launcher {
    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.SEVERE);
        Application.launch(AlbumArtSetterApplication.class, args);
    }
}
