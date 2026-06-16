package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US26-T - generazione automatica playlist per anno
class PlaylistByYearGenerationTest {

    private PlaylistManager manager;
    private PlaylistController controller;
    private TrackLibrary library;

    @BeforeEach
    void setUp() {
        manager = new PlaylistManager();
        controller = new PlaylistController(manager);
        library = new TrackLibrary();
    }

    // US26-T - seleziona solo le tracce dell'anno richiesto
    @Test
    void selectsOnlyTracksFromRequestedYear() {
        Track t2020a = track("A", 2020);
        Track t2021 = track("B", 2021);
        Track t2020b = track("C", 2020);
        library.addTrack(t2020a);
        library.addTrack(t2021);
        library.addTrack(t2020b);

        Playlist generated = controller.createPlaylistByYear(2020, "Anno 2020", library);

        assertNotNull(generated);
        assertEquals(List.of(t2020a, t2020b), generated.getTracks());
        for (Track track : generated.getTracks()) {
            assertEquals(2020, track.getYear());
        }
    }

    // US26-T - la playlist generata contiene i brani corretti
    @Test
    void generatedPlaylistContainsExpectedTracks() {
        Track target1 = track("One", 1999);
        Track other = track("Two", 2000);
        Track target2 = track("Three", 1999);
        library.addTrack(target1);
        library.addTrack(other);
        library.addTrack(target2);

        Playlist generated = controller.createPlaylistByYear(1999, "Hits 1999", library);

        assertNotNull(generated);
        assertEquals("Hits 1999", generated.getName());
        assertEquals(2, generated.getTracks().size());
        assertTrue(generated.getTracks().contains(target1));
        assertTrue(generated.getTracks().contains(target2));
        assertFalse(generated.getTracks().contains(other));
    }

    // US26-T - input valido senza risultati: nessuna playlist creata
    @Test
    void returnsNullWhenNoTracksMatchYear() {
        library.addTrack(track("A", 2020));
        library.addTrack(track("B", 2021));

        Playlist generated = controller.createPlaylistByYear(2019, "Anno 2019", library);

        assertNull(generated);
        assertTrue(manager.getPlaylists().isEmpty());
    }

    // US26-T - libreria vuota: nessuna playlist creata
    @Test
    void returnsNullWhenLibraryIsEmpty() {
        Playlist generated = controller.createPlaylistByYear(2020, "Anno 2020", library);

        assertNull(generated);
        assertTrue(manager.getPlaylists().isEmpty());
    }

    // US26-T - nome default "Brani <anno>" e registrazione nel manager
    @Test
    void defaultNameGenerationRegistersPlaylistInManager() {
        Track t2022 = track("Hit", 2022);
        library.addTrack(t2022);

        Playlist generated = controller.createPlaylistByYear(2022, library);

        assertNotNull(generated);
        assertEquals("Brani 2022", generated.getName());
        assertEquals(1, manager.getPlaylists().size());
        assertEquals(generated, manager.getPlaylists().get(0));
    }

    private static Track track(String title, int year) {
        return new Track(title, "Artist", "Pop", year, "C:/test/" + title + ".mp3", 180);
    }
}
