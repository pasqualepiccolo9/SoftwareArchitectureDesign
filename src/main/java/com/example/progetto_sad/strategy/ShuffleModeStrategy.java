package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * US18 - Pattern Strategy: modalità di riproduzione casuale (shuffle).
 *
 * ConcreteStrategy che seleziona il prossimo brano in ordine casuale restando
 * entro i limiti della sequenza corrente. Evita ripetizioni immediate finché
 * non sono stati riprodotti tutti gli altri brani disponibili nel ciclo;
 * al termine del ciclo o al cambio coda/playlist il mescolamento viene resettato.
 *
 * La selezione avviene per indice di posizione nella coda, così le tracce
 * duplicate (stesso oggetto accodato più volte) restano distinte.
 */
public class ShuffleModeStrategy implements PlayModeStrategy {

    private final Random random;
    private final List<Integer> remainingIndices = new ArrayList<>();
    private int trackedQueueSize = -1;

    public ShuffleModeStrategy() {
        this(new Random());
    }

    /**
     * @param random generatore controllabile (utile nei test per risultati ripetibili)
     */
    public ShuffleModeStrategy(Random random) {
        this.random = random;
    }

    /**
     * US18 - Reinizializza il ciclo casuale per la coda corrente.
     *
     * Va invocato quando cambia playlist, cambia la coda o si attiva la modalità shuffle.
     *
     * @param queueSize    numero di brani nella sequenza
     * @param currentIndex indice del brano attualmente in riproduzione
     */
    public void reset(int queueSize, int currentIndex) {
        trackedQueueSize = queueSize;
        remainingIndices.clear();
        if (queueSize <= 1) {
            return;
        }
        int safeCurrent = Math.max(0, Math.min(currentIndex, queueSize - 1));
        for (int i = 0; i < queueSize; i++) {
            if (i != safeCurrent) {
                remainingIndices.add(i);
            }
        }
        Collections.shuffle(remainingIndices, random);
    }

    /**
     * US18 - Restituisce il prossimo brano in ordine casuale senza ripetizioni
     * immediate finché restano indici disponibili nel ciclo corrente.
     *
     * @param tracks       lista dei brani nella sequenza corrente; non deve essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return il prossimo {@link Track}, oppure {@code null} se la coda è vuota,
     *         contiene un solo brano o l'indice corrente non è valido
     */
    @Override
    public Track getNextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            reset(0, 0);
            return null;
        }
        if (tracks.size() == 1) {
            reset(1, currentIndex);
            return null;
        }
        if (currentIndex < 0 || currentIndex >= tracks.size()) {
            return null;
        }

        if (tracks.size() != trackedQueueSize) {
            reset(tracks.size(), currentIndex);
        }

        if (remainingIndices.isEmpty()) {
            reset(tracks.size(), currentIndex);
        }
        if (remainingIndices.isEmpty()) {
            return null;
        }

        int pick = random.nextInt(remainingIndices.size());
        int nextIndex = remainingIndices.remove(pick);
        if (nextIndex < 0 || nextIndex >= tracks.size()) {
            return null;
        }
        return tracks.get(nextIndex);
    }
}
