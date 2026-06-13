package com.example.progetto_sad.command;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US22-T - test dei ConcreteCommand che agiscono sulle playlist.
class PlaylistCommandTest {

    private PlaylistManager manager;
    private Track track1;
    private Track track2;

    @BeforeEach
    void setUp() {
        manager = new PlaylistManager();
        track1 = new Track("Song A", "Artist A", "Pop", 2020, "a.mp3", 180);
        track2 = new Track("Song B", "Artist B", "Rock", 2021, "b.mp3", 200);
    }

    @Test
    void createPlaylistExecuteAddsPlaylistToManager() {
        Command command = new CreatePlaylistCommand(manager, "Preferiti");

        command.execute();

        assertEquals(1, manager.getPlaylists().size());
        assertEquals("Preferiti", manager.getPlaylists().get(0).getName());
    }

    @Test
    void createPlaylistUnexecuteRemovesCreatedPlaylist() {
        Command command = new CreatePlaylistCommand(manager, "Preferiti");
        command.execute();

        command.unexecute();

        assertTrue(manager.getPlaylists().isEmpty());
    }

    @Test
    void createPlaylistUnexecuteIgnoresAlreadyRemovedPlaylist() {
        CreatePlaylistCommand command = new CreatePlaylistCommand(manager, "Preferiti");
        command.execute();
        Playlist created = manager.getPlaylists().get(0);
        manager.removePlaylist(created);

        assertDoesNotThrow(command::unexecute);

        assertTrue(manager.getPlaylists().isEmpty());
    }

    @Test
    void createPlaylistRejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new CreatePlaylistCommand(null, "Preferiti"));
        assertThrows(IllegalArgumentException.class, () -> new CreatePlaylistCommand(manager, null));
    }

    @Test
    void deletePlaylistExecuteRemovesPlaylistAndTrackReferences() {
        Playlist playlist = manager.createPlaylist("Preferiti");
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        Command command = new DeletePlaylistCommand(manager, playlist);
        command.execute();

        assertFalse(manager.getPlaylists().contains(playlist));
        assertFalse(track1.getPlaylists().contains(playlist));
        assertFalse(track2.getPlaylists().contains(playlist));
    }

    @Test
    void deletePlaylistUnexecuteRestoresPlaylistAtOriginalPosition() {
        Playlist first = manager.createPlaylist("Preferiti");
        Playlist second = manager.createPlaylist("Studio");
        Playlist third = manager.createPlaylist("Workout");
        second.addTrack(track1);
        second.addTrack(track2);
        Command command = new DeletePlaylistCommand(manager, second);
        command.execute();

        command.unexecute();

        assertEquals(List.of(first, second, third), manager.getPlaylists());
        assertTrue(track1.getPlaylists().contains(second));
        assertTrue(track2.getPlaylists().contains(second));
    }

    @Test
    void deletePlaylistUnexecuteIgnoresAlreadyRestoredPlaylist() {
        Playlist first = manager.createPlaylist("Preferiti");
        Playlist second = manager.createPlaylist("Studio");
        Playlist third = manager.createPlaylist("Workout");
        Command command = new DeletePlaylistCommand(manager, second);
        command.execute();
        manager.addPlaylistAt(1, second);

        assertDoesNotThrow(command::unexecute);

        assertEquals(List.of(first, second, third), manager.getPlaylists());
    }

    @Test
    void deletePlaylistRejectsNullArguments() {
        Playlist playlist = manager.createPlaylist("Preferiti");

        assertThrows(IllegalArgumentException.class, () -> new DeletePlaylistCommand(null, playlist));
        assertThrows(IllegalArgumentException.class, () -> new DeletePlaylistCommand(manager, null));
    }
}
