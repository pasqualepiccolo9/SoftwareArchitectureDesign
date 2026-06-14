package com.example.progetto_sad.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US10 - test per la logica interna della sequenza di riproduzione (PlaylistSequence)
class PlaylistSequenceTest {

    private Playlist playlist;
    private Track track1;
    private Track track2;
    private Track track3;

    @BeforeEach
    void setUp() {
        playlist = new Playlist("Test Playlist");
        track1 = new Track("Song A", "Artist A", "Pop", 2020, null, 180);
        track2 = new Track("Song B", "Artist B", "Rock", 2021, null, 200);
        track3 = new Track("Song C", "Artist C", "Jazz", 2022, null, 240);
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.addTrack(track3);
    }

    // US10 - sequenza da playlist non vuota: isEmpty() deve restituire false
    @Test
    void nonEmptyPlaylistSequenceIsNotEmpty() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);

        assertFalse(sequence.isEmpty());
    }

    // US10 - sequenza appena creata: isFinished() deve restituire false
    @Test
    void nonEmptyPlaylistSequenceIsNotFinishedAtStart() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);

        assertFalse(sequence.isFinished());
    }

    // US10 - la prima traccia della playlist e' quella corrente all'avvio
    @Test
    void firstTrackIsCurrentAtStart() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);

        assertEquals(track1, sequence.getCurrentTrack());
    }

    // US10 - getTracks() restituisce le tracce nello stesso ordine della playlist
    @Test
    void getTracksReturnsTracksInPlaylistOrder() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);

        List<Track> tracks = sequence.getTracks();
        assertEquals(track1, tracks.get(0));
        assertEquals(track2, tracks.get(1));
        assertEquals(track3, tracks.get(2));
    }

    // US10 - advance() sposta la traccia corrente alla successiva nell'ordine
    @Test
    void advanceMakesNextTrackCurrent() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);

        sequence.advance();

        assertEquals(track2, sequence.getCurrentTrack());
    }

    // US10 - dopo l'avanzamento oltre l'ultima traccia, isFinished() restituisce true
    @Test
    void isFinishedAfterLastTrack() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);

        sequence.advance();
        sequence.advance();
        sequence.advance();

        assertTrue(sequence.isFinished());
    }

    // US10 - dopo la fine della sequenza, getCurrentTrack() restituisce null
    @Test
    void getCurrentTrackReturnsNullWhenFinished() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);

        sequence.advance();
        sequence.advance();
        sequence.advance();

        assertNull(sequence.getCurrentTrack());
    }

    // US10 - caso limite: playlist vuota genera una sequenza vuota e gia' terminata
    @Test
    void emptyPlaylistCreatesEmptyAndFinishedSequence() {
        Playlist empty = new Playlist("Empty");

        PlaylistSequence sequence = PlaylistSequence.from(empty);

        assertTrue(sequence.isEmpty());
        assertTrue(sequence.isFinished());
    }

    // US10 - caso limite: playlist null gestita senza crash, sequenza risulta vuota
    @Test
    void nullPlaylistHandledWithoutCrash() {
        assertDoesNotThrow(() -> PlaylistSequence.from(null));
        PlaylistSequence sequence = PlaylistSequence.from(null);
        assertTrue(sequence.isEmpty());
        assertTrue(sequence.isFinished());
    }

    // US10 - caso limite: playlist con una sola traccia termina dopo una sola advance()
    @Test
    void singleTrackPlaylistFinishesAfterOneAdvance() {
        Playlist single = new Playlist("Single");
        single.addTrack(track1);
        PlaylistSequence sequence = PlaylistSequence.from(single);

        sequence.advance();

        assertTrue(sequence.isFinished());
        assertNull(sequence.getCurrentTrack());
    }

    // US10 - caso limite: advance() su sequenza gia' terminata non genera crash e mantiene lo stato
    @Test
    void advanceOnFinishedSequenceDoesNotCrash() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);
        sequence.advance();
        sequence.advance();
        sequence.advance();

        assertDoesNotThrow(() -> sequence.advance());
        assertTrue(sequence.isFinished());
    }

    // US16-T - getNextTracks() restituisce i brani successivi in ordine, escludendo quello corrente
    @Test
    void getNextTracksReturnsFollowingTracksInOrderWithoutCurrentTrack() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);

        List<Track> nextTracks = sequence.getNextTracks();

        assertEquals(List.of(track2, track3), nextTracks);
        assertFalse(nextTracks.contains(track1));
    }

    // US16-T - una sequenza vuota non espone brani successivi
    @Test
    void getNextTracksReturnsEmptyListForEmptySequence() {
        PlaylistSequence sequence = PlaylistSequence.empty();

        assertTrue(sequence.getNextTracks().isEmpty());
    }

    // US16-T - una sequenza terminata non espone brani successivi
    @Test
    void getNextTracksReturnsEmptyListForFinishedSequence() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);
        sequence.advance();
        sequence.advance();
        sequence.advance();

        assertTrue(sequence.getNextTracks().isEmpty());
    }

    // US16-T - una sequenza con una sola traccia non ha brani successivi
    @Test
    void getNextTracksReturnsEmptyListForSingleTrackSequence() {
        Playlist single = new Playlist("Single");
        single.addTrack(track1);
        PlaylistSequence sequence = PlaylistSequence.from(single);

        assertTrue(sequence.getNextTracks().isEmpty());
    }

    // US16-T - quando la traccia corrente e' l'ultima non ci sono brani successivi
    @Test
    void getNextTracksReturnsEmptyListWhenCurrentTrackIsLast() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);
        sequence.advance();
        sequence.advance();

        assertEquals(track3, sequence.getCurrentTrack());
        assertTrue(sequence.getNextTracks().isEmpty());
    }

    // US16-T - la lista esposta non consente di modificare lo stato interno
    @Test
    void getNextTracksReturnsUnmodifiableListWithoutChangingSequence() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);
        List<Track> nextTracks = sequence.getNextTracks();

        assertThrows(UnsupportedOperationException.class, () -> nextTracks.add(track1));
        assertEquals(List.of(track2, track3), sequence.getNextTracks());
        assertEquals(track1, sequence.getCurrentTrack());
    }

    // US16-T - dopo advance() i brani successivi riflettono la nuova traccia corrente
    @Test
    void getNextTracksUpdatesAfterAdvance() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);

        sequence.advance();

        assertEquals(track2, sequence.getCurrentTrack());
        assertEquals(List.of(track3), sequence.getNextTracks());
    }

    @Test
    void removeNextTrackAtRemovesFirstNextTrackWithoutChangingCurrent() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);

        assertTrue(sequence.removeNextTrackAt(0));

        assertEquals(track1, sequence.getCurrentTrack());
        assertEquals(List.of(track1, track3), sequence.getTracks());
        assertEquals(List.of(track3), sequence.getNextTracks());
    }

    @Test
    void removeNextTrackAtRemovesIntermediateTrackMaintainingOrder() {
        Track track4 = new Track("Song D", "Artist D", "Soul", 2023, null, 210);
        PlaylistSequence sequence = new PlaylistSequence(List.of(track1, track2, track3, track4));

        assertTrue(sequence.removeNextTrackAt(1));

        assertEquals(List.of(track1, track2, track4), sequence.getTracks());
        assertEquals(List.of(track2, track4), sequence.getNextTracks());
    }

    @Test
    void removeNextTrackAtRemovesSelectedDuplicatePosition() {
        PlaylistSequence sequence = new PlaylistSequence(List.of(track1, track2, track3, track2));

        assertTrue(sequence.removeNextTrackAt(2));

        assertEquals(List.of(track1, track2, track3), sequence.getTracks());
        assertEquals(List.of(track2, track3), sequence.getNextTracks());
    }

    @Test
    void removeNextTrackAtWithInvalidIndexDoesNotChangeSequence() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);
        List<Track> originalTracks = sequence.getTracks();

        assertFalse(sequence.removeNextTrackAt(-1));
        assertFalse(sequence.removeNextTrackAt(2));

        assertEquals(originalTracks, sequence.getTracks());
        assertEquals(track1, sequence.getCurrentTrack());
    }

    @Test
    void removeNextTrackAtAfterAdvanceDoesNotChangeCurrentTrack() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);
        sequence.advance();

        assertTrue(sequence.removeNextTrackAt(0));

        assertEquals(track2, sequence.getCurrentTrack());
        assertEquals(List.of(track1, track2), sequence.getTracks());
        assertTrue(sequence.getNextTracks().isEmpty());
    }

    @Test
    void removeTracksNotInRemovesDeletedTracksAndPreservesCurrentPosition() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);
        sequence.advance();

        List<Integer> removedIndices = sequence.removeTracksNotIn(List.of(track1, track3));

        assertEquals(List.of(1), removedIndices);
        assertEquals(List.of(track1, track3), sequence.getTracks());
        assertEquals(track3, sequence.getCurrentTrack());
        assertTrue(sequence.getNextTracks().isEmpty());
    }

    @Test
    void removeTracksNotInRemovesPreviousTracksAndShiftsCurrentIndex() {
        PlaylistSequence sequence = PlaylistSequence.from(playlist);
        sequence.advance();

        List<Integer> removedIndices = sequence.removeTracksNotIn(List.of(track2, track3));

        assertEquals(List.of(0), removedIndices);
        assertEquals(List.of(track2, track3), sequence.getTracks());
        assertEquals(track2, sequence.getCurrentTrack());
        assertEquals(List.of(track3), sequence.getNextTracks());
    }
}
