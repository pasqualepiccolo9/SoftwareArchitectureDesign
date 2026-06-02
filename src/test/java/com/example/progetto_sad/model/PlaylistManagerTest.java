/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.example.progetto_sad.model;

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
}
