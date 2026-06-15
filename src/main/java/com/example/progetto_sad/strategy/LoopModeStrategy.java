package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.Track;

import java.util.List;

/**
 * US19 - Pattern Strategy: modalità di riproduzione loop (rotazione infinita della coda).
 *
 * Restituisce sempre la traccia successiva in modo circolare, segnalando al controller
 * che la riproduzione non deve terminare. La mutazione effettiva della coda (rotazione)
 * è delegata a {@link com.example.progetto_sad.model.PlaylistSequence#advanceLooping()}.
 */
public class LoopModeStrategy implements PlayModeStrategy {

    /**
     * Restituisce la traccia successiva in modo circolare.
     * Restituisce {@code null} solo se la lista è vuota o l'indice non è valido.
     *
     * @param tracks       lista dei brani nella sequenza corrente
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return la traccia successiva circolare, oppure {@code null} se non disponibile
     */
    @Override
    public Track getNextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        if (currentIndex < 0 || currentIndex >= tracks.size()) {
            return null;
        }
        return tracks.get((currentIndex + 1) % tracks.size());
    }
}
