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
public class PlaylistTest {
    
   @Test
   void testCreazionePlaylistValida() {
        PlaylistManager manager = new PlaylistManager();
        Playlist p = manager.createPlaylist("Rock Classics");
        assertEquals("Rock Classics", p.getName());
        assertEquals(1, manager.getPlaylists().size());
    }
   
   @Test
   void testCreazionePlaylistNomeInvalido() {
       PlaylistManager manager = new PlaylistManager();
        assertThrows(IllegalArgumentException.class, () -> manager.createPlaylist(""));
        assertThrows(IllegalArgumentException.class, () -> manager.createPlaylist("   "));
        assertThrows(IllegalArgumentException.class, () -> manager.createPlaylist(null));
    }
   
}
