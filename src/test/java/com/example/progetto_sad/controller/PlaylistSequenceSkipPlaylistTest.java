package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.support.QueueTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US13 - skip playlist e individuazione playlist corrente nella coda
class PlaylistSequenceSkipPlaylistTest {

    private PlaylistSequenceController controller;

    @BeforeEach
    void setUp() {
        QueueTestFixtures.resetCounter();
        controller = new PlaylistSequenceController();
    }

    // US13 - individuazione playlist corrente nel blocco sorgente
    @Test
    void identifiesCurrentPlaylistFromSourceSegment() {
        QueueTestFixtures.MixedQueueSetup setup = QueueTestFixtures.sourcePlaylistWithSingleAfter();

        assertEquals(setup.sourcePlaylist(), setup.controller().getCurrentPlaylist());
        assertTrue(setup.controller().isCurrentTrackInPlaylistSegment());
        assertEquals(0, setup.controller().getCurrentPlaylistSegmentStart());
        assertEquals(2, setup.controller().getCurrentPlaylistSegmentEnd());
    }

    // US13 - brano singolo accodato: nessuna playlist corrente
    @Test
    void singleQueuedTrackHasNoCurrentPlaylist() {
        QueueTestFixtures.MixedQueueSetup setup = QueueTestFixtures.sourcePlaylistWithSingleAfter();
        setup.controller().skipPlaylist();

        assertNull(setup.controller().getCurrentPlaylist());
        assertFalse(setup.controller().isCurrentTrackInPlaylistSegment());
        assertEquals(-1, setup.controller().getCurrentPlaylistSegmentStart());
    }

    // US13 - skip playlist salta i brani rimanenti e passa al successivo
    @Test
    void skipPlaylistJumpsToFirstTrackAfterCurrentPlaylistBlock() {
        QueueTestFixtures.MixedQueueSetup setup = QueueTestFixtures.sourcePlaylistWithSingleAfter();

        Track next = setup.controller().skipPlaylist();

        assertEquals(setup.singleTrack(), next);
        assertEquals(setup.singleTrack(), setup.controller().getCurrentTrack());
    }

    // US13 - coda mista: skip salta solo il blocco playlist corrente
    @Test
    void skipPlaylistInMixedQueueSkipsOnlyCurrentPlaylistBlock() {
        QueueTestFixtures.MixedQueueSetup setup = QueueTestFixtures.mixedQueueWithTail();
        setup.controller().goToTrack(setup.sourceTrack2());

        Track next = setup.controller().skipPlaylist();

        assertEquals(setup.singleTrack(), next);
        assertEquals(setup.singleTrack(), setup.controller().getCurrentTrack());
    }

    // US13 - skip dalla seconda playlist non tocca il brano singolo precedente
    @Test
    void skipSecondPlaylistDoesNotSkipUnrelatedSingleTracks() {
        QueueTestFixtures.MixedQueueSetup setup = QueueTestFixtures.mixedQueueWithTail();
        setup.controller().goToTrack(setup.secondPlaylistTrack1());

        assertEquals(setup.secondPlaylist(), setup.controller().getCurrentPlaylist());

        Track next = setup.controller().skipPlaylist();

        assertEquals(setup.tailSingle(), next);
    }

    // US13 - ultimo blocco in coda: skip non disponibile
    @Test
    void cannotSkipWhenCurrentBlockIsLastInQueue() {
        QueueTestFixtures.MixedQueueSetup setup = QueueTestFixtures.mixedQueueWithTail();
        setup.controller().goToTrack(setup.tailSingle());

        assertFalse(setup.controller().canSkipPlaylist());
        assertNull(setup.controller().skipPlaylist());
    }

    // US13 - coda vuota / sequenza assente
    @Test
    void skipPlaylistOnEmptyQueueIsStable() {
        assertFalse(controller.canSkipPlaylist());
        assertNull(controller.skipPlaylist());
        assertDoesNotThrow(() -> controller.skipPlaylist());
    }

    // US13 - playlist con un solo brano e elemento dopo
    @Test
    void skipSingleTrackPlaylistWithTail() {
        Track only = QueueTestFixtures.track("Only", 100);
        Track after = QueueTestFixtures.track("After", 110);
        Playlist single = QueueTestFixtures.singleTrackPlaylist("One Song", only);

        controller.startPlaylist(single);
        controller.addToQueue(after);

        assertTrue(controller.canSkipPlaylist());
        assertEquals(after, controller.skipPlaylist());
    }

    // US13 - playlist corrente assente durante riproduzione di brano singolo
    @Test
    void cannotSkipWhenPlayingSingleTrackOnly() {
        Track single = QueueTestFixtures.track("Lonely", 120);
        controller.addToQueue(single);

        assertFalse(controller.isCurrentTrackInPlaylistSegment());
        assertFalse(controller.canSkipPlaylist());
        assertNull(controller.skipPlaylist());
    }

    // US13 - dopo skip la sequenza termina se non ci sono altri brani
    @Test
    void skipPlaylistFinishesWhenNoTailExists() {
        Track a1 = QueueTestFixtures.track("A1", 100);
        Track a2 = QueueTestFixtures.track("A2", 110);
        Playlist playlist = QueueTestFixtures.playlist("Short", a1, a2);
        controller.startPlaylist(playlist);
        controller.goToTrack(a2);

        assertFalse(controller.canSkipPlaylist());
        assertNull(controller.skipPlaylist());
    }

    // US13 - playlist vuota all'avvio: nessun crash
    @Test
    void emptyPlaylistStartDoesNotBreakSkipLogic() {
        Playlist empty = QueueTestFixtures.emptyPlaylist("Empty");
        controller.startPlaylist(empty);

        assertFalse(controller.canSkipPlaylist());
        assertNull(controller.getCurrentPlaylist());
        assertDoesNotThrow(() -> controller.skipPlaylist());
    }
}
