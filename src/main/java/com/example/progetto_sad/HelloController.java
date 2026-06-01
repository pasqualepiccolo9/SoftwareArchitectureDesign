package com.example.progetto_sad;

import com.example.progetto_sad.controller.TrackController;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import com.example.progetto_sad.view.LibraryView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class HelloController {

    @FXML
    private ListView<Track> trackListView;
    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private TextField genreField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField durationField;
    @FXML
    private Label errorLabel;

    private TrackLibrary library;
    private TrackController trackController;
    private LibraryView libraryView;

    @FXML
    private void initialize() {
        library = new TrackLibrary();
        trackController = new TrackController(library);
        // US4 - lista tracce JavaFX collegata al modello
        libraryView = new LibraryView(trackListView, library);
        library.attach(libraryView);
        libraryView.display();

        // US2 - carica i campi quando selezioni una traccia dalla ListView
        trackListView.getSelectionModel().selectedItemProperty().addListener((obs, oldTrack, selected) -> {
            errorLabel.setText("");
            if (selected == null) {
                clearForm();
                return;
            }
            titleField.setText(selected.getTitle());
            authorField.setText(selected.getAuthor());
            genreField.setText(selected.getGenre());
            yearField.setText(String.valueOf(selected.getYear()));
            durationField.setText(String.valueOf(selected.getDuration()));
        });
    }

    @FXML
    private void onSaveClick() {
        // US2 - salva modifiche dal form FXML
        try {
            Track selected = trackListView.getSelectionModel().getSelectedItem();
            int year = Integer.parseInt(yearField.getText().trim());
            int duration = Integer.parseInt(durationField.getText().trim());
            trackController.updateTrack(
                    selected,
                    titleField.getText(),
                    authorField.getText(),
                    genreField.getText(),
                    year,
                    duration
            );
            errorLabel.setText("");
        } catch (NumberFormatException e) {
            errorLabel.setText("Anno o durata non validi");
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private void clearForm() {
        titleField.clear();
        authorField.clear();
        genreField.clear();
        yearField.clear();
        durationField.clear();
    }
}
