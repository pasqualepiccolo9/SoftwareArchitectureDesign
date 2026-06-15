package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.support.QueueTestFixtures;
import com.example.progetto_sad.support.ShuffleTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US18-INT - integrazione shuffle con PlaylistSequenceController
class PlaylistSequenceShuffleIntegrationTest {

    @BeforeEach
    void setUp() {
        ShuffleTestFixtures.resetCounter();
        QueueTestFixtures.resetCounter();
    }

    // US18-INT - onTrackFinished usa la strategia shuffle collegata al controller
    @Test
    void onTrackFinishedUsesShuffleStrategy() {
        List<Track> tracks = ShuffleTestFixtures.trackList(4);
        PlaylistSequenceController controller = new PlaylistSequenceController();
        controller.startPlaylist(QueueTestFixtures.playlist("Shuffle List",
                tracks.get(0), tracks.get(1), tracks.get(2), tracks.get(3)));
        controller.setShuffleMode();

        Track first = controller.getCurrentTrack();
        controller.onTrackFinished();
        Track second = controller.getCurrentTrack();

        assertNotNull(first);
        assertNotNull(second);
        assertTrue(tracks.contains(second));
    }

    // US18-INT - cambio coda resetta lo shuffle tramite addToQueue
    @Test
    void addingToQueueResetsShuffleCycle() {
        List<Track> tracks = ShuffleTestFixtures.trackList(3);
        PlaylistSequenceController controller = new PlaylistSequenceController();
        controller.startPlaylist(QueueTestFixtures.playlist("Base",
                tracks.get(0), tracks.get(1), tracks.get(2)));
        controller.setShuffleMode();

        Track extra = ShuffleTestFixtures.track("Extra", 140);
        controller.addToQueue(extra);

        controller.onTrackFinished();
        Track next = controller.getCurrentTrack();

        assertNotNull(next);
        assertTrue(controller.getSequence().getTracks().contains(next));
    }

    // US18-INT - setShuffleMode seleziona solo tracce presenti in coda
    @Test
    void shuffleModeSelectsOnlyExistingTracks() {
        PlaylistSequenceController controller = new PlaylistSequenceController();
        List<Track> tracks = ShuffleTestFixtures.trackList(3);
        controller.startPlaylist(QueueTestFixtures.playlist("S", tracks.get(0), tracks.get(1), tracks.get(2)));
        controller.setShuffleMode();

        Set<Track> visited = new HashSet<>();
        visited.add(controller.getCurrentTrack());
        for (int i = 0; i < 2; i++) {
            controller.onTrackFinished();
            Track current = controller.getCurrentTrack();
            assertNotNull(current);
            visited.add(current);
        }
        assertEquals(3, visited.size());
    }

    // US18-INT - cambio a sequenziale durante riproduzione senza eccezioni
    @Test
    void switchingToSequentialDuringPlaybackIsStable() {
        List<Track> tracks = ShuffleTestFixtures.trackList(3);
        PlaylistSequenceController controller = new PlaylistSequenceController();
        controller.startPlaylist(QueueTestFixtures.playlist("Switch",
                tracks.get(0), tracks.get(1), tracks.get(2)));
        controller.setShuffleMode();
        controller.onTrackFinished();
        controller.setSequentialMode();
        controller.onTrackFinished();

        assertNotNull(controller.getCurrentTrack());
    }
}
