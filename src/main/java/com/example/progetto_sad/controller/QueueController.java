package com.example.progetto_sad.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * US16 - Controller della schermata "Coda di riproduzione".
 *
 * Per questo task gestisce solo la presentazione del layout FXML (nomi, righe, stati).
 * Il collegamento alla sequenza condivisa e il pattern Observer verranno aggiunti
 * nel task successivo [US16-UI] "Collegare il controller del pannello Coda alla sequenza condivisa".
 */
public class QueueController {

    private Runnable onBackAction;

    @FXML private Label trackCountLabel;
    @FXML private VBox emptyStateVBox;
    @FXML private ScrollPane queueScrollPane;
    @FXML private HBox currentTrackRow;
    @FXML private Label currentTitleLabel;
    @FXML private Label currentAuthorLabel;
    @FXML private Label currentDurationLabel;
    @FXML private VBox nextTracksVBox;

    /**
     * Imposta l'azione da eseguire alla pressione del pulsante "← Indietro".
     *
     * @param action l'azione di navigazione indietro
     */
    public void setOnBackAction(Runnable action) {
        this.onBackAction = action;
    }

    @FXML
    private void handleBack() {
        if (onBackAction != null) {
            onBackAction.run();
        }
    }

    // [INT] - handler predisposto per il pulsante "+ Brano"; il collegamento al dialog
    // di aggiunta traccia alla coda verra' implementato nel task di integrazione.
    @FXML
    private void onAddTrack() {
        // TODO: [INT] collegare al dialog di aggiunta brano alla coda
    }

    /**
     * Mostra lo stato "coda vuota" o il contenuto della coda.
     *
     * @param empty {@code true} per mostrare il messaggio di coda vuota
     */
    public void showEmpty(boolean empty) {
        emptyStateVBox.setVisible(empty);
        emptyStateVBox.setManaged(empty);
        queueScrollPane.setVisible(!empty);
        queueScrollPane.setManaged(!empty);
    }

    /**
     * Imposta il contatore tracce visualizzato nell'header.
     *
     * @param count numero di tracce nella coda
     */
    public void setTrackCount(int count) {
        if (trackCountLabel != null) {
            trackCountLabel.setText(count + (count == 1 ? " traccia" : " tracce"));
        }
    }

    /**
     * Imposta i dati del brano attualmente in riproduzione nella sezione "In riproduzione".
     *
     * @param title    titolo del brano
     * @param author   autore/artista
     * @param duration durata formattata (es. "3:41")
     */
    public void setCurrentTrack(String title, String author, String duration) {
        if (currentTitleLabel != null)    currentTitleLabel.setText(title);
        if (currentAuthorLabel != null)   currentAuthorLabel.setText(author);
        if (currentDurationLabel != null) currentDurationLabel.setText(duration);
    }

    /**
     * Aggiunge una riga nella sezione "Successivi".
     * Il pulsante di rimozione e' predisposto graficamente ma non collegato a logica reale
     * in questa card: la rimozione dalla coda verra' implementata in un task successivo.
     *
     * @param index    numero progressivo della traccia nella lista
     * @param title    titolo del brano
     * @param author   autore/artista
     * @param duration durata formattata (es. "3:20")
     */
    public void addNextTrack(int index, String title, String author, String duration) {
        if (nextTracksVBox == null) {
            return;
        }
        nextTracksVBox.getChildren().add(buildNextTrackRow(index, title, author, duration));
    }

    private HBox buildNextTrackRow(int index, String title, String author, String duration) {
        Label numLabel = new Label(String.valueOf(index));
        numLabel.getStyleClass().add("track-number");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("track-title");

        Label authorLabel = new Label(author);
        authorLabel.getStyleClass().add("track-meta");

        VBox info = new VBox(3, titleLabel, authorLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label durationLabel = new Label(duration);
        durationLabel.getStyleClass().add("track-duration");

        // pulsante rimozione predisposto graficamente; disabilitato finche' non collegato alla coda reale
        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("remove-btn");
        removeBtn.setDisable(true);

        HBox row = new HBox(12, numLabel, info, durationLabel, removeBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add("track-row");
        return row;
    }
}
