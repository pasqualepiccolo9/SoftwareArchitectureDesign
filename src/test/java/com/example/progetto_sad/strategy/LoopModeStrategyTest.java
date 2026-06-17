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
 * US19-T - Test JUnit su LoopModeStrategy.
 * Verifica che la riproduzione in loop avanzi correttamente e, 
 * una volta terminata la coda, riparta dal primo brano.
 */
public class LoopModeStrategyTest {
    
   private LoopModeStrategy strategy;
    private Track track1;
    private Track track2;
    private Track track3;
    private List<Track> tracks;

    @BeforeEach
    void setUp() {
        strategy = new LoopModeStrategy();
        track1 = new Track("Song A", "Artist A", "Pop", 2020, null, 180);
        track2 = new Track("Song B", "Artist B", "Rock", 2021, null, 200);
        track3 = new Track("Song C", "Artist C", "Jazz", 2022, null, 240);
        tracks = new ArrayList<>(List.of(track1, track2, track3));
    }

    @Test
    void getNextTrackAdvancesNormallyBeforeEnd() {
        assertEquals(track2, strategy.getNextTrack(tracks, 0));
        assertEquals(track3, strategy.getNextTrack(tracks, 1));
    }

    @Test
    void getNextTrackLoopsToFirstTrackWhenAtEnd() {
        assertEquals(track1, strategy.getNextTrack(tracks, 2));
    }

    @Test
    void getNextTrackHandlesSingleTrackListCorrectly() {
        List<Track> singleTrackList = List.of(track1);
        
        assertEquals(track1, strategy.getNextTrack(singleTrackList, 0));
    }


    @Test
    void getNextTrackDoesNotAlterOriginalList() {
        List<Track> originalCopy = new ArrayList<>(tracks);
        
        strategy.getNextTrack(tracks, 0);
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
    
}
