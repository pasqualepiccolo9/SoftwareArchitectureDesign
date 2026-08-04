package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.PlaylistSequence;
import com.example.progetto_sad.model.Track;

import java.util.List;

/**
 * US17 - Pattern Strategy: modalità di riproduzione sequenziale.
 *
 * ConcreteStrategy che replica il comportamento già presente nel sistema:
 * seleziona come prossimo brano sempre quello che segue immediatamente
 * la traccia corrente nell'ordine della sequenza e vi avanza di una posizione.
 *
 * Restituisce {@code null} / {@code false} quando la sequenza è terminata, segnalando al
 * Context e al suo Client che non esistono ulteriori brani da riprodurre.
 */
public class SequentialModeStrategy implements PlayModeStrategy {

    /** {@inheritDoc} */
    @Override
    public PlayMode getMode() {
        return PlayMode.SEQUENTIAL;
    }

    /**
     * Restituisce la traccia che segue {@code currentIndex} nella lista, oppure {@code null}
     * se la traccia corrente è l'ultima o se i parametri non individuano una posizione
     * valida nella coda, come previsto dal contratto di {@link PlayModeStrategy}.
     *
     * @param tracks       lista ordinata dei brani della sequenza; può essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return la traccia successiva, oppure {@code null} se la sequenza è terminata
     */
    @Override
    public Track getNextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || currentIndex < 0 || currentIndex >= tracks.size()) {
            return null;
        }
        int nextIndex = currentIndex + 1;
        if (nextIndex < tracks.size()) {
            return tracks.get(nextIndex);
        }
        return null;
    }

    /**
     * Avanza la sequenza sulla traccia immediatamente successiva, lasciando invariato
     * l'ordine della coda.
     *
     * @param sequence la sequenza da far avanzare; può essere null
     * @return {@code true} se la sequenza è avanzata, {@code false} se la traccia corrente
     *         era l'ultima
     */
    @Override
    public boolean moveToNextTrack(PlaylistSequence sequence) {
        if (sequence == null) {
            return false;
        }
        Track next = getNextTrack(sequence.getTracks(), sequence.getCurrentIndex());
        if (next == null) {
            return false;
        }
        return sequence.advanceTo(next);
    }

    /**
     * Indica se la traccia corrente è seguita da almeno un altro brano nella coda.
     *
     * @param sequence la sequenza corrente; può essere null
     * @return {@code true} se esiste un brano successivo
     */
    @Override
    public boolean hasNextTrack(PlaylistSequence sequence) {
        return sequence != null && sequence.hasNextTrack();
    }
}
