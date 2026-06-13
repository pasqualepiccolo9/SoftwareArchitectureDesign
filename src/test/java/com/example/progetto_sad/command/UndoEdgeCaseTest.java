package com.example.progetto_sad.command;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US22-T - casi limite dell'annullamento quando lo stato cambia prima dell'undo.
class UndoEdgeCaseTest {

    private TrackLibrary library;
    private PlaylistManager playlistManager;
    private Playlist playlist;
    private Track track1;
    private Track track2;
    private Track track3;

    @BeforeEach
    void setUp() {
        library = new TrackLibrary();
        playlistManager = new PlaylistManager();
        playlist = new Playlist("Preferiti");
        track1 = new Track("Song A", "Artist A", "Pop", 2020, "a.mp3", 180);
        track2 = new Track("Song B", "Artist B", "Rock", 2021, "b.mp3", 200);
        track3 = new Track("Song C", "Artist C", "Jazz", 2022, "c.mp3", 240);
    }

    @Test
    void undoRemoveTrackFromPlaylistRestoresAtTailWhenOriginalIndexNoLongerExists() {
        fillPlaylist();
        Command command = new RemoveTrackFromPlaylistCommand(playlist, track3);
        command.execute();
        playlist.removeTrack(track2);

        assertDoesNotThrow(command::unexecute);

        assertEquals(List.of(track1, track3), playlist.getTracks());
        assertTrue(track3.getPlaylists().contains(playlist));
        assertFalse(track2.getPlaylists().contains(playlist));
    }

    @Test
    void undoRemoveTrackFromLibraryRestoresAtTailWhenLibraryShrank() {
        fillLibrary();
        Command command = new RemoveTrackFromLibraryCommand(library, track3);
        command.execute();
        library.removeTrack(track2);

        assertDoesNotThrow(command::unexecute);

        assertEquals(List.of(track1, track3), library.getTracks());
        assertFalse(library.contains(track2));
    }

    @Test
    void undoRemoveTrackFromLibraryRestoresPlaylistAtTailWhenPlaylistShrank() {
        fillLibrary();
        fillPlaylist();
        Command command = new RemoveTrackFromLibraryCommand(library, track3);
        command.execute();
        playlist.removeTrack(track2);

        assertDoesNotThrow(command::unexecute);

        assertEquals(List.of(track1, track2, track3), library.getTracks());
        assertEquals(List.of(track1, track3), playlist.getTracks());
        assertTrue(track3.getPlaylists().contains(playlist));
        assertFalse(track2.getPlaylists().contains(playlist));
    }

    @Test
    void undoDeletePlaylistRestoresAtTailWhenManagerShrank() {
        Playlist first = playlistManager.createPlaylist("Preferiti");
        Playlist second = playlistManager.createPlaylist("Studio");
        Playlist third = playlistManager.createPlaylist("Workout");
        third.addTrack(track1);
        Command command = new DeletePlaylistCommand(playlistManager, third);
        command.execute();
        playlistManager.removePlaylist(second);

        assertDoesNotThrow(command::unexecute);

        assertEquals(List.of(first, third), playlistManager.getPlaylists());
        assertTrue(track1.getPlaylists().contains(third));
        assertFalse(playlistManager.getPlaylists().contains(second));
    }

    @Test
    void commandManagerUndoConsumesCommandEvenWhenConcreteUndoHasNothingToDo() {
        CommandManager manager = new CommandManager();
        manager.executeCommand(new AddTrackToPlaylistCommand(playlist, track1));
        playlist.removeTrack(track1);

        assertDoesNotThrow(manager::undo);

        assertTrue(playlist.getTracks().isEmpty());
        assertFalse(track1.getPlaylists().contains(playlist));
        assertFalse(manager.canUndo());
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
