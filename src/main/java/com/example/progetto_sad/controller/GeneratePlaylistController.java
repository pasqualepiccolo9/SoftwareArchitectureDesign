package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.Year;
import java.util.List;

/**
 * US26 - Controller FXML per la generazione automatica di una playlist per anno.
 * Gestisce validazione dell'anno, anteprima delle tracce trovate e creazione
 * della playlist tramite PlaylistController.
 * Supporta modalità inline (integrata nella scena principale via scene.setRoot)
 * e modalità standalone (finestra separata).
 */
public class GeneratePlaylistController {

    private static final int MIN_YEAR = 1877;

    private TrackLibrary library;
    private PlaylistController playlistController;
    private Runnable onBack;
    private Runnable onSuccess;
    private Stage standaloneStage;

    private List<Track> currentTracks = List.of();

    @FXML private TextField yearField;
    @FXML private TextField nameField;
    @FXML private VBox previewListBox;
    @FXML private Label previewHeader;
    @FXML private Label statusLabel;
    @FXML private Button generateBtn;
    @FXML private Button backBtn;
    @FXML private Button closeBtn;

    @FXML
    private void initialize() {
        yearField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,4}") ? change : null));
        yearField.textProperty().addListener((obs, old, val) -> onYearChanged(val));

        nameField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= 20 ? change : null));
        nameField.textProperty().addListener((obs, old, val) -> {
            if (!old.isBlank() && val.isBlank() && !currentTracks.isEmpty()) {
                showError("Il nome della playlist non può essere vuoto.");
            } else if (!val.isBlank() && (
                    "Il nome della playlist non può essere vuoto.".equals(statusLabel.getText()) ||
                    "Nome playlist già in uso.".equals(statusLabel.getText()))) {
                clearStatus();
            }
            updateGenerateButton();
        });
    }

    /**
     * Configura il controller per la modalità inline.
     * Mostra "← Indietro", nasconde la X. Deve essere chiamato dopo loader.load().
     */
    public void setInlineMode(TrackLibrary library, PlaylistController playlistController,
                               Runnable onBack, Runnable onSuccess) {
        this.library = library;
        this.playlistController = playlistController;
        this.onBack = onBack;
        this.onSuccess = onSuccess;
        backBtn.setVisible(true);
        backBtn.setManaged(true);
        closeBtn.setVisible(false);
        closeBtn.setManaged(false);
    }

    /**
     * Configura il controller per la modalità standalone.
     * Nasconde "← Indietro", mostra la X. Deve essere chiamato dopo loader.load().
     */
    public void setStandaloneMode(TrackLibrary library, PlaylistController playlistController,
                                   Stage stage, Runnable onSuccess) {
        this.library = library;
        this.playlistController = playlistController;
        this.standaloneStage = stage;
        this.onSuccess = onSuccess;
        backBtn.setVisible(false);
        backBtn.setManaged(false);
        closeBtn.setVisible(true);
        closeBtn.setManaged(true);
    }

    @FXML
    private void onBack() {
        if (onBack != null) onBack.run();
    }

    @FXML
    private void onCancel() {
        if (onBack != null) {
            onBack.run();
        } else if (standaloneStage != null) {
            standaloneStage.close();
        }
    }

    @FXML
    private void onClose() {
        if (standaloneStage != null) standaloneStage.close();
    }

    @FXML
    private void onGenerate() {
        String text = yearField.getText();
        int year;
        try {
            year = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return;
        }
        String name = nameField.getText().trim();
        if (name.isBlank()) return;

        if (library == null || playlistController == null) {
            showError("Il controller non è ancora configurato.");
            return;
        }

        try {
            playlistController.createPlaylistByYear(year, name, library);
            if (onSuccess != null) onSuccess.run();
            if (standaloneStage != null) standaloneStage.close();
        } catch (IllegalArgumentException e) {
            showError("Nome playlist già in uso.");
        }
    }

    private void onYearChanged(String text) {
        clearStatus();

        if (text == null || text.isBlank() || text.length() < 4) {
            currentTracks = List.of();
            updatePreview(false);
            updateGenerateButton();
            return;
        }

        int year = Integer.parseInt(text);
        int currentYear = Year.now().getValue();
        if (year < MIN_YEAR || year > currentYear) {
            currentTracks = List.of();
            updatePreview(false);
            showError("Inserisci un anno compreso tra " + MIN_YEAR + " e " + currentYear + ".");
            updateGenerateButton();
            return;
        }

        if (library == null) {
            currentTracks = List.of();
            updatePreview(false);
            updateGenerateButton();
            return;
        }

        currentTracks = library.getTracksByYear(year);
        nameField.setText("Brani " + year);
        updatePreview(true);
        updateGenerateButton();
    }

    private void updatePreview(boolean showNoTracksLabel) {
        previewListBox.getChildren().clear();

        if (currentTracks.isEmpty()) {
            previewHeader.setText("Anteprima tracce trovate (0)");
            if (showNoTracksLabel) {
                Label emptyLabel = new Label("Nessuna traccia trovata per questo anno.");
                emptyLabel.getStyleClass().add("form-label");
                emptyLabel.setPadding(new Insets(8, 12, 8, 12));
                previewListBox.getChildren().add(emptyLabel);
            }
            return;
        }

        previewHeader.setText("Anteprima tracce trovate (" + currentTracks.size() + ")");
        for (Track t : currentTracks) {
            previewListBox.getChildren().add(buildPreviewRow(t));
        }
    }

    private HBox buildPreviewRow(Track t) {
        Label titleLabel = new Label(t.getTitle());
        titleLabel.getStyleClass().add("row-title");

        Label authorLabel = new Label("· " + t.getAuthor());
        authorLabel.getStyleClass().add("row-meta");

        HBox info = new HBox(6, titleLabel, authorLabel);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label durationLabel = new Label(formatDuration(t.getDuration()));
        durationLabel.getStyleClass().add("row-duration");

        HBox row = new HBox(12, info, durationLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.getStyleClass().add("track-row");
        return row;
    }

    private void updateGenerateButton() {
        String text = yearField.getText();
        boolean yearValid = false;
        if (text != null && !text.isBlank()) {
            try {
                int year = Integer.parseInt(text);
                int currentYear = Year.now().getValue();
                yearValid = year >= MIN_YEAR && year <= currentYear;
            } catch (NumberFormatException ignored) {}
        }
        boolean hasName = nameField != null && !nameField.getText().isBlank();
        generateBtn.setDisable(!(yearValid && !currentTracks.isEmpty() && hasName));
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #f06a6a; -fx-font-size: 12px; -fx-font-weight: bold;");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setStyle("");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    private String formatDuration(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
