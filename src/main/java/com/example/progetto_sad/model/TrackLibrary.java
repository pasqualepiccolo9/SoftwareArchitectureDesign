package com.example.progetto_sad.model;

import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.observer.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * US4 - Modello della libreria delle tracce (collezione in memoria).
 *
 * Implementa {@link Subject}: aggiunta, rimozione e aggiornamento metadati
 * notificano gli observer collegati (es. la vista elenco tracce).
 */
public class TrackLibrary implements Subject {

    private final List<Track> tracks = new ArrayList<>();
    private final List<Observer> observers = new ArrayList<>();

    // US4 - aggiunge una traccia e notifica gli observer
    public void addTrack(Track t) {
        if (t == null) {
            throw new IllegalArgumentException("La traccia non puo' essere null");
        }
        tracks.add(t);
        notifyObservers();
    }

    /**
     * US22 - Inserisce una traccia in una posizione specifica dell'elenco e notifica
     * gli observer. Serve ai comandi di annullamento per ripristinare una traccia
     * rimossa esattamente nella sua posizione originaria.
     *
     * @param index la posizione di inserimento, da 0 a size (inclusi)
     * @param t la traccia da inserire
     * @throws IllegalArgumentException se la traccia e' null o l'indice e' fuori intervallo
     */
    public void addTrackAt(int index, Track t) {
        if (t == null) {
            throw new IllegalArgumentException("La traccia non puo' essere null");
        }
        if (index < 0 || index > tracks.size()) {
            throw new IllegalArgumentException("Posizione di inserimento non valida");
        }
        tracks.add(index, t);
        notifyObservers();
    }

    // US4 - rimuove una traccia e notifica gli observer se la rimozione ha effetto
    public void removeTrack(Track t) {
        if (tracks.remove(t)) {
            notifyObservers();
        }
    }

    /**
     * @return copia non modificabile dell'elenco delle tracce in libreria
     */
    public List<Track> getTracks() {
        return Collections.unmodifiableList(new ArrayList<>(tracks));
    }

    public boolean contains(Track t) {
        return tracks.contains(t);
    }

    /**
     * US26 - Restituisce tutte le tracce della libreria pubblicate nell'anno indicato,
     * mantenendo l'ordine originale. La lista interna non viene modificata.
     *
     * @param year anno di pubblicazione da cercare
     * @return nuova lista (possibilmente vuota) con le tracce dell'anno
     */
    public List<Track> getTracksByYear(int year) {
        List<Track> result = new ArrayList<>();
        for (Track t : tracks) {
            if (t.getYear() == year) {
                result.add(t);
            }
        }
        return result;
    }

    // US2/US4 - notifica gli observer dopo modifica metadati di una traccia gia' in libreria
    public void trackUpdated() {
        notifyObservers();
    }

    @Override
    public void attach(Observer o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }
}
