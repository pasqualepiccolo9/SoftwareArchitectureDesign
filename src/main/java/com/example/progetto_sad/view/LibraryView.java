package com.example.progetto_sad.view;

import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import com.example.progetto_sad.observer.Observer;
import javafx.scene.control.ListView;

public class LibraryView implements Observer {

    private final ListView<Track> trackList;
    private final TrackLibrary library;

    public LibraryView(ListView<Track> trackList, TrackLibrary library) {
        this.trackList = trackList;
        this.library = library;
    }

    @Override
    public void update() {
        // US4 - aggiorna la ListView quando cambia la libreria
        Track selected = trackList.getSelectionModel().getSelectedItem();
        trackList.getItems().setAll(library.getTracks());
        if (selected != null) {
            trackList.getSelectionModel().select(selected);
        }
        trackList.refresh();
    }

    public void display() {
        update();
    }
}
