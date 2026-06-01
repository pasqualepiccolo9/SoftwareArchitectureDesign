package com.example.progetto_sad.util;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

// US1 - estrazione automatica della durata dal file audio
class AudioDurationExtractorTest {

    // file nullo: nessuna durata estraibile -> 0 (nessuna eccezione)
    @Test
    void returnsZeroForNullFile() {
        assertEquals(0, AudioDurationExtractor.extractSeconds(null));
    }

    // file inesistente: durata sconosciuta -> 0 (gestione robusta, niente crash)
    @Test
    void returnsZeroForNonExistentFile() {
        assertEquals(0, AudioDurationExtractor.extractSeconds(new File("file-inesistente.mp3")));
    }
}
