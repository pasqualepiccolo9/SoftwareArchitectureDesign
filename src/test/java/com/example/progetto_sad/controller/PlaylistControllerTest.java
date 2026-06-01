/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
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
public class PlaylistControllerTest {
    
    public PlaylistControllerTest() {
    }
    
   @Test
    void testAggiuntaTracciaValida() {
        Playlist p = new Playlist("Pop");
        Track t = new Track("Title", "Author", 180, "Pop", 2026);
        p.addTrack(t);
        assertTrue(p.getTracks().contains(t));
    }

    @Test
    void testAggiuntaTracciaDuplicataONulla() {
        Playlist p = new Playlist("Pop");
        Track t = new Track("Title", "Author", 180, "Pop", 2026);
        p.addTrack(t);
        
        // T6.3: Verifica blocco duplicati
        assertThrows(IllegalArgumentException.class, () -> p.addTrack(t));
        // T6.3: Verifica blocco selezione non valida
        assertThrows(IllegalArgumentException.class, () -> p.addTrack(null));
    }
    
    @Test
    void testRimozioneTracciaValida() {
        Playlist p = new Playlist("Jazz");
        Track t = new Track("Title", "Author", 240,"Jazz", 2026);
        p.addTrack(t);
        p.removeTrack(t);
        assertTrue(p.getTracks().isEmpty());
    }

    @Test
    void testRimozionePlaylistVuotaOAssente() {
        Playlist p = new Playlist("Jazz");
        Track t = new Track("Title", "Author",240, "Jazz", 2026);
        
        // T7.3: Errore playlist vuota
        assertThrows(IllegalStateException.class, () -> p.removeTrack(t));
        
        p.addTrack(t);
        Track t2 = new Track("Other", "Author",200, "Jazz", 2026);
        
        // T7.3: Errore traccia assente
        assertThrows(IllegalArgumentException.class, () -> p.removeTrack(t2));
    }
}
