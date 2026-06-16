package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.PlaylistController;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.Year;
import java.util.List;

/**
 * US26 - Dialog per la generazione automatica di una playlist per anno.
 * Java-only, nessun FXML. Segue lo stile degli altri dialog (add-track-dialog.css).
 */
public class GeneratePlaylistDialogView {

    private static final int MIN_YEAR = 1877;

    private Stage stage;
    private TrackLibrary library;
    private PlaylistController playlistController;
    private Runnable onSuccess;

    private TextField yearField;
    private TextField nameField;
    private VBox previewListBox;
    private Label previewHeader;
    private Label statusLabel;
    private Button generateBtn;

    private List<Track> currentTracks = List.of();

    public void init(TrackLibrary library, PlaylistController playlistController,
                     Window owner, Runnable onSuccess) {
        this.library = library;
        this.playlistController = playlistController;
        this.onSuccess = onSuccess;
        currentTracks = List.of();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setResizable(false);
        stage.setTitle("Genera playlist automatica");

        Scene scene = new Scene(buildRoot(), 560, 560);
        var cssUrl = getClass().getResource(
                "/com/example/progetto_sad/view/add-track-dialog.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setScene(scene);
    }

    public void show() {
        if (stage != null) {
            stage.showAndWait();
        }
    }

    // ── layout ───────────────────────────────────────────────────────────────

    private VBox buildRoot() {
        // Inizializza i campi prima di costruire il layout, così i listener
        // possono riferirsi a nameField e generateBtn già inizializzati.
        previewListBox = new VBox(0);

        previewHeader = new Label("Anteprima tracce trovate (0)");
        previewHeader.getStyleClass().add("form-label");

        statusLabel = new Label("");
        statusLabel.getStyleClass().add("form-label");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        generateBtn = new Button("Genera");
        generateBtn.getStyleClass().add("add-btn");
        generateBtn.setDisable(true);
        generateBtn.setOnAction(e -> onGenerate());

        nameField = new TextField();
        nameField.setPromptText("Nome della playlist");
        nameField.getStyleClass().add("year-field");
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= 40 ? change : null));
        nameField.textProperty().addListener((obs, old, val) -> updateGenerateButton());

        yearField = new TextField();
        yearField.setPromptText("es. 2024");
        yearField.getStyleClass().add("year-field");
        yearField.setPrefWidth(120);
        yearField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,4}") ? change : null));
        yearField.textProperty().addListener((obs, old, val) -> onYearChanged(val));

        VBox card = new VBox(14);
        card.getStyleClass().add("dialog-card");
        VBox.setVgrow(card, Priority.ALWAYS);
        card.getChildren().addAll(
                buildHeader(),
                buildGeneraPer(),
                buildYearFilterSection(),
                buildPreviewSection(),
                buildNameRow(),
                statusLabel,
                buildFooter()
        );

        VBox root = new VBox(card);
        root.getStyleClass().add("dialog-root");
        return root;
    }

    private HBox buildHeader() {
        Label titleLabel = new Label("Genera playlist automatica");
        titleLabel.getStyleClass().add("dialog-title");
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        SVGPath closeIcon = new SVGPath();
        closeIcon.setContent("M1 1 L13 13 M13 1 L1 13");
        closeIcon.getStyleClass().add("close-icon");
        Button closeBtn = new Button();
        closeBtn.setGraphic(closeIcon);
        closeBtn.getStyleClass().add("close-button");
        closeBtn.setOnAction(e -> stage.close());

        HBox header = new HBox(titleBox, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox buildGeneraPer() {
        Label sectionLabel = new Label("Genera per");
        sectionLabel.getStyleClass().add("form-label");

        Label annoPill = new Label("Anno");
        annoPill.getStyleClass().add("active-pill-btn");

        HBox pillBox = new HBox(annoPill);
        pillBox.setAlignment(Pos.CENTER_LEFT);

        return new VBox(6, sectionLabel, pillBox);
    }

    private VBox buildYearFilterSection() {
        Label filtroLabel = new Label("Filtro");
        filtroLabel.getStyleClass().add("form-label");

        return new VBox(6, filtroLabel, yearField);
    }

    private VBox buildPreviewSection() {
        ScrollPane scroll = new ScrollPane(previewListBox);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("dialog-scroll");
        scroll.setPrefHeight(150);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox section = new VBox(6, previewHeader, scroll);
        VBox.setVgrow(section, Priority.ALWAYS);
        return section;
    }

    private VBox buildNameRow() {
        Label nameLabel = new Label("Nome playlist");
        nameLabel.getStyleClass().add("form-label");
        return new VBox(6, nameLabel, nameField);
    }

    private HBox buildFooter() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("Annulla");
        cancelBtn.getStyleClass().add("cancel-btn");
        cancelBtn.setOnAction(e -> stage.close());

        HBox footer = new HBox(12, spacer, cancelBtn, generateBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("dialog-footer");
        return footer;
    }

    // ── logica ───────────────────────────────────────────────────────────────

    private void onYearChanged(String text) {
        clearStatus();

        if (text == null || text.isBlank()) {
            currentTracks = List.of();
            updatePreview(false);
            updateGenerateButton();
            return;
        }

        int year;
        try {
            year = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            currentTracks = List.of();
            updatePreview(false);
            updateGenerateButton();
            return;
        }

        int currentYear = Year.now().getValue();
        if (year < MIN_YEAR || year > currentYear) {
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
                Label emptyLabel = new Label("Nessuna traccia trovata per questo anno");
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

        try {
            playlistController.createPlaylistByYear(year, name, library);
            if (onSuccess != null) {
                onSuccess.run();
            }
            stage.close();
        } catch (IllegalArgumentException e) {
            statusLabel.setText(e.getMessage());
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        }
    }

    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    private String formatDuration(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
