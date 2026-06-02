package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.view.PlaylistView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

/**
 * US3/US4 - Controller della schermata principale (home / "Libreria tracce").
 *
 * Controller FXML della vista {@code LibraryView.fxml}: mostra l'elenco delle tracce
 * della libreria (US4), permette di eliminarle dalla riga con la "x" (US3, con
 * rimozione in cascata dalle playlist), di aprire il form "Aggiungi traccia" (US1),
 * di creare nuove playlist (US5) e di navigare al contenuto di una playlist (US8).
 * Osserva la {@link TrackLibrary} (pattern Observer) per aggiornare la lista quando
 * il modello cambia.
 */
public class LibraryController implements Observer {

    private static final String ADD_TRACK_FXML = "/com/example/progetto_sad/view/add-track-view.fxml";

    private final TrackLibrary library;
    private final TrackController trackController;
    private final PlaylistManager playlistManager;

    @FXML private VBox trackListVBox;
    @FXML private VBox playlistListVBox;

    /**
     * @param library         libreria delle tracce mostrata nella tabella
     * @param trackController controller applicativo per creare/eliminare tracce
     * @param playlistManager gestore delle playlist (sidebar e navigazione)
     */
    public LibraryController(TrackLibrary library, TrackController trackController,
                             PlaylistManager playlistManager) {
        this.library = library;
        this.trackController = trackController;
        this.playlistManager = playlistManager;
    }

    @FXML
    private void initialize() {
        library.attach(this);
        refreshTracks();
        refreshPlaylists();
    }

    /**
     * US4 - Aggiorna la tabella tracce quando la libreria cambia (Observer).
     * L'aggiornamento viene eseguito sul JavaFX Application Thread.
     */
    @Override
    public void update() {
        Platform.runLater(this::refreshTracks);
    }

    /* ===== US4 - tabella tracce ===== */

    private void refreshTracks() {
        if (trackListVBox == null) {
            return;
        }
        trackListVBox.getChildren().clear();
        for (Track t : trackController.getTracks()) {
            trackListVBox.getChildren().add(buildTrackRow(t));
        }
    }

    private HBox buildTrackRow(Track t) {
        Label title = new Label(t.getTitle());
        title.getStyleClass().add("cell-title");
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);

        HBox row = new HBox(
                title,
                cell(t.getAuthor(), 160),
                cell(t.getGenre(), 120),
                cell(String.valueOf(t.getYear()), 70),
                cell(formatDuration(t.getDuration()), 80),
                buildDeleteButton(t)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.getStyleClass().add("track-row");
        return row;
    }

    private Label cell(String text, double width) {
        Label label = new Label(text);
        label.getStyleClass().add("cell-meta");
        label.setMinWidth(width);
        label.setPrefWidth(width);
        return label;
    }

    private Button buildDeleteButton(Track t) {
        Button delete = new Button("✕");
        delete.getStyleClass().add("delete-btn");
        delete.setMinWidth(50);
        delete.setPrefWidth(50);
        delete.setOnAction(e -> onDeleteTrack(t));
        return delete;
    }

    /* ===== US3 - eliminazione traccia dalla riga ===== */

    private void onDeleteTrack(Track t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(currentWindow());
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Eliminare la traccia \"" + t.getTitle() + "\"?");
        alert.setContentText("Verra' rimossa anche da tutte le playlist in cui compare.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                trackController.deleteTrack(t); // la libreria notifica l'Observer -> refresh automatico
            }
        });
    }

    /* ===== US1 - apertura form "Aggiungi traccia" ===== */

    @FXML
    private void onAddTrack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ADD_TRACK_FXML));
            loader.setControllerFactory(type -> new AddTrackController(trackController));
            Parent form = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(currentWindow());
            dialog.setTitle("Aggiungi traccia");
            dialog.setScene(new Scene(form, 900, 640));
            dialog.showAndWait(); // al salvataggio la libreria notifica -> refreshTracks() automatico
        } catch (IOException e) {
            showError("Impossibile aprire il form: " + e.getMessage());
        }
    }

    /* ===== US5 - creazione nuova playlist ===== */

    @FXML
    private void onNewPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(currentWindow());
        dialog.setTitle("Nuova playlist");
        dialog.setHeaderText("Crea una nuova playlist");
        dialog.setContentText("Nome:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                playlistManager.createPlaylist(name);
                refreshPlaylists();
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        });
    }

    /* ===== US8 - sidebar playlist + navigazione ===== */

    private void refreshPlaylists() {
        if (playlistListVBox == null) {
            return;
        }
        playlistListVBox.getChildren().clear();
        for (Playlist p : playlistManager.getPlaylists()) {
            Label item = new Label("♪  " + p.getName());
            item.getStyleClass().add("playlist-item");
            item.setMaxWidth(Double.MAX_VALUE);
            item.setOnMouseClicked(e -> openPlaylist(p));
            playlistListVBox.getChildren().add(item);
        }
    }

    // US8 - naviga al contenuto della playlist scambiando il root della scena;
    // l'azione "indietro" ripristina la schermata libreria.
    private void openPlaylist(Playlist playlist) {
        Scene scene = (trackListVBox != null) ? trackListVBox.getScene() : null;
        if (scene == null) {
            return;
        }
        Parent libraryRoot = scene.getRoot();
        PlaylistController playlistController = new PlaylistController(playlistManager);
        Parent playlistRoot = PlaylistView.load(
                playlist, playlistController, trackController.getTracks(),
                () -> scene.setRoot(libraryRoot));
        scene.setRoot(playlistRoot);
    }

    /* ===== util ===== */

    private String formatDuration(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private Window currentWindow() {
        return (trackListVBox != null && trackListVBox.getScene() != null)
                ? trackListVBox.getScene().getWindow() : null;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(currentWindow());
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
