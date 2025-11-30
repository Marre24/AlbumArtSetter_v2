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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AlbumArtManager {

    private static final Gson gson = new Gson();
    private static final String DOWNLOAD_PATH = "./Covers/";
    private static final String FORMAT = ".jpg";
    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private static final long MIN_REQUEST_INTERVAL_MS = 60000 / MAX_REQUESTS_PER_MINUTE; // ~3 seconds
    private static long lastRequestTime = 0;

    public static Map<AlbumFolder, File[]> getImgFilesFor(List<AlbumFolder> albums) {
        Map<AlbumFolder, File[]> result = new HashMap<>();

        for (var album : albums) {
            File[] files = new File[4];
            var albumPaths = searchAlbumArt(album.getName(), album.getArtist());
            if (albumPaths == null || albumPaths.length == 0) {
                System.err.println("Could not get album covers for: " + album);
                continue;
            }
            // Sanitize album name for filename
            String safeAlbumName = album.getName().replaceAll("[\\\\/:*?\"<>|]", "_");
            for (int i = 0; i < Math.min(albumPaths.length, 4); i++) {
                files[i] = download(albumPaths[i], DOWNLOAD_PATH + safeAlbumName + i + FORMAT);
            }
            result.put(album, files);
        }
        return result;
    }

    public static String[] searchAlbumArt(String album, String artist) {
        HttpClient client = HttpClient.newHttpClient();

        try {
            // Rate limiting - ensure we don't exceed ~20 requests per minute
            rateLimit();

            // Search with both artist and album for better results
            String query = artist + " " + album;
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = "https://itunes.apple.com/search?term=" + encodedQuery +
                    "&entity=album&limit=200";

            System.out.println("Searching: " + query);

            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonObject root = gson.fromJson(response.body(), JsonObject.class);
            JsonArray results = root.getAsJsonArray("results");

            if (results == null || results.isEmpty()) {
                System.out.println("No results found, trying artist-only search...");
                return searchAlbumArtFallback(album, artist, client);
            }

            // Score and rank matches
            List<AlbumMatch> matches = new ArrayList<>();

            for (int i = 0; i < results.size(); i++) {
                JsonObject obj = results.get(i).getAsJsonObject();

                // Ensure it's an album/collection, not a track
                if (!obj.has("collectionName") || !obj.has("artworkUrl100")) {
                    continue;
                }

                String collectionName = obj.get("collectionName").getAsString();
                String artistName = obj.get("artistName").getAsString();
                String artworkUrl = obj.get("artworkUrl100").getAsString();

                int score = calculateMatchScore(album, artist, collectionName, artistName);

                if (score > 0) {
                    matches.add(new AlbumMatch(
                            collectionName,
                            artistName,
                            artworkUrl.replace("100x100bb", "600x600bb"),
                            score
                    ));
                }
            }

            if (matches.isEmpty()) {
                System.out.println("No matching albums found, trying fallback...");
                return searchAlbumArtFallback(album, artist, client);
            }

            // Sort by score (highest first) and take top 4
            matches.sort((a, b) -> Integer.compare(b.score, a.score));

            System.out.println("Found " + matches.size() + " matches:");
            for (int i = 0; i < Math.min(4, matches.size()); i++) {
                AlbumMatch match = matches.get(i);
                System.out.println("  " + (i+1) + ". " + match.album + " by " + match.artist + " (score: " + match.score + ")");
            }

            return matches.stream()
                    .limit(4)
                    .map(m -> m.artworkUrl)
                    .toArray(String[]::new);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String[] searchAlbumArtFallback(String album, String artist, HttpClient client) {
        try {
            // Rate limiting
            rateLimit();

            // Try searching just the artist to get all their albums
            String encodedArtist = URLEncoder.encode(artist, "UTF-8");
            String url = "https://itunes.apple.com/search?term=" + encodedArtist +
                    "&entity=album&limit=200";

            System.out.println("Fallback search: " + artist);

            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonObject root = gson.fromJson(response.body(), JsonObject.class);
            JsonArray results = root.getAsJsonArray("results");

            if (results == null || results.isEmpty()) {
                return null;
            }

            List<AlbumMatch> matches = new ArrayList<>();

            for (int i = 0; i < results.size(); i++) {
                JsonObject obj = results.get(i).getAsJsonObject();

                if (!obj.has("collectionName") || !obj.has("artworkUrl100")) {
                    continue;
                }

                String collectionName = obj.get("collectionName").getAsString();
                String artistName = obj.get("artistName").getAsString();
                String artworkUrl = obj.get("artworkUrl100").getAsString();

                int score = calculateMatchScore(album, artist, collectionName, artistName);

                if (score > 30) { // Lower threshold for fallback
                    matches.add(new AlbumMatch(
                            collectionName,
                            artistName,
                            artworkUrl.replace("100x100bb", "600x600bb"),
                            score
                    ));
                }
            }

            if (matches.isEmpty()) {
                return null;
            }

            matches.sort((a, b) -> Integer.compare(b.score, a.score));

            System.out.println("Fallback found " + matches.size() + " matches:");
            for (int i = 0; i < Math.min(4, matches.size()); i++) {
                AlbumMatch match = matches.get(i);
                System.out.println("  " + (i+1) + ". " + match.album + " by " + match.artist + " (score: " + match.score + ")");
            }

            return matches.stream()
                    .limit(4)
                    .map(m -> m.artworkUrl)
                    .toArray(String[]::new);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void rateLimit() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
            long sleepTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest;
            try {
                System.out.println("Rate limiting: waiting " + sleepTime + "ms...");
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        lastRequestTime = System.currentTimeMillis();
    }

    private static int calculateMatchScore(String targetAlbum, String targetArtist,
                                           String resultAlbum, String resultArtist) {
        int score = 0;

        // Normalize for comparison
        String normTargetAlbum = normalize(targetAlbum);
        String normResultAlbum = normalize(resultAlbum);
        String normTargetArtist = normalize(targetArtist);
        String normResultArtist = normalize(resultArtist);

        // Exact album match (highest priority)
        if (normTargetAlbum.equals(normResultAlbum)) {
            score += 100;
        }
        // Album contains target or vice versa
        else if (normResultAlbum.contains(normTargetAlbum) || normTargetAlbum.contains(normResultAlbum)) {
            score += 50;
        }
        // Word-by-word album match
        else if (containsAllWords(normTargetAlbum, normResultAlbum)) {
            score += 40;
        }
        // Partial word matches
        else if (hasPartialWordMatch(normTargetAlbum, normResultAlbum)) {
            score += 20;
        }

        // Exact artist match
        if (normTargetArtist.equals(normResultArtist)) {
            score += 50;
        }
        // Artist contains target or vice versa
        else if (normResultArtist.contains(normTargetArtist) || normTargetArtist.contains(normResultArtist)) {
            score += 25;
        }
        // Word-by-word artist match
        else if (containsAllWords(normTargetArtist, normResultArtist)) {
            score += 20;
        }
        // Partial artist match (for collaborations, features, etc.)
        else if (hasPartialWordMatch(normTargetArtist, normResultArtist)) {
            score += 10;
        }

        // Bonus for featuring/collaboration matches
        String[] artistParts = normTargetArtist.split("\\s+");
        if (artistParts.length > 0 && normResultArtist.contains(artistParts[0])) {
            score += 10;
        }

        return score;
    }

    private static boolean hasPartialWordMatch(String target, String text) {
        String[] targetWords = target.split("\\s+");
        String[] textWords = text.split("\\s+");

        int matchCount = 0;
        for (String tWord : targetWords) {
            if (tWord.length() < 3) continue; // Skip very short words
            for (String textWord : textWords) {
                if (tWord.equals(textWord)) {
                    matchCount++;
                    break;
                }
            }
        }

        return matchCount > 0 && targetWords.length > 0;
    }

    private static boolean containsAllWords(String target, String text) {
        String[] targetWords = target.split("\\s+");
        String[] textWords = text.split("\\s+");

        Set<String> textWordSet = new HashSet<>(Arrays.asList(textWords));

        for (String word : targetWords) {
            if (!word.isEmpty() && !textWordSet.contains(word)) {
                return false;
            }
        }
        return targetWords.length > 0;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFKC);
        // Remove special characters but keep spaces
        normalized = normalized.replaceAll("[^\\p{Alnum}\\s]", " ");
        // Collapse multiple spaces and trim
        normalized = normalized.replaceAll("\\s+", " ").toLowerCase().trim();
        return normalized;
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

    private static class AlbumMatch {
        String album;
        String artist;
        String artworkUrl;
        int score;

        AlbumMatch(String album, String artist, String artworkUrl, int score) {
            this.album = album;
            this.artist = artist;
            this.artworkUrl = artworkUrl;
            this.score = score;
        }
    }
}