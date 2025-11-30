package com.maxi.albumartsetter_v2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumArtManager {

    private static final Gson gson = new Gson();
    private static final String DOWNLOAD_PATH = "./Covers/";
    private static final String FORMAT = ".jpg";

    public static Map<AlbumFolder, File[]> getImgFilesFor(List<AlbumFolder> albums) {
        Map<AlbumFolder, File[]> result = new HashMap<>();

        for (var album : albums) {
            File[] files = new File[4];
            var albumPaths = searchAlbumArt(album.getName(), album.getArtist());
            if (albumPaths == null) {
                System.err.println("Could not get album covers for: " + album);
                continue;
            }
            for (int i = 0; i < albumPaths.length; i++) {
                files[i] = download(albumPaths[i], DOWNLOAD_PATH + album.getName() + i + FORMAT);
            }
            result.put(album, files);
        }
        return result;
    }

    public static String[] searchAlbumArt(String album, String artist) {
        HttpClient client = HttpClient.newHttpClient();

        try {
            String artistQuery = URLEncoder.encode(artist, "UTF-8");
            String url = "https://itunes.apple.com/search?term=" + artistQuery +
                    "&entity=album&country=us&limit=50";

            System.out.println("Sending query: " + url);

            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonObject root = gson.fromJson(response.body(), JsonObject.class);
            JsonArray results = root.getAsJsonArray("results");

            if (results == null || results.isEmpty())
                return null;

            String target = asciiOnly(album);

            boolean useExactMatch = target.length() <= 1;

            List<String> covers = new ArrayList<>();

            for (int i = 0; i < results.size() && covers.size() < 4; i++) {
                JsonObject obj = results.get(i).getAsJsonObject();
                String colName = obj.get("collectionName").getAsString();

                boolean matches = useExactMatch
                        ? colName.equals(album) || asciiOnly(colName).equals(album.toLowerCase().trim())
                        : asciiOnly(colName).equals(target);

                if (matches) {
                    String artwork100 = obj.get("artworkUrl100").getAsString();
                    covers.add(artwork100.replace("100x100bb", "600x600bb"));
                }
            }

            if (covers.isEmpty())
                return null;

            return covers.toArray(new String[0]);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File download(String url, String filename) {
        try {
            Files.createDirectories(Path.of(DOWNLOAD_PATH));

            try (InputStream in = new URI(url).toURL().openStream()) {
                String safeFilename = safeName(filename);
                Path dest = Path.of(safeFilename);
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                return dest.toFile();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String safeName(String name) {
        int lastSep = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSep >= 0) {
            String dir = name.substring(0, lastSep + 1);
            String file = name.substring(lastSep + 1);
            return dir + file.replaceAll("[\\\\/:*?\"<>|]", "_");
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static String asciiOnly(String s) {
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFKC);
        return normalized.replaceAll("[^\\p{Alnum}\\s]", "").toLowerCase().trim();
    }

}
