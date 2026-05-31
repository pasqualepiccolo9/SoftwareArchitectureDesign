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
public class PlaylistManagerTest {
    
    private PlaylistManager manager;
    
@BeforeEach
    void setUp() {
        
        manager = new PlaylistManager();
    }

    @Test
    void testCreazionePlaylistValida() {
        Playlist p = manager.createPlaylist("Rock Classics");
        
        // Verifica che la playlist sia stata creata
        assertNotNull(p);
        assertEquals("Rock Classics", p.getName());
        
        // Verifica che il manager l'abbia effettivamente salvata nella sua lista
        assertEquals(1, manager.getPlaylists().size());
        assertEquals("Rock Classics", manager.getPlaylists().get(0).getName());
    }

    @Test
    void testCreazionePlaylistNomeVuoto() {
        // Verifica che il manager (tramite la classe Playlist) blocchi i nomi vuoti
        assertThrows(IllegalArgumentException.class, () -> {
            manager.createPlaylist("");
        });
        
        // Verifica che la lista sia rimasta a 0
        assertEquals(0, manager.getPlaylists().size());
    }

}
