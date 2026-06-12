package com.example.progetto_sad.command;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US22-T - test dei ConcreteCommand che agiscono sulle tracce.
class TrackCommandTest {

    private TrackLibrary library;
    private Playlist playlist;
    private Playlist secondPlaylist;
    private Track track1;
    private Track track2;
    private Track track3;

    @BeforeEach
    void setUp() {
        library = new TrackLibrary();
        playlist = new Playlist("Preferiti");
        secondPlaylist = new Playlist("Studio");
        track1 = new Track("Song A", "Artist A", "Pop", 2020, "a.mp3", 180);
        track2 = new Track("Song B", "Artist B", "Rock", 2021, "b.mp3", 200);
        track3 = new Track("Song C", "Artist C", "Jazz", 2022, "c.mp3", 240);
    }

    @Test
    void addTrackToLibraryExecuteAddsTrack() {
        Command command = new AddTrackToLibraryCommand(library, track1);

        command.execute();

        assertEquals(List.of(track1), library.getTracks());
    }

    @Test
    void addTrackToLibraryUnexecuteRemovesAddedTrack() {
        Command command = new AddTrackToLibraryCommand(library, track1);
        command.execute();

        command.unexecute();

        assertFalse(library.contains(track1));
        assertTrue(library.getTracks().isEmpty());
    }

    @Test
    void addTrackToLibraryRejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new AddTrackToLibraryCommand(null, track1));
        assertThrows(IllegalArgumentException.class, () -> new AddTrackToLibraryCommand(library, null));
    }

    @Test
    void removeTrackFromLibraryExecuteRemovesTrackAndPlaylistReferences() {
        fillLibrary();
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        secondPlaylist.addTrack(track2);

        Command command = new RemoveTrackFromLibraryCommand(library, track2);
        command.execute();

        assertEquals(List.of(track1, track3), library.getTracks());
        assertFalse(playlist.getTracks().contains(track2));
        assertFalse(secondPlaylist.getTracks().contains(track2));
        assertTrue(track2.getPlaylists().isEmpty());
    }

    @Test
    void removeTrackFromLibraryUnexecuteRestoresTrackAndPlaylistPositions() {
        fillLibrary();
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.addTrack(track3);
        secondPlaylist.addTrack(track2);
        Command command = new RemoveTrackFromLibraryCommand(library, track2);
        command.execute();

        command.unexecute();

        assertEquals(List.of(track1, track2, track3), library.getTracks());
        assertEquals(List.of(track1, track2, track3), playlist.getTracks());
        assertEquals(List.of(track2), secondPlaylist.getTracks());
        assertTrue(track2.getPlaylists().contains(playlist));
        assertTrue(track2.getPlaylists().contains(secondPlaylist));
    }

    @Test
    void removeTrackFromLibraryRejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new RemoveTrackFromLibraryCommand(null, track1));
        assertThrows(IllegalArgumentException.class, () -> new RemoveTrackFromLibraryCommand(library, null));
    }

    @Test
    void addTrackToPlaylistExecuteAddsTrackAndUpdatesTrackReference() {
        Command command = new AddTrackToPlaylistCommand(playlist, track1);

        command.execute();

        assertEquals(List.of(track1), playlist.getTracks());
        assertTrue(track1.getPlaylists().contains(playlist));
    }

    @Test
    void addTrackToPlaylistUnexecuteRemovesTrackAndUpdatesTrackReference() {
        Command command = new AddTrackToPlaylistCommand(playlist, track1);
        command.execute();

        command.unexecute();

        assertTrue(playlist.getTracks().isEmpty());
        assertFalse(track1.getPlaylists().contains(playlist));
    }

    @Test
    void addTrackToPlaylistUnexecuteIgnoresAlreadyRemovedTrack() {
        Command command = new AddTrackToPlaylistCommand(playlist, track1);
        command.execute();
        playlist.removeTrack(track1);

        assertDoesNotThrow(command::unexecute);

        assertTrue(playlist.getTracks().isEmpty());
        assertFalse(track1.getPlaylists().contains(playlist));
    }

    @Test
    void addTrackToPlaylistRejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new AddTrackToPlaylistCommand(null, track1));
        assertThrows(IllegalArgumentException.class, () -> new AddTrackToPlaylistCommand(playlist, null));
    }

    @Test
    void removeTrackFromPlaylistExecuteRemovesTrackAndUpdatesTrackReference() {
        fillPlaylist();

        Command command = new RemoveTrackFromPlaylistCommand(playlist, track2);
        command.execute();

        assertEquals(List.of(track1, track3), playlist.getTracks());
        assertFalse(track2.getPlaylists().contains(playlist));
    }

    @Test
    void removeTrackFromPlaylistUnexecuteRestoresTrackAtOriginalPosition() {
        fillPlaylist();
        Command command = new RemoveTrackFromPlaylistCommand(playlist, track2);
        command.execute();

        command.unexecute();

        assertEquals(List.of(track1, track2, track3), playlist.getTracks());
        assertTrue(track2.getPlaylists().contains(playlist));
    }

    @Test
    void removeTrackFromPlaylistUnexecuteIgnoresAlreadyRestoredTrack() {
        fillPlaylist();
        Command command = new RemoveTrackFromPlaylistCommand(playlist, track2);
        command.execute();
        playlist.addTrackAt(1, track2);

        assertDoesNotThrow(command::unexecute);

        assertEquals(List.of(track1, track2, track3), playlist.getTracks());
    }

    @Test
    void removeTrackFromPlaylistRejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new RemoveTrackFromPlaylistCommand(null, track1));
        assertThrows(IllegalArgumentException.class, () -> new RemoveTrackFromPlaylistCommand(playlist, null));
    }

    private void fillLibrary() {
        library.addTrack(track1);
        library.addTrack(track2);
        library.addTrack(track3);
    }

    private void fillPlaylist() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.addTrack(track3);
    }
}
