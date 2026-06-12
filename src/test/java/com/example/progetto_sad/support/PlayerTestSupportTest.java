package com.example.progetto_sad.support;

import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.Player.PlayerState;
import com.example.progetto_sad.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// verifica che i doppi di test siano utilizzabili con il modello Player
class PlayerTestSupportTest {

    private FakeAudioPlayer audioPlayer;
    private Player player;

    @BeforeEach
    void setUp() {
        audioPlayer = new FakeAudioPlayer();
        player = Player.resetForTesting(audioPlayer);
    }

    @Test
    void fakeAudioPlayerRecordsLoadAndPlay() {
        Track track = PlayerTestFixtures.sampleTrack();

        player.play(track);

        assertEquals(track.getFilePath(), audioPlayer.getLoadedPath());
        assertTrue(audioPlayer.isPlaying());
        assertEquals(PlayerState.IN_RIPRODUZIONE, player.getState());
    }

    @Test
    void fakeAudioPlayerSimulatesEndOfTrack() {
        Track track = PlayerTestFixtures.trackWithDuration(120);
        player.play(track);

        audioPlayer.simulateEndOfTrack();

        assertEquals(PlayerState.FERMO, player.getState());
        assertEquals(0, player.getCurrentTime());
    }

    @Test
    void fakePlaybackClockAdvancesTime() {
        FakePlaybackClock clock = new FakePlaybackClock();
        clock.setCurrentTimeSeconds(30);
        clock.advance(15);

        assertEquals(45, clock.getCurrentTimeSeconds());
    }
}
