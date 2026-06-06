package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US10 - inizializzazione della sequenza dalla playlist avviata
class PlaylistSequenceControllerTest {

    private PlaylistSequenceController controller;
    private Playlist playlist;
    private Track track1;
    private Track track2;
    private Track track3;

    @BeforeEach
    void setUp() {
        controller = new PlaylistSequenceController();
        playlist = new Playlist("Test Playlist");
        track1 = new Track("Song A", "Artist A", 180, "Pop", 2020);
        track2 = new Track("Song B", "Artist B", 200, "Rock", 2021);
        track3 = new Track("Song C", "Artist C", 240, "Jazz", 2022);
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.addTrack(track3);
    }

    // US10 - avvio playlist: la sequenza creata non deve essere null
    @Test
    void startPlaylistCreatesNonNullSequence() {
        controller.startPlaylist(playlist);

        assertNotNull(controller.getSequence());
    }

    // US10 - avvio playlist: il primo brano della playlist diventa la traccia corrente
    @Test
    void startPlaylistSetsFirstTrackAsCurrent() {
        controller.startPlaylist(playlist);

        assertEquals(track1, controller.getCurrentTrack());
    }

    // US10 - avvio playlist: l'ordine dei brani nella sequenza coincide con quello della playlist
    @Test
    void startPlaylistMaintainsTrackOrder() {
        controller.startPlaylist(playlist);

        List<Track> tracks = controller.getSequence().getTracks();
        assertEquals(track1, tracks.get(0));
        assertEquals(track2, tracks.get(1));
        assertEquals(track3, tracks.get(2));
    }

    // US10 - avvio playlist: tutte le tracce della playlist sono presenti nella sequenza
    @Test
    void startPlaylistWithAllTracksInSequence() {
        controller.startPlaylist(playlist);

        assertEquals(3, controller.getSequence().getTracks().size());
    }

    // US10 - caso limite: playlist vuota produce una sequenza vuota e terminata senza crash
    @Test
    void startPlaylistWithEmptyPlaylistCreatesFinishedSequence() {
        Playlist empty = new Playlist("Empty");

        controller.startPlaylist(empty);

        assertNotNull(controller.getSequence());
        assertTrue(controller.getSequence().isEmpty());
        assertTrue(controller.isSequenceFinished());
    }

    // US10 - caso limite: playlist null gestita senza crash
    @Test
    void startPlaylistWithNullPlaylistHandledWithoutCrash() {
        assertDoesNotThrow(() -> controller.startPlaylist(null));
        assertNotNull(controller.getSequence());
        assertTrue(controller.isSequenceFinished());
    }
}
