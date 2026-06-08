/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import java.util.List;
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
 * Classe di test per verificare la logica di PlaylistController.
 * Assicura che il controller comunichi correttamente con il manager e gestisca
 * in sicurezza le interazioni con le playlist.
 */
public class PlaylistControllerTest {
    
    private PlaylistManager manager;
    private PlaylistController controller;
    
   /**
     * Metodo di setup eseguito prima di ogni test.
     * Inizializza un nuovo gestore e un nuovo controller per avere un ambiente pulito.
     */
    @BeforeEach
    void setUp() {
        manager = new PlaylistManager();
        controller = new PlaylistController(manager);
    }

    /**
     * Testa la corretta creazione di una playlist tramite il controller.
     * Verifica che la richiesta venga passata al gestore e salvata.
     */
    @Test
    void testCreatePlaylistViaController() {
        controller.createPlaylist("Indie Rock");
        
        assertEquals(1, manager.getPlaylists().size());
        assertEquals("Indie Rock", manager.getPlaylists().get(0).getName());
    }

    /**
     * Testa la corretta rimozione di una playlist tramite il controller.
     */
    @Test
    void testRemovePlaylistViaController() {
        // Creazione preventiva diretta per il test
        Playlist p = manager.createPlaylist("Chill Vibes");
        assertEquals(1, manager.getPlaylists().size());
        
        // Rimozione tramite controller
        controller.removePlaylist(p);
        
        assertEquals(0, manager.getPlaylists().size());
    }

    /**
     * Testa la gestione in sicurezza della rimozione quando viene
     * passato un parametro nullo (simulando nessun elemento selezionato nella UI).
     */
    @Test
    void testRemoveNullPlaylist() {
        Playlist p = manager.createPlaylist("Synthwave");
        
        // Il controller dovrebbe ignorare la chiamata senza lanciare eccezioni o crashare
        assertDoesNotThrow(() -> {
            controller.removePlaylist(null);
        });
        
        assertEquals(1, manager.getPlaylists().size());
    }

    /**
     * Testa il recupero della lista di tracce (US8) per popolare la vista.
     */
    @Test
    void testGetPlaylistTracksViaController() {
        Playlist p = manager.createPlaylist("Workout");
        Track t1 = new Track("Stronger", "Kanye West", "Rap", 2007, null, 312);
        p.addTrack(t1);
        
        List<Track> tracks = controller.getPlaylistTracks(p);
        
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        assertEquals("Stronger", tracks.get(0).getTitle());
    }

    /**
     * Testa il recupero delle tracce quando la playlist fornita è nulla.
     * Verifica che restituisca null o gestisca il caso senza eccezioni non previste.
     */
    @Test
    void testGetTracksFromNullPlaylist() {
        List<Track> tracks = controller.getPlaylistTracks(null);
        
        assertNotNull(tracks);
        assertTrue(tracks.isEmpty());
    }
}

