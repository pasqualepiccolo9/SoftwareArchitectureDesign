package com.example.progetto_sad.persistence;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Repository JSON per la persistenza dei dati applicativi.
 *
 * La classe e' separata da View e Model: riceve le strutture runtime gia'
 * costruite e si occupa solo della conversione su file.
 */
public class JsonDataRepository {

    public static final Path DEFAULT_DATA_FILE = Path.of("data", "library.json");

    private static final int FORMAT_VERSION = 1;

    private final Path dataFile;
    private final Path projectRoot;
    private final Gson gson;

    public JsonDataRepository() {
        this(DEFAULT_DATA_FILE);
    }

    public JsonDataRepository(Path dataFile) {
        if (dataFile == null) {
            throw new IllegalArgumentException("Il file dati non puo' essere null");
        }
        this.dataFile = dataFile;
        this.projectRoot = Path.of("").toAbsolutePath().normalize();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Salva su `library.json` le tracce presenti nella libreria.
     *
     * @param library libreria da serializzare
     * @throws IOException se il file non puo' essere scritto
     */
    public void saveLibrary(TrackLibrary library) throws IOException {
        if (library == null) {
            throw new IllegalArgumentException("La libreria non puo' essere null");
        }

        JsonObject root = new JsonObject();
        root.addProperty("version", FORMAT_VERSION);
        root.add("tracks", buildTracksArray(library.getTracks(), new IdentityHashMap<>()));
        root.add("playlists", new JsonArray());

        writeRoot(root);
    }

    /**
     * Salva su `library.json` libreria e playlist. Le playlist referenziano le
     * tracce tramite `trackIds`, senza duplicarne i metadati.
     *
     * @param library libreria da serializzare
     * @param playlistManager gestore delle playlist da serializzare
     * @throws IOException se il file non puo' essere scritto
     */
    public void save(TrackLibrary library, PlaylistManager playlistManager) throws IOException {
        if (library == null || playlistManager == null) {
            throw new IllegalArgumentException("Libreria e playlist manager non possono essere null");
        }

        Map<Track, Integer> trackIds = new IdentityHashMap<>();
        List<Track> tracks = collectPersistentTracks(library, playlistManager);

        JsonObject root = new JsonObject();
        root.addProperty("version", FORMAT_VERSION);
        root.add("tracks", buildTracksArray(tracks, trackIds));
        root.add("playlists", buildPlaylistsArray(playlistManager.getPlaylists(), trackIds));

        writeRoot(root);
    }

    /**
     * Carica i dati da `library.json` dentro strutture runtime gia' create.
     *
     * @param library libreria da popolare
     * @param playlistManager gestore playlist da popolare
     * @return {@code true} se il file esiste ed e' stato letto, {@code false} se manca
     * @throws IOException se il file esiste ma non puo' essere letto
     */
    public boolean loadInto(TrackLibrary library, PlaylistManager playlistManager) throws IOException {
        if (library == null || playlistManager == null) {
            throw new IllegalArgumentException("Libreria e playlist manager non possono essere null");
        }
        if (!Files.exists(dataFile)) {
            return false;
        }

        LibraryData data;
        try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            data = gson.fromJson(reader, LibraryData.class);
        }
        if (data == null) {
            return true;
        }

        List<Track> loadedTracks = new ArrayList<>();
        Map<Integer, Track> tracksById = new HashMap<>();
        if (data.tracks != null) {
            for (TrackData item : data.tracks) {
                if (item == null) {
                    continue;
                }
                Track track = new Track(item.title, item.author, item.genre, item.year,
                        toRuntimePath(item.filePath), Math.max(0, item.duration));
                loadedTracks.add(track);
                tracksById.put(item.id, track);
            }
        }

        List<PlaylistLoadData> loadedPlaylists = new ArrayList<>();
        Set<String> playlistNames = new HashSet<>();
        if (data.playlists != null) {
            for (PlaylistData item : data.playlists) {
                if (item == null || item.name == null || item.name.isBlank()) {
                    continue;
                }
                String normalizedName = item.name.trim().toLowerCase();
                if (!playlistNames.add(normalizedName)) {
                    throw new IllegalArgumentException("Playlist duplicata nel file dati: " + item.name);
                }
                List<Track> playlistTracks = new ArrayList<>();
                if (item.trackIds == null) {
                    loadedPlaylists.add(new PlaylistLoadData(item.name, playlistTracks));
                    continue;
                }
                for (Integer trackId : item.trackIds) {
                    Track track = tracksById.get(trackId);
                    if (track != null && !playlistTracks.contains(track)) {
                        playlistTracks.add(track);
                    }
                }
                loadedPlaylists.add(new PlaylistLoadData(item.name, playlistTracks));
            }
        }

        for (Track track : loadedTracks) {
            library.addTrack(track);
        }
        for (PlaylistLoadData playlistData : loadedPlaylists) {
            Playlist playlist = playlistManager.createPlaylist(playlistData.name);
            for (Track track : playlistData.tracks) {
                playlist.addTrack(track);
            }
        }
        return true;
    }

