package com.example.progetto_sad.observer;

public interface Subject {
    void attach(Observer o);

    void detach(Observer o);

    void notifyObservers();
}
