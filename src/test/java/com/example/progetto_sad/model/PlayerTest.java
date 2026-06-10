package com.example.progetto_sad.model;

import com.example.progetto_sad.model.Player.PlayerState;
import com.example.progetto_sad.audio.AudioPlayer;
import com.example.progetto_sad.support.FakeAudioPlayer;
import com.example.progetto_sad.support.PlayerTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US9-T - test del Player su caricamento, avvio, cambio traccia, reset e fine brano.
class PlayerTest {

    private FakeAudioPlayer audioPlayer;
    private Player player;

    @BeforeEach
    void setUp() {
        audioPlayer = new FakeAudioPlayer();
        player = new Player(audioPlayer);
    }

    @Test
    void loadImpostaTracciaCorrenteDurataTempoZeroEStatoFermo() {
        Track track = PlayerTestFixtures.sampleTrack();

        player.load(track);

        assertEquals(track, player.getCurrentTrack());
        assertEquals(track.getDuration(), player.getDuration());
        assertEquals(0, player.getCurrentTime());
        assertEquals(PlayerState.FERMO, player.getState());
        assertFalse(audioPlayer.isPlaying());
    }

    @Test
    void loadRifiutaTracciaNullSenzaModificareLoStato() {
        Track track = PlayerTestFixtures.sampleTrack();
        player.load(track);

        assertThrows(IllegalArgumentException.class, () -> player.load(null));

        assertEquals(track, player.getCurrentTrack());
        assertEquals(0, player.getCurrentTime());
        assertEquals(PlayerState.FERMO, player.getState());
    }

    @Test
    void playConTracciaValidaCaricaIlFileEAvviaLaRiproduzione() {
        Track track = PlayerTestFixtures.sampleTrack();

        player.play(track);

        assertEquals(track, player.getCurrentTrack());
        assertEquals(track.getFilePath(), audioPlayer.getLoadedPath());
        assertTrue(audioPlayer.isPlaying());
        assertEquals(0, player.getCurrentTime());
        assertEquals(PlayerState.IN_RIPRODUZIONE, player.getState());
    }

    @Test
    void playConTracciaNullNonAvviaRiproduzione() {
        player.play(null);

        assertNull(player.getCurrentTrack());
        assertNull(audioPlayer.getLoadedPath());
        assertFalse(audioPlayer.isPlaying());
        assertEquals(PlayerState.FERMO, player.getState());
    }

    @Test
    void playConDurataNonValidaNonAvviaRiproduzione() {
        Track invalidTrack = PlayerTestFixtures.trackWithDuration(0);

        player.play(invalidTrack);

        assertNull(player.getCurrentTrack());
        assertNull(audioPlayer.getLoadedPath());
        assertFalse(audioPlayer.isPlaying());
        assertEquals(PlayerState.FERMO, player.getState());
    }

    @Test
    void playConFilePathVuotoNonAvviaRiproduzione() {
        Track invalidTrack = new Track("Brano", "Artista", "Pop", 2024, " ", 120);

        player.play(invalidTrack);

        assertNull(player.getCurrentTrack());
        assertNull(audioPlayer.getLoadedPath());
        assertFalse(audioPlayer.isPlaying());
        assertEquals(PlayerState.FERMO, player.getState());
    }

    @Test
    void playDiNuovaTracciaFermaLaPrecedenteECaricaIlNuovoFile() throws InterruptedException {
        Track firstTrack = PlayerTestFixtures.trackWithDuration(10);
        Track secondTrack = new Track("Secondo brano", "Altro artista", "Pop", 2024, "C:/test/secondo.mp3", 12);
        player.play(firstTrack);
        waitUntilClockAdvances();

        player.play(secondTrack);

        assertEquals(1, audioPlayer.getStopCalls());
        assertEquals(secondTrack, player.getCurrentTrack());
        assertEquals(secondTrack.getFilePath(), audioPlayer.getLoadedPath());
        assertTrue(audioPlayer.isPlaying());
        assertEquals(0, player.getCurrentTime());
        assertEquals(PlayerState.IN_RIPRODUZIONE, player.getState());
    }

