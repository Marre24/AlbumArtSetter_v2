package com.maxi.albumartsetter_v2;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;

import java.util.Map;

public class AlbumChooserController {

    public TextField pathTa;
    public ListView<AlbumFolder> noArtAlbumListView;
    public ListView<AlbumFolder> albumsWithArtListView;

    public void initialize() {
        noArtAlbumListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        albumsWithArtListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void searchForAlbums(ActionEvent actionEvent) {
        Map<String, ObservableList<AlbumFolder>> folders = AlbumSearcher.SearchForAlbums(pathTa.getText());
        if (folders == null)
            return;
        noArtAlbumListView.setItems(folders.get(AlbumSearcher.NO_ART));
        albumsWithArtListView.setItems(folders.get(AlbumSearcher.ART));
    }

    public void setArt(ActionEvent actionEvent) {
        var albums = noArtAlbumListView.getSelectionModel().getSelectedItems();
        if (albums.isEmpty())
            albums = albumsWithArtListView.getSelectionModel().getSelectedItems();

        System.out.println(albums);
    }

    public void deSelectAll(ActionEvent actionEvent) {
        noArtAlbumListView.getSelectionModel().clearSelection();
        albumsWithArtListView.getSelectionModel().clearSelection();
    }
}
