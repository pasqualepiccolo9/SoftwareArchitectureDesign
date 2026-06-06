package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistSequence;
import com.example.progetto_sad.model.Track;

/**
 * US10 - Controller applicativo per la gestione della sequenza di riproduzione.
 *
 * Orchestra la creazione della {@link PlaylistSequence} quando viene avviata una
 * playlist, mantiene la sequenza attiva ed espone la traccia corrente al resto
 * del sistema. Non contiene logica di UI ne' dipendenze JavaFX.
 */
public class
PlaylistSequenceController {

    private PlaylistSequence sequence;

    /**
     * Crea il controller senza alcuna sequenza attiva.
     */
    public PlaylistSequenceController() {
        this.sequence = null;
    }

    /**
     * Avvia la riproduzione della playlist specificata creando una nuova
     * {@link PlaylistSequence}. La prima traccia della playlist diventa la traccia
     * corrente. Se la playlist e' nulla o vuota la sequenza viene comunque creata
     * in stato terminato, senza causare crash.
     *
     * @param playlist la playlist da avviare; puo' essere null o vuota
     */
    public void startPlaylist(Playlist playlist) {
        sequence = PlaylistSequence.from(playlist);
    }

    /**
     * Restituisce la sequenza attualmente attiva.
     *
     * @return la sequenza corrente, oppure {@code null} se nessuna playlist e' stata avviata
     */
    public PlaylistSequence getSequence() {
        return sequence;
    }

    /**
     * Restituisce la traccia attualmente in riproduzione.
     *
     * @return la traccia corrente, oppure {@code null} se nessuna sequenza e' attiva
     *         o la sequenza e' terminata
     */
    public Track getCurrentTrack() {
        if (sequence == null) {
            return null;
        }
        return sequence.getCurrentTrack();
    }

    /**
     * Notifica il controller che la traccia corrente e' terminata naturalmente.
     * Avanza automaticamente alla traccia successiva nella sequenza.
     * Se la sequenza non e' attiva o e' gia' terminata, la chiamata non ha effetto.
     */
    public void onTrackFinished() {
        if (sequence != null) {
            sequence.advance();
        }
    }
}
