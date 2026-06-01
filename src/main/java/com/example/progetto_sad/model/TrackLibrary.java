package com.example.progetto_sad.model;

import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.observer.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrackLibrary implements Subject {

    private final List<Track> tracks = new ArrayList<>();
    private final List<Observer> observers = new ArrayList<>();

    public void addTrack(Track t) {
        if (t == null) {
            throw new IllegalArgumentException("La traccia non puo' essere null");
        }
        tracks.add(t);
        notifyObservers();
    }

    public void removeTrack(Track t) {
        if (tracks.remove(t)) {
            notifyObservers();
        }
    }

    public List<Track> getTracks() {
        return Collections.unmodifiableList(new ArrayList<>(tracks));
    }

    public boolean contains(Track t) {
        return tracks.contains(t);
    }

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
