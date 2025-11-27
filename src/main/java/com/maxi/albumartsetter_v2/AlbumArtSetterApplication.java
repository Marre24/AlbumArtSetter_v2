package com.maxi.albumartsetter_v2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AlbumArtSetterApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AlbumArtSetterApplication.class.getResource("album-chooser-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Choose Which Albums you want to add picture to!");
        stage.setScene(scene);
        stage.show();
    }
}
