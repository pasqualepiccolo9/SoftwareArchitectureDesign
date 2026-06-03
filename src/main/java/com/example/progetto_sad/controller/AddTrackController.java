package com.example.progetto_sad.controller;

import com.example.progetto_sad.util.AudioDurationExtractor;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;

/**
 * US1 - Controller JavaFX del form "Aggiungi traccia".
 *
 * Gestisce l'azione di conferma: raccoglie i dati inseriti nella UI e la durata
 * gia' estratta automaticamente dal file al caricamento, e li passa a
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
    @FXML
    private Label durationHint;

    // File audio selezionato e durata estratta automaticamente al caricamento.
    private File selectedAudioFile;
    private int selectedDurationSeconds;

    /**
     * @param trackController controller applicativo responsabile della creazione della traccia
     */
    public AddTrackController(TrackController trackController) {
        this.trackController = trackController;
    }

    // US1 - limiti di input: Titolo/Autore max 20 caratteri, Anno solo 4 cifre.
    @FXML
    private void initialize() {
        limitLength(titleField, 20);
        limitLength(authorField, 20);
        limitDigits(yearField, 4);
    }

    // US1 - limita il campo a un numero massimo di caratteri (un valore piu' lungo gia'
    // presente resta accorciabile, ma non si possono aggiungere caratteri oltre il limite)
    private void limitLength(TextField field, int maxLength) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return (newText.length() <= maxLength || newText.length() < change.getControlText().length())
                    ? change : null;
        }));
    }

    // US1 - limita il campo a sole cifre, fino a maxDigits
    private void limitDigits(TextField field, int maxDigits) {
        field.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0," + maxDigits + "}") ? change : null));
    }

    /**
     * Apre un selettore di file audio, ne estrae automaticamente la durata
     * e mostra nel form il nome del file e la durata letta.
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

            // US1 - la durata viene estratta automaticamente al momento del caricamento del file
            selectedDurationSeconds = AudioDurationExtractor.extractSeconds(file);
            durationHint.setText(selectedDurationSeconds > 0
                    ? "Durata letta dal file: " + formatDuration(selectedDurationSeconds)
                    : "Durata non leggibile dal file");
        }
    }

    /**
     * Azione di conferma: passa al controller i dati del form e la durata gia'
     * estratta dal file, per creare la traccia. In caso di dati non validi
     * mostra il messaggio di errore senza chiudere il form.
     */
    @FXML
    private void onSave() {
        try {
            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreCombo.getValue();
            String filePath = (selectedAudioFile != null) ? selectedAudioFile.getAbsolutePath() : null;
            int year = parseYear(yearField.getText());

            trackController.createTrack(title, author, genre, year, filePath, selectedDurationSeconds);

            showInfo("Traccia aggiunta",
                    safeTitle(title) + " e' stata aggiunta alla libreria. Tracce totali: "
                            + trackController.getTracks().size());
            // US1 - dopo la conferma si torna alla schermata precedente chiudendo il form.
            closeWindow();
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

    private static String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
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
