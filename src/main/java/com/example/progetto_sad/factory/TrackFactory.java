package com.example.progetto_sad.factory;

import com.example.progetto_sad.model.Track;

import java.time.Year;

public class TrackFactory {

    private static final int MIN_YEAR = 1877; // prima registrazione sonora conosciuta

    private TrackFactory() {
    }

    /**
     * Crea una nuova traccia validando i dati inseriti dall'utente.
     *
     * Titolo, autore, genere e file audio devono essere non vuoti; l'anno deve
     * essere compreso tra {@value #MIN_YEAR} e l'anno corrente. La durata viene
     * estratta dal file audio e i campi testuali vengono ripuliti dagli spazi.
     *
     * @param title    titolo della traccia (obbligatorio)
     * @param author   autore/artista (obbligatorio)
     * @param genre    genere musicale (obbligatorio)
     * @param year     anno di pubblicazione (tra {@value #MIN_YEAR} e l'anno corrente)
     * @param filePath percorso del file audio (obbligatorio)
     * @return la nuova traccia validata
     * @throws IllegalArgumentException se un campo obbligatorio e' vuoto o l'anno e' fuori intervallo
     */
    public static Track createTrack(String title, String author, String genre, int year, String filePath) {
        validate(title, author, genre, year, filePath);
        int duration = extractDuration(filePath);
        return new Track(title.trim(), author.trim(), duration, genre.trim(), year);
    }

    private static void validate(String title, String author, String genre, int year, String filePath) {
        if (isBlank(title)) {
            throw new IllegalArgumentException("Il titolo e' obbligatorio");
        }
        if (isBlank(author)) {
            throw new IllegalArgumentException("L'autore e' obbligatorio");
        }
        if (isBlank(genre)) {
            throw new IllegalArgumentException("Il genere e' obbligatorio");
        }
        if (isBlank(filePath)) {
            throw new IllegalArgumentException("Il file audio e' obbligatorio");
        }
        int currentYear = Year.now().getValue();
        if (year < MIN_YEAR || year > currentYear) {
            throw new IllegalArgumentException(
                    "L'anno deve essere compreso tra " + MIN_YEAR + " e " + currentYear);
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
