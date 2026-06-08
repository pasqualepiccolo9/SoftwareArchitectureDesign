package com.example.progetto_sad.support;

import com.example.progetto_sad.model.Track;

/**
 * Tracce di prova riutilizzabili nei test del player e dei controller collegati.
 *
 * Evita di duplicare dati di esempio nei test degli altri membri del gruppo.
 */
public final class PlayerTestFixtures {

    public static final String DEFAULT_FILE_PATH = "C:/test/musica/brano.mp3";
    public static final int DEFAULT_DURATION_SECONDS = 180;

    private PlayerTestFixtures() {
    }

    /**
     * Traccia campione con durata predefinita (3 minuti).
     */
    public static Track sampleTrack() {
        return trackWithDuration(DEFAULT_DURATION_SECONDS);
    }

    /**
     * Traccia campione con durata personalizzata (utile per test su progresso e tempo).
     *
     * @param durationSeconds durata in secondi della traccia fake
     */
    public static Track trackWithDuration(int durationSeconds) {
        return new Track(
                "Brano di prova",
                "Artista di prova",
                "Rock",
                2020,
                DEFAULT_FILE_PATH,
                durationSeconds
        );
    }
}
