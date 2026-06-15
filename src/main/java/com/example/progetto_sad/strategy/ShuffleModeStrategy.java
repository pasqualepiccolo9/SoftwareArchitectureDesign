package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * US18 - Pattern Strategy: modalità di riproduzione casuale (shuffle).
 *
 * Sceglie casualmente una traccia tra quelle future nella sequenza (dopo currentIndex).
 * Non mantiene stato interno: la posizione nella sequenza è l'unica fonte di verità
 * su cosa è già stato riprodotto. Questo garantisce che:
 *   - tracce già riprodotte (a currentIndex o prima) non vengano mai riproposte;
 *   - tracce aggiunte nuovamente alla coda siano automaticamente candidate;
 *   - la sessione termini naturalmente quando non esistono tracce future.
 */
public class ShuffleModeStrategy implements PlayModeStrategy {

    private final Random random;

    public ShuffleModeStrategy() {
        this.random = new Random();
    }

    /**
     * Restituisce una traccia scelta casualmente tra quelle presenti dopo currentIndex.
     * Restituisce {@code null} se non esistono tracce future nella lista ricevuta.
     *
     * @param tracks       lista dei brani nella sequenza corrente; non deve essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return una traccia futura casuale, oppure {@code null} se non ce ne sono
     */
    @Override
    public Track getNextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        int start = currentIndex + 1;
        if (start >= tracks.size()) {
            return null;
        }
        List<Track> candidates = new ArrayList<>(tracks.subList(start, tracks.size()));
        return candidates.get(random.nextInt(candidates.size()));
    }
}
