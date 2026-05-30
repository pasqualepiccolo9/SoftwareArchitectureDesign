package com.example.progetto_sad.factory;

import com.example.progetto_sad.model.Track;

import java.time.Year;

public class TrackFactory {

    private static final int MIN_YEAR = 1877; // prima registrazione sonora conosciuta

    private TrackFactory() {
    }

    public static Track createTrack(String title, String author, String genre, int year, String filePath) {
        validateMetadata(title, author, genre, year);
        if (isBlank(filePath)) {
            throw new IllegalArgumentException("Il file audio e' obbligatorio");
        }
        int duration = extractDuration(filePath);
        return new Track(title.trim(), author.trim(), duration, genre.trim(), year);
    }

    public static void validateMetadata(String title, String author, String genre, int year) {
        if (isBlank(title)) {
            throw new IllegalArgumentException("Il titolo e' obbligatorio");
        }
        if (isBlank(author)) {
            throw new IllegalArgumentException("L'autore e' obbligatorio");
        }
        if (isBlank(genre)) {
            throw new IllegalArgumentException("Il genere e' obbligatorio");
        }
        int currentYear = Year.now().getValue();
        if (year < MIN_YEAR || year > currentYear) {
            throw new IllegalArgumentException(
                    "L'anno deve essere compreso tra " + MIN_YEAR + " e " + currentYear);
        }
    }

    public static void validateDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("La durata non puo' essere negativa");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static int extractDuration(String filePath) {
        // TODO US1: estrarre la durata reale (in secondi) dal file audio.
        // Richiede una libreria audio (es. javafx-media via javafx.scene.media.Media,
        // oppure jaudiotagger) da concordare col team. La Sprint 1 non riproduce audio,
        // quindi per ora si restituisce 0 come placeholder.
        return 0;
    }
}
