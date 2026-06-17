/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.Track;
import java.util.ArrayList;
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
 * US17-T - Test JUnit su SequentialModeStrategy.
 * Verifica che la riproduzione sequenziale scorra l'elenco nell'ordine
 * naturale, si fermi alla fine e non alteri mai la struttura dati originale.
 */
public class SequentialModeStrategyTest {
    
   private SequentialModeStrategy strategy;
    private Track track1;
    private Track track2;
    private Track track3;
    private List<Track> tracks;

    @BeforeEach
    void setUp() {
        strategy = new SequentialModeStrategy();
        track1 = new Track("Song A", "Artist A", "Pop", 2020, null, 180);
        track2 = new Track("Song B", "Artist B", "Rock", 2021, null, 200);
        track3 = new Track("Song C", "Artist C", "Jazz", 2022, null, 240);
        tracks = new ArrayList<>(List.of(track1, track2, track3));
    }


    @Test
    void getNextTrackReturnsNextElementInOrder() {
        assertEquals(track2, strategy.getNextTrack(tracks, 0));
        assertEquals(track3, strategy.getNextTrack(tracks, 1));
    }

    @Test
    void getNextTrackReturnsNullWhenAtLastElement() {
        assertNull(strategy.getNextTrack(tracks, 2));
    }

 
    @Test
    void getNextTrackDoesNotAlterOriginalList() {
        List<Track> originalCopy = new ArrayList<>(tracks);
        
        strategy.getNextTrack(tracks, 0);
        strategy.getNextTrack(tracks, 1);
        strategy.getNextTrack(tracks, 2);
        
        assertEquals(originalCopy, tracks);
    }


    @Test
    void getNextTrackReturnsNullForEmptyList() {
        assertNull(strategy.getNextTrack(new ArrayList<>(), 0));
    }

    @Test
    void getNextTrackReturnsNullForNullList() {
        assertNull(strategy.getNextTrack(null, 0));
    }

  
    @Test
    void getNextTrackReturnsNullForIndexOutOfBounds() {
        assertNull(strategy.getNextTrack(tracks, 5));
    }

    
    @Test
    void getNextTrackReturnsFirstElementWhenIndexIsNegativeOne() {
        assertEquals(track1, strategy.getNextTrack(tracks, -1));
    }
    
}
