package com.example.progetto_sad.factory;

import com.example.progetto_sad.model.Track;
import org.junit.jupiter.api.Test;

import java.time.Year;

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

    // US1 - boundary: limite inferiore valido (MIN_YEAR = 1877) accettato
    @Test
    void minYearBoundaryIsAccepted() {
        Track t = TrackFactory.createTrack("Brano", "Autore", "Rock", 1877, VALID_PATH);
        assertEquals(1877, t.getYear());
    }

    // US1 - boundary: un anno appena sotto il minimo viene rifiutato
    @Test
    void yearJustBelowMinIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TrackFactory.createTrack("Brano", "Autore", "Rock", 1876, VALID_PATH));
    }

    // US1 - boundary: limite superiore valido (anno corrente) accettato
    @Test
    void currentYearBoundaryIsAccepted() {
        int currentYear = Year.now().getValue();
        Track t = TrackFactory.createTrack("Brano", "Autore", "Rock", currentYear, VALID_PATH);
        assertEquals(currentYear, t.getYear());
    }

    // US1 - boundary: un anno appena sopra l'anno corrente viene rifiutato
    @Test
    void yearJustAboveCurrentIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TrackFactory.createTrack("Brano", "Autore", "Rock",
                        Year.now().getValue() + 1, VALID_PATH));
    }
}
