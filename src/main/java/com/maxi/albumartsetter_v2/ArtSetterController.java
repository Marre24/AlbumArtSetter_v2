package com.maxi.albumartsetter_v2;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static com.maxi.albumartsetter_v2.GlobalVariables.imgFiles;

public class ArtSetterController {
    public ImageView slot1Image;
    public ImageView slot2Image;
    public ImageView slot3Image;
    public ImageView slot4Image;

    private AlbumFolder current;


    public void initialize() {
        next();
    }

    public void select(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        int id = Integer.parseInt(source.getId());
        var ip = imgFiles.get(current);

        try {
            for (int i = 0; i < ip.length; i++) {
                if (i == id){
                    File targetFile = new File(current.getAlbumDir(), "Cover.jpg");

                    Files.move(
                            ip[i].toPath(),
                            targetFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    continue;
                }
                if (ip[i] != null && !ip[i].delete())
                    System.err.println("Failed to delete temporary file: " + ip[i]);
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        next();
    }

    public void skip(ActionEvent actionEvent) {
        next();
    }

    private void next() {
        if (current != null)
            imgFiles.remove(current);

        if (imgFiles.isEmpty()){
            System.out.println("DONE!");
            Stage stage = (Stage) slot1Image.getScene().getWindow();
            SceneBuilder.startAlbumChooserScene(stage);
            return;
        }
        slot1Image.setImage(null);
        slot2Image.setImage(null);
        slot3Image.setImage(null);
        slot4Image.setImage(null);

        current = imgFiles.keySet().iterator().next();
        var ip = imgFiles.get(current);

        if (ip[0] == null)
            return;
        Image i1 = new Image(ip[0].toURI().toString());
        slot1Image.setImage(i1);

        if (ip[1] == null)
            return;
        Image i2 = new Image(ip[1].toURI().toString());
        slot2Image.setImage(i2);

        if (ip[2] == null)
            return;
        Image i3 = new Image(ip[2].toURI().toString());
        slot3Image.setImage(i3);

        if (ip[3] == null)
            return;
        Image i4 = new Image(ip[3].toURI().toString());
        slot4Image.setImage(i4);
    }
}
