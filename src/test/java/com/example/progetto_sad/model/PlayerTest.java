/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.example.progetto_sad.model;

import com.example.progetto_sad.controller.PlaylistSequenceController;
import com.example.progetto_sad.support.FakeAudioPlayer;
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
public class PlayerTest {
    
    public PlayerTest() {
    }
    
 private Player player;
    private FakeAudioPlayer fakeAudioPlayer;
    private Track dummyTrack;

    @BeforeEach
    void setUp() {
        
        fakeAudioPlayer = new FakeAudioPlayer();
        player = new Player(fakeAudioPlayer);
        
        
        dummyTrack = new Track("Test Song", "Test Artist", "Pop", 2026, "dummy/path.mp3", 200);
    }

    /**
     * US11-T - Verifica che la pausa porti il Player nello stato "In Pausa",
     * blocchi il motore audio e mantenga invariata la posizione e la traccia corrente.
     */
    @Test
    void testPauseMaintainsStateAndTime() {
        
        player.play(dummyTrack);
        
        
        assertEquals(Player.PlayerState.IN_RIPRODUZIONE, player.getState());
        assertTrue(fakeAudioPlayer.isPlaying());
        assertEquals(dummyTrack, player.getCurrentTrack());

        
        int timeBeforePause = player.getCurrentTime();

        
        player.pause();

      
        assertEquals(Player.PlayerState.IN_PAUSA, player.getState());
        
        
        assertFalse(fakeAudioPlayer.isPlaying());
        
        
        assertEquals(timeBeforePause, player.getCurrentTime());
                
        
        assertEquals(dummyTrack, player.getCurrentTrack());
    }
    
    /**
     * US11-T - Verifica che la ripresa (resume) faccia ripartire la traccia
     * dallo stesso secondo, senza azzerare il tempo e mantenendo il flusso univoco.
     */
    @Test
    void testResumeRestartsFromPausePoint() {
        
        player.play(dummyTrack);
        player.pause();
        
        
        assertEquals(Player.PlayerState.IN_PAUSA, player.getState());
        
       
        int timeAtPause = player.getCurrentTime();

        
        player.resume();

        
        assertEquals(Player.PlayerState.IN_RIPRODUZIONE, player.getState());
        
        
        assertTrue(fakeAudioPlayer.isPlaying());
        
        
        assertEquals(timeAtPause, player.getCurrentTime());
        
        assertEquals(dummyTrack, player.getCurrentTrack());
    }
    
    /**
     * US11-T - Verifica che lo stato "In Pausa", la traccia corrente e il secondo corrente
     * restino invariati durante interazioni esterne o tempi morti, simulando il cambio
     * schermata in cui il Player non riceve comandi diretti.
     */
    @Test
    void testPauseStatePersistsDuringExternalInteractions() {
        
        player.play(dummyTrack);
        player.pause();
        
        Player.PlayerState stateAtPause = player.getState();
        Track trackAtPause = player.getCurrentTrack();
        int timeAtPause = player.getCurrentTime();

        assertEquals(Player.PlayerState.IN_PAUSA, stateAtPause);
        
        PlaylistSequenceController externalQueue = new PlaylistSequenceController();
        Track externalTrack = new Track("Other Track", "Other Artist", "Rock", 2020, "path2", 150);
        externalQueue.addToQueue(externalTrack);
        
        assertEquals(stateAtPause, player.getState());
        
        assertEquals(timeAtPause, player.getCurrentTime());
        
        assertEquals(trackAtPause, player.getCurrentTrack());

        assertFalse(fakeAudioPlayer.isPlaying());
    }

}
