package com.example.progetto_sad.util;

/**
 * US21 - Formattazione del tempo di riproduzione per la UI.
 *
 * Converte i secondi del player in una stringa leggibile (es. 0:00, 0:45, 2:05).
 * La logica e' indipendente dalla View e puo' essere riusata dai Controller.
 */
public final class PlaybackTimeFormatter {

    private static final String ZERO_TIME = "0:00";

    private PlaybackTimeFormatter() {
    }

    /**
     * US21 - Formatta i secondi in formato {@code minuti:secondi} con due cifre per i secondi.
     *
     * @param seconds secondi da formattare; se {@code null} o negativi, restituisce 0:00
     * @return rappresentazione testuale del tempo (es. 2:05)
     */
    public static String formatSeconds(Integer seconds) {
        if (seconds == null) {
            return ZERO_TIME;
        }
        return formatSeconds(seconds.intValue());
    }

    /**
     * US21 - Formatta i secondi in formato {@code minuti:secondi} con due cifre per i secondi.
     *
     * @param seconds secondi da formattare; valori negativi vengono trattati come 0
     * @return rappresentazione testuale del tempo (es. 0:45)
     */
    public static String formatSeconds(int seconds) {
        int safeSeconds = Math.max(0, seconds);
        int minutes = safeSeconds / 60;
        int remainingSeconds = safeSeconds % 60;
        return minutes + ":" + String.format("%02d", remainingSeconds);
    }
}
