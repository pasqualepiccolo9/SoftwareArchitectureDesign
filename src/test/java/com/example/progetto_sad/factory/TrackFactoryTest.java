package com.example.progetto_sad.factory;

import com.example.progetto_sad.model.Track;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US1 - Creazione e caricamento traccia
class TrackFactoryTest {

    private static final String VALID_PATH = "C:/musica/brano.mp3";

    @Test
    void createValidTrack() {
        Track t = TrackFactory.createTrack("Imagine", "John Lennon", "Rock", 1971, VALID_PATH);

        assertEquals("Imagine", t.getTitle());
        assertEquals("John Lennon", t.getAuthor());
        assertEquals("Rock", t.getGenre());
        assertEquals(1971, t.getYear());
        assertTrue(t.getDuration() >= 0);
    }

    @Test
    void trimsSurroundingWhitespace() {
        Track t = TrackFactory.createTrack("  Imagine  ", "  John  ", "  Rock  ", 1971, VALID_PATH);

        assertEquals("Imagine", t.getTitle());
        assertEquals("John", t.getAuthor());
        assertEquals("Rock", t.getGenre());
    }

    @Test
    void blankTitleIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TrackFactory.createTrack("   ", "John", "Rock", 1971, VALID_PATH));
    }

    @Test
    void emptyAuthorIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TrackFactory.createTrack("Imagine", "", "Rock", 1971, VALID_PATH));
    }

    @Test
    void nullGenreIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TrackFactory.createTrack("Imagine", "John", null, 1971, VALID_PATH));
    }

    @Test
    void missingAudioFileIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TrackFactory.createTrack("Imagine", "John", "Rock", 1971, "   "));
    }

    @Test
    void yearTooLowIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TrackFactory.createTrack("Imagine", "John", "Rock", 1800, VALID_PATH));
    }

    @Test
    void yearInTheFutureIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TrackFactory.createTrack("Imagine", "John", "Rock", 9999, VALID_PATH));
    }
}
