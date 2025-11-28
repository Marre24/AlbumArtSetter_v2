package com.maxi.albumartsetter_v2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AlbumArtSetterApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneBuilder.startAlbumChooserScene(stage);
    }
}
