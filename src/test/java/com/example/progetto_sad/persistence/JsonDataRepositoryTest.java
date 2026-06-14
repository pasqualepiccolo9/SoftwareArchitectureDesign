package com.example.progetto_sad.persistence;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Persistenza dati - test del salvataggio JSON.
class JsonDataRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void saveWritesTrackMetadataAndRelativePath() throws IOException {
        TrackLibrary library = new TrackLibrary();
        PlaylistManager playlistManager = new PlaylistManager();
        Track track = new Track("Song A", "Artist A", "Pop", 2020,
                "data/audio/song-a.mp3", 180);
        library.addTrack(track);
        Path dataFile = tempDir.resolve("library.json");

        new JsonDataRepository(dataFile).save(library, playlistManager);

        JsonObject root = readRoot(dataFile);
        JsonArray tracks = root.getAsJsonArray("tracks");
        assertEquals(1, tracks.size());
        JsonObject savedTrack = tracks.get(0).getAsJsonObject();
        assertEquals(1, savedTrack.get("id").getAsInt());
        assertEquals("Song A", savedTrack.get("title").getAsString());
        assertEquals("Artist A", savedTrack.get("author").getAsString());
        assertEquals("Pop", savedTrack.get("genre").getAsString());
        assertEquals(2020, savedTrack.get("year").getAsInt());
        assertEquals(180, savedTrack.get("duration").getAsInt());
        assertEquals("data/audio/song-a.mp3", savedTrack.get("filePath").getAsString());
    }

    @Test
    void saveWritesPlaylistAssociationsAsTrackIds() throws IOException {
        TrackLibrary library = new TrackLibrary();
        PlaylistManager playlistManager = new PlaylistManager();
        Track first = new Track("Song A", "Artist A", "Pop", 2020, "data/audio/a.mp3", 180);
        Track second = new Track("Song B", "Artist B", "Rock", 2021, "data/audio/b.mp3", 200);
        library.addTrack(first);
        library.addTrack(second);
        Playlist playlist = playlistManager.createPlaylist("Preferiti");
        playlist.addTrack(second);
        Path dataFile = tempDir.resolve("library.json");

        new JsonDataRepository(dataFile).save(library, playlistManager);

        JsonObject root = readRoot(dataFile);
        JsonArray playlists = root.getAsJsonArray("playlists");
        assertEquals(1, playlists.size());
        JsonObject savedPlaylist = playlists.get(0).getAsJsonObject();
        assertEquals("Preferiti", savedPlaylist.get("name").getAsString());
        JsonArray trackIds = savedPlaylist.getAsJsonArray("trackIds");
        assertEquals(1, trackIds.size());
        assertEquals(2, trackIds.get(0).getAsInt());
    }

    @Test
    void saveCreatesParentDirectoryWhenMissing() throws IOException {
        TrackLibrary library = new TrackLibrary();
        PlaylistManager playlistManager = new PlaylistManager();
        Path dataFile = tempDir.resolve("nested").resolve("library.json");

        new JsonDataRepository(dataFile).save(library, playlistManager);

        assertTrue(Files.exists(dataFile));
    }

    @Test
    void loadIntoRestoresSavedTracksAndPlaylistRelations() throws IOException {
        TrackLibrary sourceLibrary = new TrackLibrary();
        PlaylistManager sourceManager = new PlaylistManager();
        Track first = new Track("Song A", "Artist A", "Pop", 2020, "data/audio/a.mp3", 180);
        Track second = new Track("Song B", "Artist B", "Rock", 2021, "data/audio/b.mp3", 200);
        sourceLibrary.addTrack(first);
        sourceLibrary.addTrack(second);
        Playlist sourcePlaylist = sourceManager.createPlaylist("Preferiti");
        sourcePlaylist.addTrack(second);
        Path dataFile = tempDir.resolve("library.json");
        JsonDataRepository repository = new JsonDataRepository(dataFile);
        repository.save(sourceLibrary, sourceManager);

        TrackLibrary loadedLibrary = new TrackLibrary();
        PlaylistManager loadedManager = new PlaylistManager();
        boolean loaded = repository.loadInto(loadedLibrary, loadedManager);

        assertTrue(loaded);
        assertEquals(2, loadedLibrary.getTracks().size());
        Track loadedSecond = loadedLibrary.getTracks().get(1);
        assertEquals("Song B", loadedSecond.getTitle());
        assertEquals("Artist B", loadedSecond.getAuthor());
        assertEquals("Rock", loadedSecond.getGenre());
        assertEquals(2021, loadedSecond.getYear());
        assertEquals(200, loadedSecond.getDuration());
        assertEquals(Path.of("data/audio/b.mp3").toAbsolutePath().normalize().toString(),
                loadedSecond.getFilePath());

        assertEquals(1, loadedManager.getPlaylists().size());
        Playlist loadedPlaylist = loadedManager.getPlaylists().get(0);
        assertEquals("Preferiti", loadedPlaylist.getName());
        assertEquals(1, loadedPlaylist.getTracks().size());
        assertSame(loadedSecond, loadedPlaylist.getTracks().get(0));
        assertTrue(loadedSecond.getPlaylists().contains(loadedPlaylist));
    }

    @Test
    void loadIntoWithMissingFileReturnsFalseAndKeepsStructuresEmpty() throws IOException {
        TrackLibrary library = new TrackLibrary();
        PlaylistManager playlistManager = new PlaylistManager();
        Path missingFile = tempDir.resolve("missing-library.json");

        boolean loaded = new JsonDataRepository(missingFile).loadInto(library, playlistManager);

        assertFalse(loaded);
        assertTrue(library.getTracks().isEmpty());
        assertTrue(playlistManager.getPlaylists().isEmpty());
    }

    @Test
    void loadSafelyIntoWithCorruptedFileDoesNotCrashAndKeepsStructuresEmpty() throws IOException {
        TrackLibrary library = new TrackLibrary();
        PlaylistManager playlistManager = new PlaylistManager();
        Path corruptedFile = tempDir.resolve("library.json");
        Files.writeString(corruptedFile, "{ json non valido");
        JsonDataRepository repository = new JsonDataRepository(corruptedFile);

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(err));
        boolean loaded;
        try {
            loaded = assertDoesNotThrow(() -> repository.loadSafelyInto(library, playlistManager));
        } finally {
            System.setErr(originalErr);
        }

        assertFalse(loaded);
        assertTrue(err.toString().contains("Caricamento dati non riuscito"));
        assertTrue(library.getTracks().isEmpty());
        assertTrue(playlistManager.getPlaylists().isEmpty());
    }

    @Test
    void loadSafelyIntoWithInvalidDataDoesNotPartiallyPopulateModels() throws IOException {
        TrackLibrary library = new TrackLibrary();
        PlaylistManager playlistManager = new PlaylistManager();
        Path corruptedFile = tempDir.resolve("library.json");
        Files.writeString(corruptedFile, """
                {
                  "version": 1,
                  "tracks": [
                    {
                      "id": 1,
                      "title": "Song A",
                      "author": "Artist A",
                      "genre": "Pop",
                      "year": 2020,
                      "duration": 180,
                      "filePath": "data/audio/a.mp3"
                    }
                  ],
                  "playlists": [
                    { "name": "Preferiti", "trackIds": [1] },
                    { "name": "Preferiti", "trackIds": [1] }
                  ]
                }
                """);
        JsonDataRepository repository = new JsonDataRepository(corruptedFile);

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(err));
        boolean loaded;
        try {
            loaded = assertDoesNotThrow(() -> repository.loadSafelyInto(library, playlistManager));
        } finally {
            System.setErr(originalErr);
        }

        assertFalse(loaded);
        assertTrue(err.toString().contains("Caricamento dati non riuscito"));
        assertTrue(library.getTracks().isEmpty());
        assertTrue(playlistManager.getPlaylists().isEmpty());
    }

    private JsonObject readRoot(Path file) throws IOException {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }
}
