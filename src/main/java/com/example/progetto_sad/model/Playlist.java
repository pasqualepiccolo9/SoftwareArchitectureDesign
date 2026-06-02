package com.example.progetto_sad.model;

import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.observer.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rappresenta una playlist all'interno dell'applicazione.
 * Contiene un nome identificativo e una collezione ordinata di tracce.
 * Implementa l'interfaccia Subject per notificare i cambiamenti all'interfaccia grafica.
 */
public class Playlist implements Subject {

    private String name;
    private final List<Track> tracks;
    private final List<Observer> observers;
    
    /**
     * Costruisce una nuova playlist con il nome specificato.
     * * @param name Il nome da assegnare alla nuova playlist.
     * @throws IllegalArgumentException se il nome fornito è nullo o vuoto.
     */
    public Playlist(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della playlist non può essere vuoto");
        }
        this.name = name;
        this.tracks = new ArrayList<>();
        this.observers = new ArrayList<>();
    }
    
    /**
     * Restituisce il nome della playlist.
     * * @return Il nome della playlist.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Restituisce la lista delle tracce attualmente presenti nella playlist.
     * * @return La lista di oggetti Track contenuti nella playlist.
     */
    public List<Track> getTracks() {
        return new ArrayList<>(tracks); // Ritorna una copia per sicurezza
    }
    
    /**
     * Aggiunge una nuova traccia alla playlist e notifica gli osservatori.
     * * @param t La traccia da aggiungere alla playlist.
     * @throws IllegalArgumentException se la traccia è nulla o se è già presente nella playlist.
     */
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
    
    /**
     * Rimuove una traccia specifica dalla playlist e notifica gli osservatori.
     * * @param t La traccia da rimuovere.
     * @throws IllegalStateException se si tenta di rimuovere una traccia da una playlist vuota.
     * @throws IllegalArgumentException se la traccia fornita è nulla o non è presente nella playlist.
     */
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

    /**
     * Registra un nuovo osservatore per monitorare i cambiamenti della playlist.
     * * @param o L'osservatore da aggiungere alla lista.
     */
    @Override
    public void attach(Observer o) {
        if (!observers.contains(o)) observers.add(o);
    }
    
    /**
     * Rimuove un osservatore precedentemente registrato.
     * * @param o L'osservatore da rimuovere.
     */
    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    /**
     * Notifica tutti gli osservatori registrati invocando il loro metodo di aggiornamento.
     * Questo metodo viene chiamato automaticamente ogni volta che la playlist subisce una modifica.
     */
    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }

}
