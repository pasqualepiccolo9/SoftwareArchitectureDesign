
package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.PlaylistSequence;
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

    @Test
    void contextAllowsSwitchingBetweenAllStrategies() {
        PlayModeContext context = new PlayModeContext(new SequentialModeStrategy());

        context.setStrategy(new ShuffleModeStrategy());
        assertTrue(context.getStrategy() instanceof ShuffleModeStrategy);

        context.setStrategy(new LoopModeStrategy());
        assertTrue(context.getStrategy() instanceof LoopModeStrategy);

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

    @Test
    void settingNullStrategyThrowsException() {
        PlayModeContext context = new PlayModeContext(new SequentialModeStrategy());

        assertThrows(IllegalArgumentException.class, () -> context.setStrategy(null));
    }

    // US17-T - la modalità esposta dal Context è quella dichiarata dalla strategia collegata
    @Test
    void modeAlwaysReflectsTheActiveStrategy() {
        PlayModeContext context = new PlayModeContext(new SequentialModeStrategy());
        assertEquals(PlayMode.SEQUENTIAL, context.getMode());

        context.setStrategy(new ShuffleModeStrategy());
        assertEquals(PlayMode.SHUFFLE, context.getMode());

        context.setStrategy(new LoopModeStrategy());
        assertEquals(PlayMode.LOOP, context.getMode());
    }

    // US17-T - il Context comunica con la strategia solo tramite l'interfaccia:
    // funziona anche con una strategia concreta che non conosce.
    @Test
    void contextDelegatesEveryOperationThroughTheStrategyInterface() {
        RecordingStrategy custom = new RecordingStrategy(PlayMode.SHUFFLE, 0);
        PlayModeContext context = new PlayModeContext(custom);
        PlaylistSequence sequence = new PlaylistSequence(tracks);

        assertEquals(PlayMode.SHUFFLE, context.getMode());
        assertTrue(context.moveToNextTrack(sequence));
        assertTrue(context.hasNextTrack(sequence));
        assertEquals(1, custom.moveRequests);
    }

    // US19-T - attivando una strategia le si lascia preparare la coda alla propria modalità
    @Test
    void activateLetsTheNewStrategyPrepareTheQueue() {
        RecordingStrategy custom = new RecordingStrategy(PlayMode.LOOP, 2);
        PlayModeContext context = new PlayModeContext(new SequentialModeStrategy());
        PlaylistSequence sequence = new PlaylistSequence(tracks);

        int discarded = context.activate(custom, sequence);

        assertEquals(2, discarded);
        assertEquals(1, custom.activations);
        assertEquals(PlayMode.LOOP, context.getMode());
    }

    // US19-T - senza coda attiva la strategia viene collegata ma non prepara nulla
    @Test
    void activateWithoutSequenceLinksStrategyWithoutPreparingQueue() {
        RecordingStrategy custom = new RecordingStrategy(PlayMode.LOOP, 2);
        PlayModeContext context = new PlayModeContext(new SequentialModeStrategy());

        assertEquals(0, context.activate(custom, null));

        assertSame(custom, context.getStrategy());
        assertEquals(0, custom.activations);
    }

    /**
     * ConcreteStrategy di prova: registra le chiamate ricevute dal Context per
     * verificare che la delega avvenga solo attraverso {@link PlayModeStrategy}.
     */
    private static final class RecordingStrategy implements PlayModeStrategy {

        private final PlayMode mode;
        private final int tracksDiscardedOnActivation;
        private int moveRequests;
        private int activations;

        RecordingStrategy(PlayMode mode, int tracksDiscardedOnActivation) {
            this.mode = mode;
            this.tracksDiscardedOnActivation = tracksDiscardedOnActivation;
        }

        @Override
        public PlayMode getMode() {
            return mode;
        }

        @Override
        public Track getNextTrack(List<Track> tracks, int currentIndex) {
            return null;
        }

        @Override
        public boolean moveToNextTrack(PlaylistSequence sequence) {
            moveRequests++;
            return true;
        }

        @Override
        public boolean hasNextTrack(PlaylistSequence sequence) {
            return true;
        }

        @Override
        public int onActivated(PlaylistSequence sequence) {
            activations++;
            return tracksDiscardedOnActivation;
        }
    }
}