    /**
     * Variante robusta per l'avvio dell'applicazione: se il file manca, e'
     * corrotto o non leggibile, l'app puo' partire con strutture vuote.
     *
     * @param library libreria da popolare
     * @param playlistManager gestore playlist da popolare
     * @return {@code true} se i dati sono stati caricati, {@code false} altrimenti
     */
    public boolean loadSafelyInto(TrackLibrary library, PlaylistManager playlistManager) {
        if (library == null || playlistManager == null) {
            throw new IllegalArgumentException("Libreria e playlist manager non possono essere null");
        }
        try {
            return loadInto(library, playlistManager);
        } catch (IOException | RuntimeException e) {
            System.err.println("Caricamento dati non riuscito: " + e.getMessage());
            return false;
        }
    }

    private List<Track> collectPersistentTracks(TrackLibrary library, PlaylistManager playlistManager) {
        List<Track> tracks = new ArrayList<>();
        Map<Track, Boolean> seen = new IdentityHashMap<>();
        for (Track track : library.getTracks()) {
            addIfMissing(tracks, seen, track);
        }
        for (Playlist playlist : playlistManager.getPlaylists()) {
            for (Track track : playlist.getTracks()) {
                addIfMissing(tracks, seen, track);
            }
        }
        return tracks;
    }

    private void addIfMissing(List<Track> tracks, Map<Track, Boolean> seen, Track track) {
        if (track != null && !seen.containsKey(track)) {
            seen.put(track, Boolean.TRUE);
            tracks.add(track);
        }
    }

    private JsonArray buildTracksArray(List<Track> tracks, Map<Track, Integer> trackIds) {
        JsonArray array = new JsonArray();
        int nextId = 1;
        for (Track track : tracks) {
            trackIds.put(track, nextId);
            JsonObject item = new JsonObject();
            item.addProperty("id", nextId);
            item.addProperty("title", track.getTitle());
            item.addProperty("author", track.getAuthor());
            item.addProperty("genre", track.getGenre());
            item.addProperty("year", track.getYear());
            item.addProperty("duration", track.getDuration());
            item.addProperty("filePath", toStoredPath(track.getFilePath()));
            array.add(item);
            nextId++;
        }
        return array;
    }

    private JsonArray buildPlaylistsArray(List<Playlist> playlists, Map<Track, Integer> trackIds) {
        JsonArray array = new JsonArray();
        for (Playlist playlist : playlists) {
            JsonObject item = new JsonObject();
            item.addProperty("name", playlist.getName());

            JsonArray ids = new JsonArray();
            for (Track track : playlist.getTracks()) {
                Integer id = trackIds.get(track);
                if (id != null) {
                    ids.add(id);
                }
            }
            item.add("trackIds", ids);
            array.add(item);
        }
        return array;
    }

    private void writeRoot(JsonObject root) throws IOException {
        Path parent = dataFile.toAbsolutePath().normalize().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        }
    }

    private String toStoredPath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }
        try {
            Path path = Path.of(filePath);
            if (!path.isAbsolute()) {
                return normalizeSeparators(path.normalize().toString());
            }
            Path absolute = path.toAbsolutePath().normalize();
            if (absolute.startsWith(projectRoot)) {
                return normalizeSeparators(projectRoot.relativize(absolute).toString());
            }
            return absolute.toString();
        } catch (InvalidPathException e) {
            return filePath;
        }
    }

    private String normalizeSeparators(String path) {
        return path.replace('\\', '/');
    }

    private String toRuntimePath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        try {
            Path path = Path.of(storedPath);
            if (path.isAbsolute()) {
                return path.normalize().toString();
            }
            return projectRoot.resolve(path).normalize().toString();
        } catch (InvalidPathException e) {
            return storedPath;
        }
    }

    private static final class LibraryData {
        private List<TrackData> tracks;
        private List<PlaylistData> playlists;
    }

    private static final class TrackData {
        private int id;
        private String title;
        private String author;
        private String genre;
        private int year;
        private int duration;
        private String filePath;
    }

    private static final class PlaylistData {
        private String name;
        private List<Integer> trackIds;
    }

    private static final class PlaylistLoadData {
        private final String name;
        private final List<Track> tracks;

        private PlaylistLoadData(String name, List<Track> tracks) {
            this.name = name;
            this.tracks = tracks;
        }
    }
}
