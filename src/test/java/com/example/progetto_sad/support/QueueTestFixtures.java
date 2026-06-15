package com.example.progetto_sad.support;

import com.example.progetto_sad.controller.PlaylistSequenceController;
import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;

/**
 * Dati di supporto per i test su coda mista e skip playlist (US13).
 *
 * Centralizza la creazione di tracce, playlist e configurazioni di coda
 * riutilizzabili nei test del gruppo senza duplicare setup.
 */
public final class QueueTestFixtures {

    private static int trackCounter;

    private QueueTestFixtures() {
    }

    /**
     * Azzera il contatore interno usato per generare titoli univoci nei test.
     */
    public static void resetCounter() {
        trackCounter = 0;
    }

    /**
     * Crea una traccia di prova con titolo e percorso univoci.
     *
     * @param label    etichetta descrittiva usata nel titolo
     * @param duration durata in secondi
     */
    public static Track track(String label, int duration) {
        trackCounter++;
        return new Track(
                "Track " + label + " #" + trackCounter,
                "Artist " + trackCounter,
                "Pop",
                2020,
                "C:/test/" + label + "-" + trackCounter + ".mp3",
                duration
        );
    }

    /**
     * Crea una playlist con il nome indicato e le tracce fornite.
     */
    public static Playlist playlist(String name, Track... tracks) {
        Playlist playlist = new Playlist(name);
        for (Track track : tracks) {
            playlist.addTrack(track);
        }
        return playlist;
    }

    /**
     * Playlist vuota con il nome indicato.
     */
    public static Playlist emptyPlaylist(String name) {
        return new Playlist(name);
    }

    /**
     * Playlist con un solo brano.
     */
    public static Playlist singleTrackPlaylist(String name, Track track) {
        return playlist(name, track);
    }

    /**
     * US13 - Costruisce una coda mista: playlist sorgente, brano singolo accodato,
     * seconda playlist e altro brano singolo finale.
     *
     * @return il controller con la coda già configurata (posizione corrente: primo brano)
     */
    public static MixedQueueSetup mixedQueueWithTail() {
        resetCounter();
        Track a1 = track("A1", 180);
        Track a2 = track("A2", 190);
        Track a3 = track("A3", 200);
        Track single = track("Single", 150);
        Track b1 = track("B1", 210);
        Track b2 = track("B2", 220);
        Track tail = track("Tail", 130);

        Playlist playlistA = playlist("Playlist A", a1, a2, a3);
        Playlist playlistB = playlist("Playlist B", b1, b2);

        PlaylistSequenceController controller = new PlaylistSequenceController();
        controller.startPlaylist(playlistA);
        controller.addToQueue(single);
        controller.addPlaylistToQueue(playlistB);
        controller.addToQueue(tail);

        return new MixedQueueSetup(controller, playlistA, playlistB, a1, a2, a3, single, b1, b2, tail);
    }

    /**
     * US13 - Coda con sola playlist sorgente e un brano singolo dopo di essa.
     */
    public static MixedQueueSetup sourcePlaylistWithSingleAfter() {
        resetCounter();
        Track a1 = track("A1", 180);
        Track a2 = track("A2", 190);
        Track single = track("Single", 150);
        Playlist playlistA = playlist("Playlist A", a1, a2);

        PlaylistSequenceController controller = new PlaylistSequenceController();
        controller.startPlaylist(playlistA);
        controller.addToQueue(single);

        return new MixedQueueSetup(controller, playlistA, null, a1, a2, null, single, null, null, null);
    }

    /**
     * Record con i riferimenti utili nei test sulla coda mista.
     */
    public record MixedQueueSetup(
            PlaylistSequenceController controller,
            Playlist sourcePlaylist,
            Playlist secondPlaylist,
            Track sourceTrack1,
            Track sourceTrack2,
            Track sourceTrack3,
            Track singleTrack,
            Track secondPlaylistTrack1,
            Track secondPlaylistTrack2,
            Track tailSingle
    ) {
    }
}
