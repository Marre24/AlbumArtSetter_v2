package com.maxi.albumartsetter_v2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SceneBuilder {


    public static void startAlbumChooserScene(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(AlbumArtSetterApplication.class.getResource("album-chooser-view.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            System.err.println("Could not load album chooser scene: " + e.getMessage());
            return;
        }
        stage.setTitle("Choose which albums you want to add picture to!");
        stage.setScene(scene);
        stage.show();
    }

    public static void startArtSetter(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(AlbumArtSetterApplication.class.getResource("art-setter-view.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            System.err.println("Could not load album art setter scene: " + e.getMessage());
            return;
        }
        stage.setTitle("Choose matching art!");
        stage.setScene(scene);
        stage.show();
    }
}
