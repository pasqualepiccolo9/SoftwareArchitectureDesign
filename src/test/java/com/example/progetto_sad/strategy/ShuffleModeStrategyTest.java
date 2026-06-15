package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.support.ShuffleTestFixtures;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        strategy.reset(tracks.size(), 0);

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

    // US18 - un solo brano
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
        strategy.reset(tracks.size(), 2);

        Track next = strategy.getNextTrack(tracks, 2);

        assertNotNull(next);
        assertTrue(tracks.indexOf(next) != 2);
    }

    // US18 - ciclo completo senza ripetizioni finché restano brani nel giro
    @Test
    void playsAllOtherTracksBeforeStartingNewCycle() {
        List<Track> tracks = ShuffleTestFixtures.trackList(3);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle(99L);
        strategy.reset(tracks.size(), 0);

        Set<Track> played = new HashSet<>();
        int currentIndex = 0;
        for (int step = 0; step < 2; step++) {
            Track next = strategy.getNextTrack(tracks, currentIndex);
            assertNotNull(next);
            played.add(next);
            currentIndex = tracks.indexOf(next);
        }
        assertEquals(2, played.size());
    }

    // US18 - reset quando cambia la dimensione della coda
    @Test
    void resetsWhenQueueSizeChanges() {
        List<Track> three = ShuffleTestFixtures.trackList(3);
        List<Track> four = ShuffleTestFixtures.trackList(4);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle();
        strategy.reset(three.size(), 0);

        Track fromLargerQueue = strategy.getNextTrack(four, 0);

        assertNotNull(fromLargerQueue);
        assertTrue(four.contains(fromLargerQueue));
    }

    // US18 - reset esplicito al cambio playlist/coda
    @Test
    void explicitResetStartsNewShuffleCycle() {
        List<Track> tracks = ShuffleTestFixtures.trackList(3);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle(5L);
        strategy.reset(tracks.size(), 0);
        Track first = strategy.getNextTrack(tracks, 0);

        strategy.reset(tracks.size(), tracks.indexOf(first));
        Track afterReset = strategy.getNextTrack(tracks, tracks.indexOf(first));

        assertNotNull(first);
        assertNotNull(afterReset);
    }

    // US18 - duplicati in coda: selezione per indice, non per uguaglianza oggetto
    @Test
    void handlesDuplicateTracksByPosition() {
        Track shared = ShuffleTestFixtures.track("Dup", 100);
        List<Track> tracks = ShuffleTestFixtures.duplicateTrackList(shared, 3);
        ShuffleModeStrategy strategy = ShuffleTestFixtures.deterministicShuffle(11L);
        strategy.reset(tracks.size(), 1);

        Track next = strategy.getNextTrack(tracks, 1);

        assertNotNull(next);
        assertEquals(shared, next);
    }

    // US18 - cambio modalità / indice non valido senza eccezioni
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

        strategyA.reset(firstUse.size(), 0);
        strategyB.reset(secondUse.size(), 0);

        assertNotNull(strategyA.getNextTrack(firstUse, 0));
        assertNotNull(strategyB.getNextTrack(secondUse, 0));
        assertEquals(3, firstUse.size());
        assertEquals(3, secondUse.size());
    }
}
