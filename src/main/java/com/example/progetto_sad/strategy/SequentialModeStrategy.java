package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.Track;

import java.util.List;

/**
 * US17 - Pattern Strategy: modalità di riproduzione sequenziale.
 *
 * ConcreteStrategy che replica il comportamento già presente nel sistema:
 * seleziona come prossimo brano sempre quello che segue immediatamente
 * la traccia corrente nell'ordine della sequenza.
 *
 * Restituisce {@code null} quando la sequenza è terminata, segnalando al
 * Context e al suo Client che non esistono ulteriori brani da riprodurre.
 */
public class SequentialModeStrategy implements PlayModeStrategy {

    /**
     * Restituisce la traccia che segue {@code currentIndex} nella lista,
     * oppure {@code null} se la lista è vuota o se la traccia corrente è l'ultima.
     *
     * @param tracks       lista ordinata dei brani della sequenza; può essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return la traccia successiva, oppure {@code null} se la sequenza è terminata
     */
    @Override
    public Track getNextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        int nextIndex = currentIndex + 1;
        if (nextIndex < tracks.size()) {
            return tracks.get(nextIndex);
        }
        return null;
    }
}
