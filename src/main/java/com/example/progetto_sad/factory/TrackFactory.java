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
     * essere compreso tra {@value #MIN_YEAR} e l'anno corrente. La durata e' gia'
     * stata estratta automaticamente dal file al caricamento (vedi
     * AudioDurationExtractor) e qui viene solo inserita nella traccia; i campi
     * testuali vengono ripuliti dagli spazi.
     *
     * @param title           titolo della traccia (obbligatorio)
     * @param author          autore/artista (obbligatorio)
     * @param genre           genere musicale (obbligatorio)
     * @param year            anno di pubblicazione (tra {@value #MIN_YEAR} e l'anno corrente)
     * @param filePath        percorso del file audio (obbligatorio)
     * @param durationSeconds durata in secondi gia' estratta dal file audio
     * @return la nuova traccia validata
     * @throws IllegalArgumentException se un campo obbligatorio e' vuoto o l'anno e' fuori intervallo
     */
    public static Track createTrack(String title, String author, String genre, int year,
                                    String filePath, int durationSeconds) {
 
        validate(title, author, genre, year, filePath);
        return new Track(title.trim(), author.trim(), durationSeconds, genre.trim(), year);
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
}
