/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.PlaylistSequence;
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

    
    // US17-T - contratto comune alle tre strategie: un indice fuori da [0, size) non
    // individua alcuna posizione valida nella coda, quindi non esiste un brano successivo.
    @Test
    void getNextTrackReturnsNullForNegativeIndex() {
        assertNull(strategy.getNextTrack(tracks, -1));
    }

    // US17-T - la strategia dichiara la propria modalità: nessun controllo di tipo altrove
    @Test
    void modeIdentifiesSequentialPlayback() {
        assertEquals(PlayMode.SEQUENTIAL, strategy.getMode());
    }

    // US17-T - l'avanzamento sequenziale sposta la sequenza senza riordinare la coda
    @Test
    void moveToNextTrackAdvancesKeepingQueueOrder() {
        PlaylistSequence sequence = new PlaylistSequence(tracks);

        assertTrue(strategy.moveToNextTrack(sequence));

        assertEquals(track2, sequence.getCurrentTrack());
        assertEquals(List.of(track1, track2, track3), sequence.getTracks());
    }

    // US17-T - sull'ultimo brano segnala l'assenza di successivi senza terminare la sequenza
    @Test
    void moveToNextTrackReturnsFalseOnLastTrackWithoutFinishingSequence() {
        PlaylistSequence sequence = new PlaylistSequence(tracks);
        sequence.moveToIndex(2);

        assertFalse(strategy.moveToNextTrack(sequence));

        assertEquals(track3, sequence.getCurrentTrack());
        assertFalse(sequence.isFinished());
    }

    // US17-T - hasNextTrack segue la posizione nella coda
    @Test
    void hasNextTrackFollowsQueuePosition() {
        PlaylistSequence sequence = new PlaylistSequence(tracks);

        assertTrue(strategy.hasNextTrack(sequence));

        sequence.moveToIndex(2);

        assertFalse(strategy.hasNextTrack(sequence));
    }
}
