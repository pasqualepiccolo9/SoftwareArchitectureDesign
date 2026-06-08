package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    // US10 - avanzamento: dopo onTrackFinished() la traccia corrente diventa la seconda
    @Test
    void onTrackFinishedAdvancesToSecondTrack() {
        controller.startPlaylist(playlist);

        controller.onTrackFinished();

        assertEquals(track2, controller.getCurrentTrack());
    }

    // US10 - avanzamento: chiamate consecutive avanzano nell'ordine corretto della playlist
    @Test
    void onTrackFinishedAdvancesThroughAllTracksInOrder() {
        controller.startPlaylist(playlist);

        assertEquals(track1, controller.getCurrentTrack());
        controller.onTrackFinished();
        assertEquals(track2, controller.getCurrentTrack());
        controller.onTrackFinished();
        assertEquals(track3, controller.getCurrentTrack());
    }

    // US10 - avanzamento: dopo l'ultima traccia la sequenza risulta terminata e senza traccia corrente
    @Test
    void onTrackFinishedOnLastTrackFinishesSequence() {
        controller.startPlaylist(playlist);

        controller.onTrackFinished();
        controller.onTrackFinished();
        controller.onTrackFinished();

        assertTrue(controller.isSequenceFinished());
        assertNull(controller.getCurrentTrack());
    }

    // US10 - caso limite: onTrackFinished() prima di startPlaylist() non genera crash
    @Test
    void onTrackFinishedWithoutActiveSequenceDoesNotCrash() {
        assertDoesNotThrow(() -> controller.onTrackFinished());
    }

    // US10 - avanzamento: getTracks() mantiene l'ordine originale anche dopo l'avanzamento
    @Test
    void onTrackFinishedDoesNotAlterTrackOrder() {
        controller.startPlaylist(playlist);

        controller.onTrackFinished();

        List<Track> tracks = controller.getSequence().getTracks();
        assertEquals(track1, tracks.get(0));
        assertEquals(track2, tracks.get(1));
        assertEquals(track3, tracks.get(2));
    }

    // US16-T - senza una sequenza attiva non ci sono brani successivi
    @Test
    void getNextTracksReturnsEmptyListWithoutActiveSequence() {
        assertTrue(controller.getNextTracks().isEmpty());
    }

    // US16-T - dopo l'avvio della playlist i successivi mantengono l'ordine originale
    @Test
    void getNextTracksReturnsFollowingTracksInOrderAfterStartPlaylist() {
        controller.startPlaylist(playlist);

        assertEquals(List.of(track2, track3), controller.getNextTracks());
    }

    // US16-T - i successivi si aggiornano dopo la fine di ogni traccia
    @Test
    void getNextTracksUpdatesAfterTrackFinished() {
        controller.startPlaylist(playlist);

        controller.onTrackFinished();
        assertEquals(List.of(track3), controller.getNextTracks());

        controller.onTrackFinished();
        assertTrue(controller.getNextTracks().isEmpty());

        controller.onTrackFinished();
        assertTrue(controller.getNextTracks().isEmpty());
    }

    // US16-T - un brano accodato viene aggiunto in fondo senza alterare l'ordine
    @Test
    void addToQueueAppendsTrackMaintainingOrder() {
        Track queuedTrack = new Track("Queued Song", "Queued Artist", 210, "Soul", 2023);
        controller.startPlaylist(playlist);

        controller.addToQueue(queuedTrack);

        assertEquals(List.of(track1, track2, track3, queuedTrack),
                controller.getSequence().getTracks());
        assertEquals(List.of(track2, track3, queuedTrack), controller.getNextTracks());
    }

    // US16-T - accodare null non genera crash e non modifica la sequenza
    @Test
    void addToQueueWithNullDoesNotCrashOrChangeSequence() {
        controller.startPlaylist(playlist);
        List<Track> tracksBeforeAdd = controller.getSequence().getTracks();

        assertDoesNotThrow(() -> controller.addToQueue(null));

        assertEquals(tracksBeforeAdd, controller.getSequence().getTracks());
        assertEquals(List.of(track2, track3), controller.getNextTracks());
    }

    // US16-T - senza sequenza attiva l'accodamento crea una sequenza con il brano
    @Test
    void addToQueueWithoutActiveSequenceCreatesSequenceWithTrack() {
        controller.addToQueue(track1);

        assertNotNull(controller.getSequence());
        assertEquals(List.of(track1), controller.getSequence().getTracks());
        assertEquals(track1, controller.getCurrentTrack());
        assertTrue(controller.getNextTracks().isEmpty());
    }

    @Test
    void removeNextTrackAtDelegatesToActiveSequence() {
        controller.startPlaylist(playlist);

        assertTrue(controller.removeNextTrackAt(0));

        assertEquals(track1, controller.getCurrentTrack());
        assertEquals(List.of(track3), controller.getNextTracks());
        assertEquals(List.of(track1, track3), controller.getSequence().getTracks());
    }

    @Test
    void removeNextTrackAtWithoutActiveSequenceReturnsFalseWithoutCrash() {
        assertDoesNotThrow(() -> controller.removeNextTrackAt(0));
        assertTrue(controller.getNextTracks().isEmpty());
        assertFalse(controller.removeNextTrackAt(0));
    }
}
