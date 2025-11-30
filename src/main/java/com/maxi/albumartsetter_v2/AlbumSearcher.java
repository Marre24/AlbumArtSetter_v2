package com.maxi.albumartsetter_v2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlbumSearcher {
    private static final String COVER_NAME = "Cover.jpg";
    public static final String NO_ART = "No Art";
    public static final String ART = "Art";

    public static Map<String, ObservableList<AlbumFolder>> SearchForAlbums(String text) {
        File rootDir = new File(text);

        if (!rootDir.exists() || !rootDir.isDirectory() || rootDir.listFiles() == null)
            return null;

        Map<String, ObservableList<AlbumFolder>> map = new HashMap<>();

        map.put(NO_ART, FXCollections.observableList(new ArrayList<>()));
        map.put(ART, FXCollections.observableList(new ArrayList<>()));

        for (var albumDir : rootDir.listFiles()){
            if (!albumDir.isDirectory())
                continue;
            map.get(haveArt(albumDir)).add(getAlbum(albumDir));
        }

        return map;
    }

    private static AlbumFolder getAlbum(File albumDir) {
        for (var file : albumDir.listFiles()){
            String[] result = getMp3AlbumName(file);
            if (result != null)
                return new AlbumFolder(albumDir, result[0], result[1]);
        }

        return null;
    }

    private static String[] getMp3AlbumName(File file) {
        try {
            var audioFile = AudioFileIO.read(file);
            MP3File mp3 = (MP3File) audioFile;
            Tag tag = mp3.getTag();
            if (tag == null || tag.getFirst(FieldKey.ALBUM).isBlank() || tag.getFirst(FieldKey.ALBUM_ARTIST).isBlank())
                return null;
            return new String[]{tag.getFirst(FieldKey.ALBUM), tag.getFirst(FieldKey.ALBUM_ARTIST)};
        } catch (CannotReadException e) {
            System.err.println("Could not read from file: " + file.getPath());
            return null;
        } catch (IOException e) {
            System.err.println("IOException thrown: " + e.getMessage());
            return null;
        } catch (TagException e) {
            System.err.println("Could not get tag: " + e.getMessage());
            return null;
        } catch (ReadOnlyFileException e) {
            System.err.println("File: " + file.getPath() + " was a readonly file" + e.getMessage());
            return null;
        } catch (InvalidAudioFrameException e) {
            System.err.println("InvalidAudioFrameException: " + e.getMessage());
            return null;
        }
    }

    private static String haveArt(File albumDir) {
        var list = albumDir.listFiles(file -> file.getName().equals(COVER_NAME));
        if (list == null || list.length == 0)
            return NO_ART;
        return ART;
    }
}
