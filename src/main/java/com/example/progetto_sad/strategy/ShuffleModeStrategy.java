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
 * La selezione mantiene le occorrenze ancora disponibili, così anche tracce
 * duplicate presenti più volte in coda partecipano al ciclo casuale.
 */
public class ShuffleModeStrategy implements PlayModeStrategy {

    private final Random random;
    private final List<Track> remainingTracks = new ArrayList<>();
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
        remainingTracks.clear();
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

        if (remainingTracks.isEmpty()) {
            refillRemainingTracks(tracks, currentIndex);
        }
        if (remainingTracks.isEmpty()) {
            return null;
        }

        int pick = random.nextInt(remainingTracks.size());
        Track nextTrack = remainingTracks.remove(pick);
        if (nextTrack == null || !tracks.contains(nextTrack)) {
            return null;
        }
        return nextTrack;
    }

    private void refillRemainingTracks(List<Track> tracks, int currentIndex) {
        remainingTracks.clear();
        if (tracks == null || tracks.size() <= 1 || currentIndex < 0 || currentIndex >= tracks.size()) {
            trackedQueueSize = tracks == null ? 0 : tracks.size();
            return;
        }
        trackedQueueSize = tracks.size();
        for (int i = 0; i < tracks.size(); i++) {
            if (i != currentIndex) {
                remainingTracks.add(tracks.get(i));
            }
        }
        Collections.shuffle(remainingTracks, random);
    }
}
