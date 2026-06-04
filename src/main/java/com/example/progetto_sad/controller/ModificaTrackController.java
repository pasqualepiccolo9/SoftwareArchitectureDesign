package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Track;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * US2 - Controller JavaFX della schermata "Modifica traccia".
 *
 * Riceve la traccia da modificare e il {@link TrackController}; al caricamento
 * precompila i campi con i dati della traccia. Su "Salva" delega la modifica al
 * controller applicativo; su "Annulla" chiude senza salvare. L'eliminazione NON
 * avviene piu' qui: si fa dalla riga della traccia nella schermata Libreria (US3).
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
    private Label errorLabel;

    /**
     * @param trackController controller applicativo per modifica/eliminazione
     * @param track           traccia da modificare (già presente in libreria)
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
        // US1/US2 - limiti di input (dopo il precompile, così i valori esistenti non vengono bloccati)
        limitLength(titleField, 20);
        limitLength(authorField, 20);
        limitDigits(yearField, 4);
    }

    // US2 - limita il campo a un numero massimo di caratteri (un valore più lungo già
    // presente resta accorciabile, ma non si possono aggiungere caratteri oltre il limite)
    private void limitLength(TextField field, int maxLength) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return (newText.length() <= maxLength || newText.length() < change.getControlText().length())
                    ? change : null;
        }));
    }

    // US2 - limita il campo a sole cifre, fino a maxDigits
    private void limitDigits(TextField field, int maxDigits) {
        field.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0," + maxDigits + "}") ? change : null));
    }

    // US2 - salva le modifiche (la durata è sola lettura, non si modifica)
    @FXML
    private void onSaveClick() {
        try {
            int year = Integer.parseInt(yearField.getText().trim());
            trackController.updateTrack(track, titleField.getText(), authorField.getText(),
                    genreCombo.getValue(), year);
            errorLabel.setText("");
            // US2 - conferma e ritorno alla schermata precedente (coerente col form "Aggiungi").
            showSavedConfirmation();
            closeWindow();
        } catch (NumberFormatException e) {
            errorLabel.setText("Anno non valido");
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    // US2 - scarta le modifiche e torna alla schermata precedente (senza salvare)
    @FXML
    private void onCancelClick() {
        closeWindow();
    }

    // US2 - conferma di avvenuto salvataggio
    private void showSavedConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(currentWindow());
        alert.setTitle("Modifica salvata");
        alert.setHeaderText(null);
        alert.setContentText("Le modifiche a \"" + track.getTitle() + "\" sono state salvate.");
        alert.showAndWait();
    }

    // US2 - chiude la finestra di modifica, tornando alla schermata precedente
    private void closeWindow() {
        if (currentWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    private Window currentWindow() {
        return titleField.getScene().getWindow();
    }
}
