/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.example.progetto_sad.model;

import com.example.progetto_sad.observer.Observer;
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
 * Classe di test per verificare la logica interna della singola Playlist.
 * Copre l'aggiunta e la rimozione delle tracce, oltre alla gestione dei duplicati (US6 e US7).
 */
public class PlaylistTest {
    
    private Playlist playlist;
    private Track dummyTrack1;
    private Track dummyTrack2;
    /**
     * Metodo di setup eseguito prima di ogni test.
     * Inizializza la playlist e crea alcune tracce fittizie da usare nei test.
     */
    @BeforeEach
    void setUp() {
        playlist = new Playlist("My Favorites");
        dummyTrack1 = new Track("Bohemian Rhapsody", "Queen", 354, "Rock", 1975);
        dummyTrack2 = new Track("Shape of You", "Ed Sheeran", 233, "Pop", 2017);
    }
    
    /**
     * Testa l'aggiunta corretta di una traccia valida alla playlist.
     */
    @Test
    void testValidTrackAddition() {
        playlist.addTrack(dummyTrack1);
        
        assertEquals(1, playlist.getTracks().size());
        assertTrue(playlist.getTracks().contains(dummyTrack1));
    }

    /**
     * Testa il blocco dell'inserimento se si tenta di aggiungere
     * una traccia già presente (evita i duplicati).
     */
    @Test
    void testDuplicateTrackAddition() {
        playlist.addTrack(dummyTrack1);
        
        assertThrows(IllegalArgumentException.class, () -> {
            playlist.addTrack(dummyTrack1);
        });
        
        assertEquals(1, playlist.getTracks().size());
    }

    /**
     * Testa il blocco dell'inserimento se si passa un oggetto traccia nullo.
     */
    @Test
    void testNullTrackAddition() {
        assertThrows(IllegalArgumentException.class, () -> {
            playlist.addTrack(null);
        });
    }

    /**
     * Testa la corretta rimozione di una traccia esistente nella playlist.
     */
    @Test
    void testValidTrackRemoval() {
        playlist.addTrack(dummyTrack1);
        playlist.addTrack(dummyTrack2);
        assertEquals(2, playlist.getTracks().size());

        playlist.removeTrack(dummyTrack1);
        
        assertEquals(1, playlist.getTracks().size());
        assertFalse(playlist.getTracks().contains(dummyTrack1));
        assertTrue(playlist.getTracks().contains(dummyTrack2));
    }

    /**
     * Testa il blocco della rimozione se la playlist è completamente vuota.
     */
    @Test
    void testRemoveFromEmptyPlaylist() {
        assertThrows(IllegalStateException.class, () -> {
            playlist.removeTrack(dummyTrack1);
        });
    }

    /**
     * Testa il blocco della rimozione se la traccia specificata non fa
     * parte della playlist corrente.
     */
    @Test
    void testRemoveNonExistentTrack() {
        playlist.addTrack(dummyTrack1); // Aggiungo solo la traccia 1
        
        assertThrows(IllegalArgumentException.class, () -> {
            playlist.removeTrack(dummyTrack2);}); // Tento di rimuovere la traccia 2
        
    }
}

