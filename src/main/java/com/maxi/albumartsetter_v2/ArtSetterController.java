package com.maxi.albumartsetter_v2;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.Stack;

public class ArtSetterController {
    public ImageView slot1Image;
    public ImageView slot2Image;
    public ImageView slot3Image;
    public ImageView slot4Image;

    private Stack<File> imgPaths;

    File p1 = null;
    File p2 = null;
    File p3 = null;
    File p4 = null;

    public void initialize(){
        if (imgPaths == null || imgPaths.isEmpty())
            return;

        p1 = imgPaths.pop();
        if (imgPaths.isEmpty())
            return;
        p2 = imgPaths.pop();
        if (imgPaths.isEmpty())
            return;
        p3 = imgPaths.pop();
        if (imgPaths.isEmpty())
            return;
        p4 = imgPaths.pop();
    }


    public void fullReRoll(ActionEvent actionEvent) {

    }

    public void skip(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

        if (imgPaths == null || imgPaths.isEmpty())
            SceneBuilder.startAlbumChooserScene(stage);

    }

    public void reRoll(ActionEvent actionEvent) {
        if (imgPaths.isEmpty())
            return;

        Node source = (Node) actionEvent.getSource();
        int id = Integer.parseInt(source.getId());

        switch (id){
            case 1:
                p1 = imgPaths.pop();
                break;
            case 2:
                p2 = imgPaths.pop();
                break;
            case 3:
                p3 = imgPaths.pop();
                break;
            case 4:
                p4 = imgPaths.pop();
                break;
        }
    }

    public void select(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        String nr = source.getId().split("_")[0];
        int id = (int) Double.parseDouble(nr);

        switch (id){
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
        }
    }

    public void setImgPaths(Stack<File> imgPaths) {
        this.imgPaths = imgPaths;
    }
}
