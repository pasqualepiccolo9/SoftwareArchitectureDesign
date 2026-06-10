package com.example.progetto_sad.model;

import com.example.progetto_sad.model.Player.PlayerState;
import com.example.progetto_sad.support.FakeAudioPlayer;
import com.example.progetto_sad.support.PlayerTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US9-T - test del Player su caricamento, avvio, cambio traccia e reset.
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

        //assertEquals(1, audioPlayer.getStopCalls());
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

        //assertEquals(1, audioPlayer.getStopCalls());
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

        //assertEquals(0, audioPlayer.getStopCalls());
        assertEquals(track, player.getCurrentTrack());
        assertEquals(0, player.getCurrentTime());
        assertEquals(PlayerState.FERMO, player.getState());
    }

    private void waitUntilClockAdvances() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2500;
        while (System.currentTimeMillis() < deadline && player.getCurrentTime() < 1) {
            Thread.sleep(25);
        }
        assertTrue(player.getCurrentTime() >= 1);
    }
    
    /* ===== COPERTURA AGGIUNTIVA USER STORY: [US11-T] ===== */

    @Test
    void pausaImpostaStatoInPausaArrestaAudioEMantieneTempoInvariato() throws InterruptedException {
        
        Track track = PlayerTestFixtures.trackWithDuration(10);
        player.play(track);
        waitUntilClockAdvances();
        int tempoPrimaDellaPausa = player.getCurrentTime();

        
        player.pause();

        
        assertEquals(PlayerState.IN_PAUSA, player.getState());
        assertFalse(audioPlayer.isPlaying());
        assertEquals(tempoPrimaDellaPausa, player.getCurrentTime());
        assertEquals(track, player.getCurrentTrack());
    }

    @Test
    void ripresaRiparteDalPuntoDiPausaSenzaAzzerareIlTempo() throws InterruptedException {
        
        Track track = PlayerTestFixtures.trackWithDuration(10);
        player.play(track);
        waitUntilClockAdvances();
        player.pause();
        int tempoAlMomentoDellaPausa = player.getCurrentTime();

        
        player.resume();

        
        assertEquals(PlayerState.IN_RIPRODUZIONE, player.getState());
        assertTrue(audioPlayer.isPlaying());
        assertEquals(tempoAlMomentoDellaPausa, player.getCurrentTime());
        assertEquals(track, player.getCurrentTrack());
    }

    @Test
    void statoPausaETimelineRestanoInvariatiDuranteNavigazioneDellUtente() {
        
        Track track = PlayerTestFixtures.sampleTrack();
        player.play(track);
        player.pause();
        int tempoAlMomentoDellaPausa = player.getCurrentTime();


        TrackLibrary libreriaIsolata = new TrackLibrary();
        PlaylistManager playlistManagerIsolato = new PlaylistManager();
        libreriaIsolata.addTrack(new Track("Nav Track", "Nav Artist", "Pop", 2026, "nav/path.mp3", 120));
        playlistManagerIsolato.createPlaylist("Nav Playlist");


        assertEquals(PlayerState.IN_PAUSA, player.getState());
        assertEquals(tempoAlMomentoDellaPausa, player.getCurrentTime());
        assertEquals(track, player.getCurrentTrack());
        assertFalse(audioPlayer.isPlaying());
    }
}
