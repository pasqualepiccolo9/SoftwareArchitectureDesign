package com.example.progetto_sad;

import com.example.progetto_sad.controller.TrackController;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import com.example.progetto_sad.view.LibraryView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * US2/US4 - Controller JavaFX della schermata libreria tracce (hello-view).
 *
 * Mostra l'elenco delle tracce e il form di modifica. Delega la logica a
 * {@link TrackController}; l'aggiornamento automatico della lista e' gestito
 * da {@link LibraryView}, registrata come Observer sulla libreria.
 */
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

    /**
     * Inizializza libreria, controller applicativo e vista elenco tracce (US4),
     * e il listener che precompila il form alla selezione (US2).
     */
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

    // US2 - salva modifiche dal form (la durata e' sola lettura, non si modifica)
    @FXML
    private void onSaveClick() {
        try {
            Track selected = trackListView.getSelectionModel().getSelectedItem();
            int year = Integer.parseInt(yearField.getText().trim());
            trackController.updateTrack(
                    selected,
                    titleField.getText(),
                    authorField.getText(),
                    genreField.getText(),
                    year
            );
            errorLabel.setText("");
        } catch (NumberFormatException e) {
            errorLabel.setText("Anno non valido");
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
