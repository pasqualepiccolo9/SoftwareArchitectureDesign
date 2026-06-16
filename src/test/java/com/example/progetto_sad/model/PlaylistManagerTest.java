/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.example.progetto_sad.model;

import java.util.List;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author miche
 */
/**
 * Classe di test per verificare la logica di business di PlaylistManager.
 * Copre la creazione, l'eliminazione e la gestione degli errori (US5).
 */
public class PlaylistManagerTest {
    
    private PlaylistManager manager;
    
    /**
     * Metodo di setup eseguito prima di ogni singolo test.
     * Garantisce che ogni test parta con un gestore vuoto e pulito.
     */
    @BeforeEach
    void setUp() {
        
        manager = new PlaylistManager();
    }
    
    /**
     * Testa la creazione di una playlist con un nome valido.
     * Verifica che l'oggetto non sia nullo e che venga aggiunto alla lista.
     */
    @Test
    void testValidPlaylistCreation() {
        Playlist p = manager.createPlaylist("Rock Classics");
        
        // Verifica che la playlist sia stata creata
        assertNotNull(p);
        assertEquals("Rock Classics", p.getName());
        
        // Verifica che il manager l'abbia effettivamente salvata nella sua lista
        assertEquals(1, manager.getPlaylists().size());
        assertEquals("Rock Classics", manager.getPlaylists().get(0).getName());
    }
    
    /**
     * Testa il blocco della creazione se il nome fornito è vuoto.
     * Verifica che venga lanciata l'eccezione e che la lista rimanga vuota.
     */
    @Test
    void testEmptyPlaylistName() {
        // Verifica che il manager (tramite la classe Playlist) blocchi i nomi vuoti
        assertThrows(IllegalArgumentException.class, () -> {
            manager.createPlaylist("");
        });
        
        // Verifica che la lista sia rimasta a 0
        assertEquals(0, manager.getPlaylists().size());
    }
    
    /**
     * Testa la rimozione corretta di una playlist esistente.
     */
    @Test
    void testValidPlaylistRemoval() {
        Playlist p1 = manager.createPlaylist("Rock");
        Playlist p2 = manager.createPlaylist("Jazz");
        
        assertEquals(2, manager.getPlaylists().size());
        
        // Rimuovo la playlist Rock
        manager.removePlaylist(p1);
        
        assertEquals(1, manager.getPlaylists().size());
        assertEquals("Jazz", manager.getPlaylists().get(0).getName());
    }
    
    /**
     * Testa la gestione degli errori quando si tenta di rimuovere
     * una playlist nulla o non registrata nel gestore.
     */
    @Test
    void testInvalidPlaylistRemoval() {
        // Creo una playlist fittizia ma NON la registro nel manager
        Playlist p = new Playlist("Pop");
        
        // Verifico che il manager blocchi il tentativo di rimuovere qualcosa che non gestisce
        assertThrows(IllegalArgumentException.class, () -> {
            manager.removePlaylist(p);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            manager.removePlaylist(null);
        });
    }

    /**
     * US3 - Testa che la rimozione di una playlist la tolga anche dalla lista
     * delle playlist di tutte le tracce che conteneva (sincronizzazione inversa).
     */
    @Test
    void testRemovePlaylistDetachesItFromTracks() {
        Playlist p = manager.createPlaylist("Rock");
        Track track = new Track("Bohemian Rhapsody", "Queen", "Rock", 1975, null, 354);
        p.addTrack(track);

        manager.removePlaylist(p);

        assertFalse(track.getPlaylists().contains(p));
    }

    /**
     * US5 - Testa che non si possa creare una playlist con un nome gia' esistente.
     */
    @Test
    void testDuplicatePlaylistNameThrows() {
        manager.createPlaylist("Rock");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.createPlaylist("Rock");
        });

        assertEquals(1, manager.getPlaylists().size());
    }

    /**
     * US5 - Testa che il controllo sui nomi duplicati ignori spazi iniziali/finali
     * e differenze tra maiuscole e minuscole.
     */
    @Test
    void testDuplicatePlaylistNameIgnoresCaseAndSpaces() {
        manager.createPlaylist("Rock");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.createPlaylist("  rock  ");
        });

        assertEquals(1, manager.getPlaylists().size());
    }

    /**
     * US26-T - Generazione playlist per anno: seleziona solo le tracce dell'anno richiesto.
     */
    @Test
    void testCreatePlaylistForYearSelectsOnlyMatchingYear() {
        Track t2020a = new Track("A", "Author A", "Pop", 2020, null, 180);
        Track t2021 = new Track("B", "Author B", "Rock", 2021, null, 200);
        Track t2020b = new Track("C", "Author C", "Jazz", 2020, null, 210);

        Playlist generated = invokeCreatePlaylistForYear(2020, List.of(t2020a, t2021, t2020b));

        assertNotNull(generated);
        assertEquals("Anno 2020", generated.getName());
        assertEquals(List.of(t2020a, t2020b), generated.getTracks());
        assertEquals(1, manager.getPlaylists().size());
    }

    /**
     * US26-T - La playlist generata contiene esattamente i brani attesi.
     */
    @Test
    void testCreatePlaylistForYearContainsExpectedTracksOnly() {
        Track target1 = new Track("Track 1", "Artist 1", "Pop", 1999, null, 120);
        Track other = new Track("Track 2", "Artist 2", "Rock", 2000, null, 150);
        Track target2 = new Track("Track 3", "Artist 3", "Jazz", 1999, null, 190);

        Playlist generated = invokeCreatePlaylistForYear(1999, List.of(target1, other, target2));

        assertNotNull(generated);
        assertTrue(generated.getTracks().contains(target1));
        assertTrue(generated.getTracks().contains(target2));
        assertFalse(generated.getTracks().contains(other));
        assertEquals(2, generated.getTracks().size());
    }

    /**
     * US26-T - Input valido senza risultati: non deve essere creata alcuna playlist.
     */
    @Test
    void testCreatePlaylistForYearWithNoMatchesReturnsNullAndDoesNotCreate() {
        Track t2020 = new Track("A", "Author A", "Pop", 2020, null, 180);
        Track t2021 = new Track("B", "Author B", "Rock", 2021, null, 200);

        Playlist generated = invokeCreatePlaylistForYear(2019, List.of(t2020, t2021));

        assertNull(generated);
        assertTrue(manager.getPlaylists().isEmpty());
    }

    /**
     * US26-T - Libreria vuota: non deve essere creata alcuna playlist.
     */
    @Test
    void testCreatePlaylistForYearWithEmptyLibraryReturnsNull() {
        Playlist generated = invokeCreatePlaylistForYear(2020, List.of());

        assertNull(generated);
        assertTrue(manager.getPlaylists().isEmpty());
    }

    private Playlist invokeCreatePlaylistForYear(int year, List<Track> tracks) {
        try {
            Method method = PlaylistManager.class.getMethod("createPlaylistForYear", int.class, List.class);
            return (Playlist) method.invoke(manager, year, tracks);
        } catch (NoSuchMethodException e) {
            fail("Metodo mancante: createPlaylistForYear(int, List<Track>)");
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
