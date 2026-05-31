package com.example.progetto_sad.model;

import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.observer.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Playlist implements Subject {

    private String name;
    private final List<Track> tracks;
    private final List<Observer> observers;

    public Playlist(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della playlist non può essere vuoto");
        }
        this.name = name;
        this.tracks = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    
public List<Track> getTracks() {
        return new ArrayList<>(tracks); // Ritorna una copia per sicurezza
    }

public void addTrack(Track t) {
        if (t == null) {
            throw new IllegalArgumentException("Selezione non valida: nessuna traccia selezionata.");
        }
        if (tracks.contains(t)) {
            throw new IllegalArgumentException("La traccia è già presente in questa playlist.");
        }
        tracks.add(t);
        notifyObservers();
    }

public void removeTrack(Track t) {
        if (tracks.isEmpty()) {
            throw new IllegalStateException("Impossibile rimuovere: la playlist è vuota.");
        }
        if (t == null || !tracks.contains(t)) {
            throw new IllegalArgumentException("La traccia selezionata non è presente nella playlist.");
        }
        tracks.remove(t);
        notifyObservers();
    }

// Metodi base del Subject per l'Observer Pattern
    @Override
    public void attach(Observer o) {
        if (!observers.contains(o)) observers.add(o);
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
