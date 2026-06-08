package com.example.progetto_sad.support;

/**
 * Timer controllabile per i test che dipendono dal tempo di riproduzione.
 *
 * Sostituisce l'attesa su clock reale: il test imposta o avanza manualmente
 * i secondi trascorsi.
 */
public class FakePlaybackClock {

    private int currentTimeSeconds;

    public int getCurrentTimeSeconds() {
        return currentTimeSeconds;
    }

    public void setCurrentTimeSeconds(int seconds) {
        this.currentTimeSeconds = Math.max(0, seconds);
    }

    /**
     * Avanza il tempo simulato del numero di secondi indicato.
     *
     * @param seconds incremento in secondi (valori negativi vengono ignorati)
     */
    public void advance(int seconds) {
        if (seconds > 0) {
            currentTimeSeconds += seconds;
        }
    }

    public void reset() {
        currentTimeSeconds = 0;
    }
}
