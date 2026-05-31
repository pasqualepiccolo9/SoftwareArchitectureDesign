package com.example.progetto_sad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;

/**
 * US1 - Controller JavaFX del form "Aggiungi traccia".
 *
 * Gestisce l'azione di conferma: raccoglie i dati inseriti nella UI e li passa a
 * {@link TrackController}, che crea la traccia e la aggiunge alla libreria.
 * Non contiene logica di dominio: validazione e creazione sono delegate al
 * controller applicativo e alla factory.
 */
public class AddTrackController {

    private final TrackController trackController;

    @FXML
    private TextField audioFileField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private ComboBox<String> genreCombo;
    @FXML
    private TextField yearField;

    // File audio selezionato dall'utente (il path completo viene passato alla factory).
    private File selectedAudioFile;

    /**
     * @param trackController controller applicativo responsabile della creazione della traccia
     */
    public AddTrackController(TrackController trackController) {
        this.trackController = trackController;
    }

    /**
     * Apre un selettore di file audio e mostra il nome del file scelto nel campo.
     */
    @FXML
    private void onBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleziona file audio");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "File audio", "*.mp3", "*.wav", "*.m4a", "*.flac", "*.aac", "*.ogg"));

        File file = chooser.showOpenDialog(currentWindow());
        if (file != null) {
            selectedAudioFile = file;
            audioFileField.setText(file.getName());
        }
    }

    /**
     * Azione di conferma: passa i dati del form a TrackController per creare la traccia.
     * In caso di dati non validi mostra il messaggio di errore senza chiudere il form.
     */
    @FXML
    private void onSave() {
        try {
            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreCombo.getValue();
            String filePath = (selectedAudioFile != null) ? selectedAudioFile.getAbsolutePath() : null;
            int year = parseYear(yearField.getText());

            trackController.createTrack(title, author, genre, year, filePath);

            showInfo("Traccia aggiunta",
                    safeTitle(title) + " e' stata aggiunta alla libreria. Tracce totali: "
                            + trackController.getTracks().size());
            clearForm();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    /**
     * Annulla l'inserimento e chiude la finestra.
     */
    @FXML
    private void onCancel() {
        closeWindow();
    }

    /**
     * Chiude la finestra (pulsante X) senza salvare.
     */
    @FXML
    private void onClose() {
        closeWindow();
    }

    private int parseYear(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("L'anno e' obbligatorio");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("L'anno deve essere un numero valido");
        }
    }

    private void clearForm() {
        titleField.clear();
        authorField.clear();
        yearField.clear();
        genreCombo.getSelectionModel().clearSelection();
        genreCombo.setValue(null);
        audioFileField.clear();
        selectedAudioFile = null;
    }

    private void showInfo(String header, String content) {
        showAlert(Alert.AlertType.INFORMATION, "Conferma", header, content);
    }

    private void showError(String content) {
        showAlert(Alert.AlertType.ERROR, "Dati non validi", "Impossibile salvare la traccia", content);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(currentWindow());
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static String safeTitle(String s) {
        return (s == null || s.isBlank()) ? "La traccia" : s.trim();
    }

    private void closeWindow() {
        if (currentWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    private Window currentWindow() {
        return titleField.getScene().getWindow();
    }
}
