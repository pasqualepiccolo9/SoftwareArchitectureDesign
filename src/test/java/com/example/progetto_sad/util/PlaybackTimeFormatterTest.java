package com.example.progetto_sad.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// US21 - formattazione tempo di riproduzione per la UI
class PlaybackTimeFormatterTest {

    // US21 - zero secondi
    @Test
    void formatsZeroSeconds() {
        assertEquals("0:00", PlaybackTimeFormatter.formatSeconds(0));
    }

    // US21 - meno di un minuto
    @Test
    void formatsLessThanOneMinute() {
        assertEquals("0:45", PlaybackTimeFormatter.formatSeconds(45));
    }

    // US21 - piu' minuti
    @Test
    void formatsMultipleMinutes() {
        assertEquals("2:05", PlaybackTimeFormatter.formatSeconds(125));
    }

    // US21 - valore null trattato come zero
    @Test
    void formatsNullAsZero() {
        assertEquals("0:00", PlaybackTimeFormatter.formatSeconds((Integer) null));
    }

    // US21 - valore negativo trattato come zero
    @Test
    void formatsNegativeAsZero() {
        assertEquals("0:00", PlaybackTimeFormatter.formatSeconds(-10));
    }

    // US21 - caso limite con minuti a due cifre
    @Test
    void formatsLongDuration() {
        assertEquals("61:01", PlaybackTimeFormatter.formatSeconds(3661));
    }
}
