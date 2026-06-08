package com.example.progetto_sad.util;

/**
 * US21 - Calcolo del progresso di riproduzione di un brano.
 *
 * Deriva un valore normalizzato tra 0.0 e 1.0 a partire dal tempo corrente
 * e dalla durata totale. Indipendente dalla View, riutilizzabile dal Controller.
 */
public final class PlaybackProgress {

    private PlaybackProgress() {
    }

    /**
     * US21 - Calcola il progresso con gestione di valori nulli o non validi.
     *
     * @param currentSeconds tempo trascorso in secondi
     * @param totalSeconds   durata totale in secondi
     * @return valore tra 0.0 e 1.0; 0.0 se la durata e' nulla, negativa o i parametri sono null
     */
    public static double calculate(Integer currentSeconds, Integer totalSeconds) {
        if (currentSeconds == null || totalSeconds == null) {
            return 0.0;
        }
        return calculate(currentSeconds.intValue(), totalSeconds.intValue());
    }

    /**
     * US21 - Calcola il progresso mantenendo il risultato in un range sicuro [0.0, 1.0].
     *
     * @param currentSeconds tempo trascorso in secondi
     * @param totalSeconds   durata totale in secondi
     * @return 0.0 se la durata e' &lt;= 0; altrimenti il rapporto corrente/durata,
     *         limitato tra 0.0 e 1.0
     */
    public static double calculate(int currentSeconds, int totalSeconds) {
        if (totalSeconds <= 0) {
            return 0.0;
        }
        int safeCurrent = Math.max(0, currentSeconds);
        if (safeCurrent >= totalSeconds) {
            return 1.0;
        }
        return (double) safeCurrent / totalSeconds;
    }
}
