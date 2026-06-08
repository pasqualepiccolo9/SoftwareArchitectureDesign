package com.example.progetto_sad.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// US21 - calcolo progresso brano
class PlaybackProgressTest {

    private static final double DELTA = 0.0001;

    // US21 - durata valida, riproduzione a meta'
    @Test
    void calculatesProgressWithValidDuration() {
        assertEquals(0.25, PlaybackProgress.calculate(45, 180), DELTA);
    }

    // US21 - durata zero: nessun progresso calcolabile
    @Test
    void returnsZeroForZeroDuration() {
        assertEquals(0.0, PlaybackProgress.calculate(30, 0), DELTA);
    }

    // US21 - tempo oltre la durata: progresso massimo
    @Test
    void clampsProgressWhenCurrentExceedsDuration() {
        assertEquals(1.0, PlaybackProgress.calculate(200, 180), DELTA);
    }

    // US21 - tempo negativo: progresso minimo
    @Test
    void clampsProgressWhenCurrentIsNegative() {
        assertEquals(0.0, PlaybackProgress.calculate(-5, 180), DELTA);
    }

    // US21 - valori null: progresso zero
    @Test
    void returnsZeroForNullValues() {
        assertEquals(0.0, PlaybackProgress.calculate(null, 180), DELTA);
        assertEquals(0.0, PlaybackProgress.calculate(10, null), DELTA);
        assertEquals(0.0, PlaybackProgress.calculate(null, null), DELTA);
    }

    // US21 - inizio brano
    @Test
    void returnsZeroAtStart() {
        assertEquals(0.0, PlaybackProgress.calculate(0, 240), DELTA);
    }

    // US21 - fine brano
    @Test
    void returnsOneAtEnd() {
        assertEquals(1.0, PlaybackProgress.calculate(240, 240), DELTA);
    }

    // US21 - durata negativa trattata come non valida
    @Test
    void returnsZeroForNegativeDuration() {
        assertEquals(0.0, PlaybackProgress.calculate(10, -60), DELTA);
    }
}
