package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.PlaylistController;
import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.observer.Observer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.List;

/**
 * View responsabile della visualizzazione del contenuto di una playlist.
 * Mostra le tracce associate, aggiorna la UI quando il Model cambia
 * e gestisce le azioni dell'utente sulla playlist selezionata.
 */
public class PlaylistView implements Observer {

    @FXML private Label playlistNameLabel;
    @FXML private Label summaryLabel;
    @FXML private VBox trackListVBox;
    @FXML private Label emptyLabel;
    @FXML private Button addTrackBtn;

    private Runnable onBackAction;

    private Playlist playlist;
    private PlaylistController controller;
    private HBox selectedRow;
    private List<Track> availableTracks = List.of();

    public static Parent load(
            Playlist playlist,
            PlaylistController controller,
            List<Track> availableTracks,
            Runnable onBackAction
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    PlaylistView.class.getResource("/com/example/progetto_sad/view/playlist-view.fxml")
            );
            Parent root = loader.load();
            PlaylistView view = loader.getController();
            view.setAvailableTracks(availableTracks);
            view.setOnBackAction(onBackAction);
            view.init(playlist, controller);
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare playlist-view.fxml", e);
        }
    }

    /**
     * Imposta l'elenco delle tracce disponibili che possono essere aggiunte
     * alla playlist visualizzata.
     *
     * @param tracks la lista delle tracce disponibili
     */
    public void setAvailableTracks(List<Track> tracks) {
        this.availableTracks = tracks != null ? tracks : List.of();
    }

    /**
     * Inizializza la vista con la playlist da mostrare e il relativo controller.
     * La vista viene registrata come Observer della playlist e aggiorna
     * immediatamente il contenuto visualizzato.
     *
     * @param playlist la playlist da visualizzare
     * @param controller il controller usato per gestire le operazioni sulla playlist
     */
    public void init(Playlist playlist, PlaylistController controller) {
        this.playlist = playlist;
        this.controller = controller;
        if (playlist != null) {
            playlist.attach(this);
        }
        if (addTrackBtn != null) {
            addTrackBtn.setOnAction(e -> openAddTrackDialog());
        }
        refresh();
    }

    /**
     * Aggiorna la vista quando la playlist osservata cambia.
     * L'aggiornamento viene eseguito sul JavaFX Application Thread.
     */
    @Override
    public void update() {
        Platform.runLater(this::refresh);
    }

    /**
     * Imposta l'azione da eseguire quando l'utente richiede di tornare
     * alla schermata precedente.
     *
     * @param onBackAction l'azione da eseguire alla pressione del pulsante indietro
     */
    public void setOnBackAction(Runnable onBackAction) {
        this.onBackAction = onBackAction;
    }

    /**
     * Forza l'aggiornamento della vista e mostra lo stato corrente della playlist.
     */
    public void display() {
        refresh();
    }

    private void refresh() {
        if (controller == null || playlistNameLabel == null || summaryLabel == null
                || trackListVBox == null || emptyLabel == null) {
            return;
        }

        if (addTrackBtn != null) {
            boolean canAdd = playlist != null && !availableTracks.isEmpty();
            addTrackBtn.setDisable(!canAdd);
        }

        if (playlist == null) {
            playlistNameLabel.setText("—");
            summaryLabel.setText("");
            trackListVBox.getChildren().clear();
            showEmpty(true);
            return;
        }

        playlistNameLabel.setText(playlist.getName());

        List<Track> tracks = controller.getPlaylistTracks(playlist);
        int totalSeconds = tracks.stream().mapToInt(Track::getDuration).sum();
        summaryLabel.setText(tracks.size() + " tracce · " + formatDuration(totalSeconds) + " totali");

        trackListVBox.getChildren().clear();
        selectedRow = null;

        if (controller.isPlaylistEmpty(playlist)) {
            showEmpty(true);
            return;
        }

        showEmpty(false);
        for (Track t : tracks) {
            trackListVBox.getChildren().add(buildTrackRow(t));
        }
    }

    private HBox buildTrackRow(Track t) {
        Label icon = new Label("♪");
        icon.getStyleClass().add("track-meta");
        icon.setMinWidth(20);

        Label titleLabel = new Label(t.getTitle());
        titleLabel.getStyleClass().add("track-title");

        Label metaLabel = new Label(t.getAuthor() + " · " + t.getGenre() + " · " + t.getYear());
        metaLabel.getStyleClass().add("track-meta");

        VBox info = new VBox(2, titleLabel, metaLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label durationLabel = new Label(formatDuration(t.getDuration()));
        durationLabel.getStyleClass().add("track-duration");

        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("remove-btn");
        removeBtn.setOnAction(e -> confirmAndRemoveTrack(t));
        removeBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

        HBox row = new HBox(12, icon, info, durationLabel, removeBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add("track-row");

        row.setOnMouseClicked(e -> selectRow(row));

        return row;
    }

    private void selectRow(HBox row) {
        if (selectedRow != null) {
            selectedRow.getStyleClass().remove("track-row-selected");
            if (!selectedRow.getStyleClass().contains("track-row")) {
                selectedRow.getStyleClass().add("track-row");
            }
        }
        row.getStyleClass().remove("track-row");
        row.getStyleClass().add("track-row-selected");
        selectedRow = row;
    }

    private void showEmpty(boolean empty) {
        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);
        trackListVBox.setVisible(!empty);
        trackListVBox.setManaged(!empty);
    }

    private void openAddTrackDialog() {
        if (playlist == null || controller == null || availableTracks.isEmpty()
                || addTrackBtn == null || addTrackBtn.getScene() == null) {
            return;
        }
        Window owner = addTrackBtn.getScene().getWindow();
        AddTrackDialogView dialog = new AddTrackDialogView();
        dialog.init(availableTracks, playlist, controller, owner);
        dialog.show();
    }

    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void confirmAndRemoveTrack(Track track) {
        if (track == null || playlist == null || controller == null) {
            return;
        }

        ButtonType cancelButton = new ButtonType("Annulla");
        ButtonType removeButton = new ButtonType("Rimuovi");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma rimozione");
        alert.setHeaderText("Rimuovere la traccia dalla playlist?");
        alert.setContentText(
                "Vuoi davvero rimuovere \"" + track.getTitle() + "\" dalla playlist \""
                        + playlist.getName() + "\"?"
        );

        alert.getButtonTypes().setAll(cancelButton, removeButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == removeButton) {
                controller.removeTrackFromPlaylist(track, playlist);
            }
        });
    }

    @FXML
    private void handleBack() {
        if (onBackAction != null) {
            onBackAction.run();
        }
    }
}
