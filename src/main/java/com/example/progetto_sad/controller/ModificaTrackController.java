package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Track;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Optional;

/**
 * US2/US3 - Controller JavaFX della schermata "Modifica traccia".
 *
 * Riceve la traccia da modificare e il {@link TrackController}; al caricamento
 * precompila i campi con i dati della traccia. Su "Salva" delega la modifica e su
 * "Elimina" (previa conferma) delega l'eliminazione al controller applicativo.
 * Non gestisce alcuna lista: la schermata viene aperta a partire da una traccia
 * gia' selezionata altrove.
 */
public class ModificaTrackController {

    private final TrackController trackController;
    private final Track track;

    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private ComboBox<String> genreCombo;
    @FXML
    private TextField yearField;
    @FXML
    private TextField durationField;
    @FXML
    private Label errorLabel;

    /**
     * @param trackController controller applicativo per modifica/eliminazione
     * @param track           traccia da modificare (gia' presente in libreria)
     */
    public ModificaTrackController(TrackController trackController, Track track) {
        this.trackController = trackController;
        this.track = track;
    }

    @FXML
    private void initialize() {
        // Precompila il form con i dati della traccia da modificare.
        titleField.setText(track.getTitle());
        authorField.setText(track.getAuthor());
        genreCombo.setValue(track.getGenre());
        yearField.setText(String.valueOf(track.getYear()));
        durationField.setText(String.valueOf(track.getDuration()));
    }

    // US2 - salva le modifiche (la durata e' sola lettura, non si modifica)
    @FXML
    private void onSaveClick() {
        try {
            int year = Integer.parseInt(yearField.getText().trim());
            trackController.updateTrack(track, titleField.getText(), authorField.getText(),
                    genreCombo.getValue(), year);
            errorLabel.setText("");
        } catch (NumberFormatException e) {
            errorLabel.setText("Anno non valido");
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    // US3 - elimina la traccia in modifica, previa conferma dell'utente
    @FXML
    private void onDeleteClick() {
        if (track == null) {
            return;
        }
        if (confirmDeletion(track)) {
            trackController.deleteTrack(track);
            errorLabel.setText("");
        }
    }

    // US3 - pop-up di conferma "Sei sicuro?" prima di eliminare
    private boolean confirmDeletion(Track t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Eliminare la traccia \"" + t.getTitle() + "\"?");
        alert.setContentText("L'operazione non puo' essere annullata.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
