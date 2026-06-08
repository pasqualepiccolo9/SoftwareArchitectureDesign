package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistSequence;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.observer.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * US10 - Controller applicativo per la gestione della sequenza di riproduzione.
 *
 * Orchestra la creazione della {@link PlaylistSequence} quando viene avviata una
 * playlist, mantiene la sequenza attiva ed espone la traccia corrente al resto
 * del sistema. Non contiene logica di UI nè dipendenze JavaFX.
 */
public class
PlaylistSequenceController implements Subject {

    private PlaylistSequence sequence;
    private final List<Observer> observers;

    /**
     * Crea il controller senza alcuna sequenza attiva.
     */
    public PlaylistSequenceController() {
        this.sequence = null;
        this.observers = new ArrayList<>();
    }

    /**
     * Avvia la riproduzione della playlist specificata creando una nuova
     * {@link PlaylistSequence}. La prima traccia della playlist diventa la traccia
     * corrente. Se la playlist è nulla o vuota la sequenza viene comunque creata
     * in stato terminato, senza causare crash.
     *
     * @param playlist la playlist da avviare; puo' essere null o vuota
     */
    public void startPlaylist(Playlist playlist) {
        sequence = PlaylistSequence.from(playlist);
        notifyObservers();
    }

    /**
     * Restituisce la sequenza attualmente attiva.
     *
     * @return la sequenza corrente, oppure {@code null} se nessuna playlist è stata avviata
     */
    public PlaylistSequence getSequence() {
        return sequence;
    }

    /**
     * Restituisce la traccia attualmente in riproduzione.
     *
     * @return la traccia corrente, oppure {@code null} se nessuna sequenza è attiva
     *         o la sequenza è terminata
     */
    public Track getCurrentTrack() {
        if (sequence == null) {
            return null;
        }
        return sequence.getCurrentTrack();
    }

    /**
     * Notifica il controller che la traccia corrente è terminata naturalmente.
     * Avanza automaticamente alla traccia successiva nella sequenza.
     * Se la sequenza non è attiva o è gia' terminata, la chiamata non ha effetto.
     */
    public void onTrackFinished() {
        if (sequence != null && !sequence.isFinished()) {
            sequence.advance();
            notifyObservers();
        }
    }

    /**
     * Indica se esiste una sequenza attiva con almeno un brano ancora da riprodurre.
     *
     * @return {@code true} se la sequenza è stata avviata e non è terminata
     */
    public boolean hasActiveSequence() {
        return sequence != null && !sequence.isFinished();
    }

    /**
     * Indica se la sequenza è stata avviata ed è terminata (playlist vuota o
     * ultimo brano concluso).
     *
     * @return {@code true} se la sequenza esiste ma non ha piu' brani da riprodurre
     */
    public boolean isSequenceFinished() {
        return sequence != null && sequence.isFinished();
    }

    /**
     * Restituisce i brani che seguono la traccia corrente nella sequenza attiva.
     * Se nessuna sequenza e' attiva o non ci sono successivi, restituisce una lista vuota.
     *
     * @return copia non modificabile delle tracce successive a quella corrente
     */
    public List<Track> getNextTracks() {
        if (sequence == null) {
            return List.of();
        }
        return sequence.getNextTracks();
    }

    /**
     * Aggiunge un singolo brano alla fine della sequenza di riproduzione senza
     * interrompere la traccia corrente. Se nessuna sequenza e' attiva, ne crea una
     * vuota e vi inserisce il brano come primo elemento. Se {@code track} e' null,
     * la chiamata non ha effetto.
     *
     * @param track il brano da accodare
     */
    public void addToQueue(Track track) {
        if (track == null) {
            return;
        }
        if (sequence == null) {
            sequence = PlaylistSequence.empty();
        }
        sequence.addTrack(track);
        notifyObservers();
    }

    /**
     * Rimuove un brano successivo dalla sequenza attiva usando un indice relativo
     * ai soli brani successivi.
     *
     * @param nextIndex indice zero-based tra i brani successivi
     * @return {@code true} se il brano e' stato rimosso, {@code false} altrimenti
     */
    public boolean removeNextTrackAt(int nextIndex) {
        if (sequence == null) {
            return false;
        }
        boolean removed = sequence.removeNextTrackAt(nextIndex);
        if (removed) {
            notifyObservers();
        }
        return removed;
    }

    @Override
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
