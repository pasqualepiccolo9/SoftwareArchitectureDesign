package com.example.progetto_sad.model;

import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.support.FakeAudioPlayer;
import com.example.progetto_sad.support.PlayerTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US21-T - test del Player come Subject del pattern Observer (notifiche di stato).
class PlayerObserverTest {

    private FakeAudioPlayer audioPlayer;
    private Player player;

    @BeforeEach
    void setUp() {
        audioPlayer = new FakeAudioPlayer();
        player = new Player(audioPlayer);
    }

    // US21-T - un observer registrato viene notificato al caricamento di una traccia.
    @Test
    void observerRegistratoVieneNotificatoAlCaricamento() {
        AtomicInteger notifiche = new AtomicInteger(0);
        player.attach(notifiche::incrementAndGet);

        player.load(PlayerTestFixtures.sampleTrack());

        assertEquals(1, notifiche.get());
    }

    // US21-T - un observer registrato viene notificato all'avvio della riproduzione.
    @Test
    void observerRegistratoVieneNotificatoAllAvvio() {
        AtomicInteger notifiche = new AtomicInteger(0);
        player.attach(notifiche::incrementAndGet);

        player.play(PlayerTestFixtures.sampleTrack());

        assertTrue(notifiche.get() > 0);
    }

    // US21-T - un observer registrato viene notificato allo stop.
    @Test
    void observerRegistratoVieneNotificatoAlloStop() {
        player.play(PlayerTestFixtures.sampleTrack());
        AtomicInteger notifiche = new AtomicInteger(0);
        player.attach(notifiche::incrementAndGet);

        player.stop();

        assertEquals(1, notifiche.get());
    }

    // US21-T - un observer registrato viene notificato alla fine naturale del brano.
    @Test
    void observerRegistratoVieneNotificatoAllaFineNaturaleDelBrano() {
        player.play(PlayerTestFixtures.sampleTrack());
        AtomicInteger notifiche = new AtomicInteger(0);
        player.attach(notifiche::incrementAndGet);

        audioPlayer.simulateEndOfTrack();

        assertEquals(1, notifiche.get());
    }

    // US21-T - dopo detach() l'observer non riceve piu' notifiche.
    @Test
    void observerStaccatoNonRicevePiuNotifiche() {
        AtomicInteger notifiche = new AtomicInteger(0);
        Observer observer = notifiche::incrementAndGet;
        player.attach(observer);
        player.detach(observer);

        player.load(PlayerTestFixtures.sampleTrack());

        assertEquals(0, notifiche.get());
    }

    // US21-T - un observer null viene ignorato senza generare errori.
    @Test
    void observerNullVieneIgnoratoSenzaErrori() {
        AtomicInteger notifiche = new AtomicInteger(0);
        player.attach(null);
        player.attach(notifiche::incrementAndGet);

        assertDoesNotThrow(() -> player.load(PlayerTestFixtures.sampleTrack()));
        assertEquals(1, notifiche.get());
    }

    // US21-T - registrare due volte lo stesso observer non duplica le notifiche.
    @Test
    void doppioAttachDelloStessoObserverNotificaUnaSolaVolta() {
        AtomicInteger notifiche = new AtomicInteger(0);
        Observer observer = notifiche::incrementAndGet;
        player.attach(observer);
        player.attach(observer);

        player.load(PlayerTestFixtures.sampleTrack());

        assertEquals(1, notifiche.get());
    }

    // US21-T - piu' observer registrati vengono tutti notificati.
    @Test
    void piuObserverRegistratiVengonoTuttiNotificati() {
        AtomicInteger primo = new AtomicInteger(0);
        AtomicInteger secondo = new AtomicInteger(0);
        player.attach(primo::incrementAndGet);
        player.attach(secondo::incrementAndGet);

        player.load(PlayerTestFixtures.sampleTrack());

        assertEquals(1, primo.get());
        assertEquals(1, secondo.get());
    }
}
