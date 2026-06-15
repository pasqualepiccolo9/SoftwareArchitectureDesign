package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.Track;

import java.util.List;
import java.util.Random;

/**
 * US18 - Pattern Strategy: modalità di riproduzione casuale (shuffle).
 *
 * ConcreteStrategy che seleziona il prossimo brano scegliendo casualmente
 * un indice diverso da quello corrente tra tutti i brani disponibili nella
 * sequenza. La riproduzione non termina finché sono presenti almeno due
 * brani, poiché ogni chiamata restituisce sempre un brano valido.
 *
 * Restituisce {@code null} solo se la lista è vuota o contiene un solo brano,
 * coerentemente con la convenzione stabilita da {@link SequentialModeStrategy}.
 */
public class ShuffleModeStrategy implements PlayModeStrategy {

    private final Random random;

    public ShuffleModeStrategy() {
        this.random = new Random();
    }

    /**
     * Restituisce una traccia scelta casualmente dalla lista, diversa da quella
     * all'indice {@code currentIndex}.
     *
     * @param tracks       lista ordinata dei brani della sequenza; può essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return una traccia casuale diversa da quella corrente, oppure {@code null}
     *         se la lista è vuota o contiene un solo brano
     */
    @Override
    public Track getNextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        if (tracks.size() == 1) {
            return null;
        }
        int nextIndex;
        do {
            nextIndex = random.nextInt(tracks.size());
        } while (nextIndex == currentIndex);
        return tracks.get(nextIndex);
    }
}
