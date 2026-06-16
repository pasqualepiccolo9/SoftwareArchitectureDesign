
package com.example.progetto_sad.strategy;

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
 * US17-T - Test JUnit su cambio strategia attiva.
 * Verifica che il contesto Strategy gestisca correttamente i cambi di stato
 * tra le varie modalità di riproduzione senza generare incoerenze.
 */
public class PlayModeContextTest {
    
    private Track track1;
    private Track track2;
    private Track track3;
    private List<Track> tracks;

    @BeforeEach
    void setUp() {
        track1 = new Track("Song A", "Artist A", "Pop", 2020, null, 180);
        track2 = new Track("Song B", "Artist B", "Rock", 2021, null, 200);
        track3 = new Track("Song C", "Artist C", "Jazz", 2022, null, 240);
        tracks = List.of(track1, track2, track3);
    }

    
    @Test
    void contextInitializesWithProvidedStrategy() {
        PlayModeContext context = new PlayModeContext(new SequentialModeStrategy());
        
        assertTrue(context.getStrategy() instanceof SequentialModeStrategy);
    }

    // US17-T: Verifica il cambio tra tutte le strategie disponibili
    @Test
    void contextAllowsSwitchingBetweenAllStrategies() {
        PlayModeContext context = new PlayModeContext(new SequentialModeStrategy());

        // 1. Passaggio a Shuffle
        context.setStrategy(new ShuffleModeStrategy());
        assertTrue(context.getStrategy() instanceof ShuffleModeStrategy);

        // 2. Passaggio a Loop
        context.setStrategy(new LoopModeStrategy());
        assertTrue(context.getStrategy() instanceof LoopModeStrategy);

        // 3. Ritorno a Sequenziale
        context.setStrategy(new SequentialModeStrategy());
        assertTrue(context.getStrategy() instanceof SequentialModeStrategy);
    }

    // US17-T: Verifica coerenza di stato (nessun crash durante il cambio)
    @Test
    void switchingStrategiesDoesNotProduceInconsistentStates() {
        PlayModeContext context = new PlayModeContext(new SequentialModeStrategy());
        
        assertDoesNotThrow(() -> context.getNextTrack(tracks, 0));
                
        context.setStrategy(new LoopModeStrategy());
        
        assertDoesNotThrow(() -> context.getNextTrack(tracks, 2));
                
        assertEquals(track1, context.getNextTrack(tracks, 2));
    }

    // Sicurezza architetturale: proteggere il context da stati nulli
    @Test
    void settingNullStrategyThrowsException() {
        PlayModeContext context = new PlayModeContext(new SequentialModeStrategy());
        
        assertThrows(IllegalArgumentException.class, () -> context.setStrategy(null));
    }
}
