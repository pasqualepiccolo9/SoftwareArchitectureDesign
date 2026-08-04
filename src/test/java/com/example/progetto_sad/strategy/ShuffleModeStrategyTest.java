package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.PlaylistSequence;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.support.ShuffleTestFixtures;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US18 - test sulla strategia shuffle
class ShuffleModeStrategyTest {

    // US18 - selezione valida entro i limiti della coda
    @Test
    void selectsValidTrackWithinQueueBounds() {
        List<Track> tracks = ShuffleTestFixtures.trackList(4);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle();

        Track next = strategy.getNextTrack(tracks, 0);

        assertNotNull(next);
        assertTrue(tracks.contains(next));
        assertTrue(tracks.indexOf(next) >= 0 && tracks.indexOf(next) < tracks.size());
    }

    // US18 - coda vuota
    @Test
    void returnsNullForEmptyQueue() {
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle();

        assertNull(strategy.getNextTrack(List.of(), 0));
        assertNull(strategy.getNextTrack(null, 0));
    }

    // US18 - un solo brano: non resta nulla da riprodurre dopo quello corrente
    @Test
    void returnsNullForSingleTrackQueue() {
        List<Track> tracks = ShuffleTestFixtures.trackList(1);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle();

        assertNull(strategy.getNextTrack(tracks, 0));
    }

    // US18 - nessuna ripetizione immediata con più brani disponibili
    @Test
    void doesNotRepeatCurrentTrackImmediately() {
        List<Track> tracks = ShuffleTestFixtures.trackList(5);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle(7L);

        Track next = strategy.getNextTrack(tracks, 2);

        assertNotNull(next);
        assertTrue(tracks.indexOf(next) != 2);
    }

    // US18 - l'estrazione pesca solo tra i brani non ancora riprodotti
    @Test
    void onlyPicksTracksNotPlayedYet() {
        List<Track> tracks = ShuffleTestFixtures.trackList(5);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle(3L);

        for (int attempt = 0; attempt < 20; attempt++) {
            Track next = strategy.getNextTrack(tracks, 2);

            assertNotNull(next);
            assertTrue(tracks.indexOf(next) > 2,
                    "lo shuffle non deve riproporre brani già riprodotti");
        }
    }

    // US18 - la coda viene percorsa interamente, ogni brano una sola volta, poi termina
    @Test
    void playsEveryQueuedTrackOnceAndThenStops() {
        List<Track> tracks = ShuffleTestFixtures.trackList(5);
        PlaylistSequence sequence = new PlaylistSequence(tracks);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle(99L);

        List<Track> played = new ArrayList<>();
        played.add(sequence.getCurrentTrack());
        while (strategy.moveToNextTrack(sequence)) {
            played.add(sequence.getCurrentTrack());
        }

        assertEquals(5, played.size());
        assertEquals(5, new HashSet<>(played).size());
        assertTrue(played.containsAll(tracks));
        assertFalse(strategy.hasNextTrack(sequence));
    }

    // US18 - l'ordine di riproduzione non è quello della coda originale
    @Test
    void playbackOrderIsShuffledNotSequential() {
        List<Track> tracks = ShuffleTestFixtures.trackList(6);
        PlaylistSequence sequence = new PlaylistSequence(tracks);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle(99L);

        List<Track> played = new ArrayList<>();
        played.add(sequence.getCurrentTrack());
        while (strategy.moveToNextTrack(sequence)) {
            played.add(sequence.getCurrentTrack());
        }

        assertNotEquals(tracks, played);
    }

    // US18 - duplicati in coda: selezione per posizione, non per uguaglianza oggetto
    @Test
    void handlesDuplicateTracksByPosition() {
        Track shared = ShuffleTestFixtures.track("Dup", 100);
        List<Track> tracks = ShuffleTestFixtures.duplicateTrackList(shared, 3);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle(11L);

        Track next = strategy.getNextTrack(tracks, 1);

        assertNotNull(next);
        assertEquals(shared, next);
    }

    // US18 - indice non valido senza eccezioni, come previsto dal contratto comune
    @Test
    void handlesInvalidCurrentIndexWithoutCrash() {
        List<Track> tracks = ShuffleTestFixtures.trackList(3);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle();

        assertDoesNotThrow(() -> assertNull(strategy.getNextTrack(tracks, -1)));
        assertDoesNotThrow(() -> assertNull(strategy.getNextTrack(tracks, 10)));
    }

    // US18-T - i fixture shuffle sono riutilizzabili senza ricreare tracce a ogni test
    @Test
    void shuffleFixturesAreReusableAcrossTests() {
        List<Track> firstUse = ShuffleTestFixtures.trackList(3);
        List<Track> secondUse = ShuffleTestFixtures.trackList(3);
        ShuffleModeStrategy strategyA = ShuffleTestFixtures.deterministicShuffle();
        ShuffleModeStrategy strategyB = ShuffleTestFixtures.deterministicShuffle();

        assertNotNull(strategyA.getNextTrack(firstUse, 0));
        assertNotNull(strategyB.getNextTrack(secondUse, 0));
        assertEquals(3, firstUse.size());
        assertEquals(3, secondUse.size());
    }

    // US18-T - la strategia dichiara la propria modalità: nessun controllo di tipo altrove
    @Test
    void modeIdentifiesShufflePlayback() {
        assertEquals(PlayMode.SHUFFLE, ShuffleTestFixtures.deterministicShuffle().getMode());
    }

    // US18-T - l'avanzamento casuale cambia brano preservando l'intero contenuto della coda
    @Test
    void moveToNextTrackKeepsEveryQueuedTrackAvailable() {
        List<Track> tracks = ShuffleTestFixtures.trackList(4);
        PlaylistSequence sequence = new PlaylistSequence(tracks);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle();

        assertTrue(strategy.moveToNextTrack(sequence));

        assertNotEquals(tracks.get(0), sequence.getCurrentTrack());
        assertEquals(4, sequence.getTracks().size());
        assertTrue(sequence.getTracks().containsAll(tracks));
    }

    // US18 - con un solo brano non esiste una scelta casuale successiva
    @Test
    void moveToNextTrackReturnsFalseForSingleTrackQueue() {
        PlaylistSequence sequence = new PlaylistSequence(ShuffleTestFixtures.trackList(1));
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle();

        assertFalse(strategy.moveToNextTrack(sequence));
    }

    // US18-INT - i brani accodati durante la riproduzione entrano nel giro casuale
    @Test
    void tracksQueuedDuringPlaybackJoinTheShuffle() {
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle();
        PlaylistSequence sequence = new PlaylistSequence(ShuffleTestFixtures.trackList(3));
        Track extra = ShuffleTestFixtures.track("Extra", 140);

        assertTrue(strategy.moveToNextTrack(sequence));
        sequence.addTrack(extra);

        Set<Track> played = new HashSet<>();
        while (strategy.moveToNextTrack(sequence)) {
            played.add(sequence.getCurrentTrack());
        }

        assertTrue(played.contains(extra));
        assertFalse(strategy.hasNextTrack(sequence));
    }
}
