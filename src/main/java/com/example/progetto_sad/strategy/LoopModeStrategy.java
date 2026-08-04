package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.PlaylistSequence;
import com.example.progetto_sad.model.Track;

import java.util.List;

/**
 * US19 - Pattern Strategy: modalità di riproduzione loop (rotazione infinita della coda).
 *
 * ConcreteStrategy che non termina mai finché la coda contiene almeno un brano: la
 * traccia corrente viene riportata in fondo alla coda tramite
 * {@link PlaylistSequence#advanceLooping()} e la successiva diventa quella corrente.
 *
 * All'attivazione la modalità scarta i brani già riprodotti, così che la rotazione
 * riguardi soltanto la coda ancora attiva (traccia corrente e brani successivi).
 */
public class LoopModeStrategy implements PlayModeStrategy {

    /** {@inheritDoc} */
    @Override
    public PlayMode getMode() {
        return PlayMode.LOOP;
    }

    /**
     * Restituisce la traccia successiva in modo circolare: essendo la modalità infinita,
     * restituisce {@code null} soltanto quando i parametri non individuano una posizione
     * valida nella coda, come previsto dal contratto di {@link PlayModeStrategy}.
     *
     * @param tracks       lista dei brani nella sequenza corrente; può essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return la traccia successiva circolare, oppure {@code null} se non disponibile
     */
    @Override
    public Track getNextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || currentIndex < 0 || currentIndex >= tracks.size()) {
            return null;
        }
        return tracks.get((currentIndex + 1) % tracks.size());
    }

    /**
     * Ruota la coda: la traccia corrente torna in fondo e la successiva diventa corrente.
     * Con un solo brano in coda la rotazione riesce senza modificare nulla, così la
     * riproduzione prosegue all'infinito sullo stesso brano.
     *
     * @param sequence la sequenza da far ruotare; può essere null
     * @return {@code true} se la rotazione è avvenuta, {@code false} se la coda è vuota
     */
    @Override
    public boolean moveToNextTrack(PlaylistSequence sequence) {
        if (sequence == null) {
            return false;
        }
        return sequence.advanceLooping();
    }

    /**
     * In modalità loop esiste sempre un brano successivo finché la coda non è vuota.
     *
     * @param sequence la sequenza corrente; può essere null
     * @return {@code true} se la coda contiene almeno un brano
     */
    @Override
    public boolean hasNextTrack(PlaylistSequence sequence) {
        return sequence != null && !sequence.isEmpty();
    }

    /**
     * US19 - All'attivazione scarta dalla coda i brani già riprodotti, così che la
     * rotazione infinita riguardi solo la traccia corrente e i brani successivi.
     *
     * @param sequence la sequenza corrente; può essere null
     * @return il numero di brani scartati dalla testa della coda
     */
    @Override
    public int onActivated(PlaylistSequence sequence) {
        if (sequence == null) {
            return 0;
        }
        return sequence.discardTracksBeforeCurrent();
    }
}