    @Test
    void stopDuranteRiproduzioneArrestaAudioEResettaTempo() throws InterruptedException {
        Track track = PlayerTestFixtures.trackWithDuration(10);
        player.play(track);
        waitUntilClockAdvances();

        player.stop();

        assertEquals(1, audioPlayer.getStopCalls());
        assertEquals(track, player.getCurrentTrack());
        assertFalse(audioPlayer.isPlaying());
        assertEquals(0, player.getCurrentTime());
        assertEquals(PlayerState.FERMO, player.getState());
    }

    @Test
    void stopDaFermoMantieneStatoCoerente() {
        Track track = PlayerTestFixtures.sampleTrack();
        player.load(track);

        player.stop();

        assertEquals(0, audioPlayer.getStopCalls());
        assertEquals(track, player.getCurrentTrack());
        assertEquals(0, player.getCurrentTime());
        assertEquals(PlayerState.FERMO, player.getState());
    }

    @Test
    void motoreTempoAvanzaDuranteLaRiproduzioneSenzaSuperareLaDurata() throws InterruptedException {
        Track track = PlayerTestFixtures.trackWithDuration(3);

        player.play(track);
        waitUntilClockAdvances();

        assertTrue(player.getCurrentTime() > 0);
        assertTrue(player.getCurrentTime() <= track.getDuration());
        assertEquals(PlayerState.IN_RIPRODUZIONE, player.getState());
    }

    @Test
    void fineBranoDaMotoreAudioResettaTempoStatoENotificaCallback() {
        AtomicInteger completedTracks = new AtomicInteger(0);
        Track track = PlayerTestFixtures.trackWithDuration(10);
        player.setOnEndOfTrack(completedTracks::incrementAndGet);
        player.play(track);

        audioPlayer.simulateEndOfTrack();

        assertEquals(1, audioPlayer.getStopCalls());
        assertEquals(1, completedTracks.get());
        assertEquals(track, player.getCurrentTrack());
        assertFalse(audioPlayer.isPlaying());
        assertEquals(0, player.getCurrentTime());
        assertEquals(PlayerState.FERMO, player.getState());
    }

    @Test
    void fineBranoDaMotoreTempoFermaLaRiproduzioneEResettaTempo() throws InterruptedException {
        Track track = PlayerTestFixtures.trackWithDuration(1);

        player.play(track);
        waitUntilPlayerStops();

        assertEquals(1, audioPlayer.getStopCalls());
        assertEquals(track, player.getCurrentTrack());
        assertFalse(audioPlayer.isPlaying());
        assertEquals(0, player.getCurrentTime());
        assertEquals(PlayerState.FERMO, player.getState());
    }

    // US9-T - se il motore audio lancia all'avvio, il Player recupera uno stato stabile.
    @Test
    void playRecuperaStatoStabileSeIlMotoreAudioLanciaUnEccezione() {
        Player playerConMotoreInErrore = new Player(new MotoreCheLanciaAllAvvio());
        Track track = PlayerTestFixtures.sampleTrack();

        // l'Adapter lancia su play(): il Player deve intercettare e restare stabile
        assertDoesNotThrow(() -> playerConMotoreInErrore.play(track));

        assertEquals(track, playerConMotoreInErrore.getCurrentTrack());
        assertEquals(0, playerConMotoreInErrore.getCurrentTime());
        assertEquals(PlayerState.FERMO, playerConMotoreInErrore.getState());
    }

    private void waitUntilClockAdvances() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2500;
        while (System.currentTimeMillis() < deadline && player.getCurrentTime() < 1) {
            Thread.sleep(25);
        }
        assertTrue(player.getCurrentTime() >= 1);
    }

    private void waitUntilPlayerStops() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2500;
        while (System.currentTimeMillis() < deadline && player.getState() != PlayerState.FERMO) {
            Thread.sleep(25);
        }
        assertEquals(PlayerState.FERMO, player.getState());
    }

    // Doppio di AudioPlayer che simula un motore che fallisce all'avvio della riproduzione,
    // per verificare il ramo di recupero (try/catch) di Player.play().
    private static class MotoreCheLanciaAllAvvio implements AudioPlayer {
        @Override public void load(String filePath) { }
        @Override public void play() { throw new RuntimeException("motore audio non disponibile"); }
        @Override public void stop() { }
        @Override public void setOnEndOfTrack(Runnable onEndOfTrack) { }
    }
}
