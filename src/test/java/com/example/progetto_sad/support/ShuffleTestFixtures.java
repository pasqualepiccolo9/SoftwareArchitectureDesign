package com.example.progetto_sad.support;

import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.strategy.ShuffleModeStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dati di supporto per i test sulla modalità shuffle (US18).
 *
 * Fornisce tracce controllate e strategie shuffle con seme fisso per test
 * ripetibili senza dipendere dal tempo reale.
 */
public final class ShuffleTestFixtures {

    public static final long DEFAULT_SEED = 42L;

    private static int trackCounter;

    private ShuffleTestFixtures() {
    }

    public static void resetCounter() {
        trackCounter = 0;
    }

    /**
     * Strategia shuffle con {@link Random} a seme fisso per test deterministici.
     */
    public static ShuffleModeStrategy deterministicShuffle() {
        return new ShuffleModeStrategy(new Random(DEFAULT_SEED));
    }

    /**
     * Strategia shuffle con seme personalizzato.
     */
    public static ShuffleModeStrategy deterministicShuffle(long seed) {
        return new ShuffleModeStrategy(new Random(seed));
    }

    /**
     * Crea {@code count} tracce distinte con titoli univoci.
     */
    public static List<Track> trackList(int count) {
        resetCounter();
        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tracks.add(track("S" + i, 120 + i));
        }
        return tracks;
    }

    /**
     * Crea una traccia di prova.
     */
    public static Track track(String label, int duration) {
        trackCounter++;
        return new Track(
                "Shuffle " + label + " #" + trackCounter,
                "Artist " + trackCounter,
                "Rock",
                2021,
                "C:/shuffle/" + label + "-" + trackCounter + ".mp3",
                duration
        );
    }

    /**
     * Lista con la stessa traccia ripetuta in posizioni diverse (duplicati in coda).
     */
    public static List<Track> duplicateTrackList(Track shared, int occurrences) {
        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < occurrences; i++) {
            tracks.add(shared);
        }
        return tracks;
    }
}
