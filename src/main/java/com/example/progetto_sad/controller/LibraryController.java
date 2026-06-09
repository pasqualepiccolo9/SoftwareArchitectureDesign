package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.view.PlaylistView;
import com.example.progetto_sad.view.QueueView;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * US3/US4 - Controller della schermata principale (home / "Libreria tracce").
 * Controller FXML della vista {@code LibraryView.fxml}: mostra l'elenco delle tracce
 * della libreria (US4), permette di eliminarle dalla riga con la "x" (US3, con
 * rimozione in cascata dalle playlist), di aprire il form "Aggiungi traccia" (US1),
 * di creare nuove playlist (US5) e di navigare al contenuto di una playlist (US8).
 * Collega inoltre la Player Bar ai metodi del {@link Player} per la riproduzione
 * singola (US9), lasciando la logica di riproduzione al modello.
 * Osserva la {@link TrackLibrary} (pattern Observer) per aggiornare la lista quando
 * il modello cambia.
 */
public class LibraryController implements Observer {

    private static final String ADD_TRACK_FXML = "/com/example/progetto_sad/view/add-track-view.fxml";
    private static final String MODIFICA_TRACK_FXML = "/com/example/progetto_sad/view/modifica-track-view.fxml";

    private final TrackLibrary library;
    private final TrackController trackController;
    private final PlaylistManager playlistManager;
    private final PlaylistSequenceController seqController; // US14
    private final Player player;

    private Track selectedTrack;
    private final Observer playerObserver;
    private final AtomicBoolean playerBarRefreshScheduled;

    @FXML private VBox trackListVBox;
    @FXML private VBox playlistListVBox;
    @FXML private TextField searchField;
    @FXML private Label playerTitleLabel;
    @FXML private Label playerMetaLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label durationLabel;
    @FXML private Slider playerProgressSlider;
    @FXML private Button playButton;
    @FXML private Button stopButton;

    /**
     * @param library         libreria delle tracce mostrata nella tabella
     * @param trackController controller applicativo per creare/eliminare tracce
     * @param playlistManager gestore delle playlist (sidebar e navigazione)
     * @param seqController   controller della sequenza di riproduzione condivisa (US14)
     * @param player          player di dominio condiviso con la Player Bar (US9)
     */
    public LibraryController(TrackLibrary library, TrackController trackController,
                             PlaylistManager playlistManager,
                             PlaylistSequenceController seqController,
                             Player player) {
        this.library = library;
        this.trackController = trackController;
        this.playlistManager = playlistManager;
        this.seqController = seqController;
        this.player = player;
        this.playerObserver = this::requestPlayerBarRefresh;
        this.playerBarRefreshScheduled = new AtomicBoolean(false);
    }

    @FXML
    private void initialize() {
        library.attach(this);
        if (searchField != null) {
            limitLength(searchField, 20); // US5 CA4 - limite caratteri
        }
        initializePlayerBar();
        refreshTracks();
        refreshPlaylists();
    }

    /**
     * US4 - Aggiorna la tabella tracce quando la libreria cambia (Observer).
     * L'aggiornamento viene eseguito sul JavaFX Application Thread.
     */
    @Override
    public void update() {
        Platform.runLater(this::refreshLibraryView);
    }

    /* ===== US4 - tabella tracce ===== */

    private void refreshLibraryView() {
        refreshTracks();
        syncPlayerBarWithLibrary();
    }

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
                buildQueueButton(t),
                buildEditButton(t),
                buildDeleteButton(t)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.getStyleClass().add("track-row");
        if (t == selectedTrack) {
            row.getStyleClass().add("track-row-selected");
        }
        row.setOnMouseClicked(e -> selectTrack(t));
        return row;
    }

    // US14 - bottone "Aggiungi alla Coda": aggiunge la traccia alla sequenza di riproduzione condivisa.
    // Il feedback visivo (testo temporaneo "✓ Aggiunto") non blocca l'UI ed e' gestito
    // tramite PauseTransition per rispettare il thread JavaFX.
    private Button buildQueueButton(Track t) {
        Button btn = new Button("Aggiungi alla Coda");
        btn.getStyleClass().add("queue-btn");
        btn.setOnAction(e -> {
            if (seqController == null) {
                showError("Sequenza di riproduzione non disponibile.");
                return;
            }
            seqController.addToQueue(t);
            btn.setText("✓ Aggiunto");
            btn.setDisable(true);
            PauseTransition pausa = new PauseTransition(Duration.seconds(1.5));
            pausa.setOnFinished(ev -> {
                btn.setText("Aggiungi alla Coda");
                btn.setDisable(false);
            });
            pausa.play();
        });
        return btn;
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

    private Button buildEditButton(Track t) {
        Button edit = new Button("✎");
        edit.getStyleClass().add("edit-btn");
        edit.setMinWidth(50);
        edit.setPrefWidth(50);
        edit.setScaleX(-1); // US2 - specchia il glifo: punta verso sinistra, gomma verso destra
        edit.setOnAction(e -> openEditTrack(t));
        return edit;
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

    /* ===== US2 - apertura schermata "Modifica traccia" ===== */

    private void openEditTrack(Track t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MODIFICA_TRACK_FXML));
            loader.setControllerFactory(type -> new ModificaTrackController(trackController, t));
            Parent form = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(currentWindow());
            dialog.setTitle("Modifica traccia");
            dialog.setScene(new Scene(form, 560, 620));
            dialog.showAndWait(); // al salvataggio la modifica notifica -> refreshTracks() automatico
        } catch (IOException e) {
            showError("Impossibile aprire la schermata di modifica: " + e.getMessage());
        }
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
        limitLength(dialog.getEditor(), 20); // US5 CA4 - limite caratteri sul nome
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

    @FXML
    private void openQueue() {
        Scene scene = (trackListVBox != null) ? trackListVBox.getScene() : null;
        if (scene == null) {
            return;
        }

        Parent libraryRoot = scene.getRoot();
        QueueController queueController = new QueueController();
        queueController.setSequenceController(seqController);
        queueController.setTrackLibrary(library);
        queueController.setPlaylistManager(playlistManager);
        Parent queueRoot = QueueView.load(queueController, () -> {
            queueController.setSequenceController(null);
            scene.setRoot(libraryRoot);
        });
        queueController.refresh();
        scene.setRoot(queueRoot);
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
                () -> {
                    scene.setRoot(libraryRoot);
                    refreshPlaylists(); // US5 - sidebar aggiornata al ritorno (es. dopo eliminazione playlist)
                });
        scene.setRoot(playlistRoot);
    }

    /* ===== US9 - Player Bar ===== */

    private void initializePlayerBar() {
        if (player == null) {
            return;
        }
        player.attach(playerObserver);
        resetProgressSlider();
        refreshPlayerBar();
    }

    private void selectTrack(Track track) {
        selectedTrack = track;
        refreshTracks();
        requestPlayerBarRefresh();
    }

    @FXML
    private void onPlayPlayer() {
        if (player == null) {
            return;
        }
        Track trackToPlay = selectedTrack != null ? selectedTrack : player.getCurrentTrack();
        player.play(trackToPlay);
        requestPlayerBarRefresh();
    }

    @FXML
    private void onStopPlayer() {
        if (player == null) {
            return;
        }
        player.stop();
        requestPlayerBarRefresh();
    }

    /**
     * US21 - Richiede un aggiornamento della Player Bar sul JavaFX Application Thread,
     * "fondendo" (coalescing) piu' richieste ravvicinate in un solo refresh.
     *
     * Le notifiche possono arrivare fitte e da thread diversi (il clock del Player
     * notifica ogni secondo, piu' gli eventi UI Play/Stop/selezione): senza coalescing
     * si accumulerebbero tante Platform.runLater, con sfarfallii e carico inutile sul
     * FX thread. La AtomicBoolean garantisce che sia in coda un solo refresh per volta;
     * il flag viene rimesso a false PRIMA di disegnare, cosi' un cambiamento avvenuto
     * nel frattempo pianifica comunque un nuovo refresh (nessun aggiornamento perso).
     */
    private void requestPlayerBarRefresh() {
        // un refresh e' gia' schedulato: coprira' anche questa richiesta, quindi esco
        if (!playerBarRefreshScheduled.compareAndSet(false, true)) {
            return;
        }
        Platform.runLater(() -> {
            playerBarRefreshScheduled.set(false); // riapre la "coda" prima del refresh: non si perdono update
            refreshPlayerBar();
        });
    }

    private void refreshPlayerBar() {
        if (player == null) {
            return;
        }
        Track currentTrack = player.getCurrentTrack();
        Track displayedTrack = currentTrack != null ? currentTrack : selectedTrack;
        Player.PlayerState state = player.getState();
        boolean isPlaying = state == Player.PlayerState.IN_RIPRODUZIONE;

        if (displayedTrack == null) {
            resetPlayerBar();
            return;
        } else if (isPlaying && currentTrack != null) {
            setPlayerText(currentTrack.getTitle(), currentTrack.getAuthor() + " • In riproduzione");
        } else if (!hasPlayableAudio(displayedTrack)) {
            setPlayerText(displayedTrack.getTitle(), displayedTrack.getAuthor() + " • File audio non disponibile");
        } else if (currentTrack != null) {
            setPlayerText(currentTrack.getTitle(), currentTrack.getAuthor() + " • Fermata");
        } else {
            setPlayerText(displayedTrack.getTitle(), displayedTrack.getAuthor() + " • Pronta");
        }

        int currentTime = player.getCurrentTime();
        int duration = currentTrack != null ? player.getDuration()
                : (displayedTrack != null ? Math.max(0, displayedTrack.getDuration()) : 0);
        updateProgress(currentTime, duration);
        updatePlayerButtons(isPlaying);
    }

    private void resetPlayerBar() {
        setPlayerText("—", "Seleziona una traccia");
        updateProgress(0, 0);
        updatePlayerButtons(false);
    }

    private void syncPlayerBarWithLibrary() {
        boolean selectedTrackRemoved = selectedTrack != null
                && !trackController.getTracks().contains(selectedTrack);
        boolean currentTrackRemoved = player != null
                && player.getCurrentTrack() != null
                && !trackController.getTracks().contains(player.getCurrentTrack());

        if (selectedTrackRemoved) {
            selectedTrack = null;
        }
        if (currentTrackRemoved) {
            player.stop();
        }
        if (selectedTrackRemoved || currentTrackRemoved) {
            requestPlayerBarRefresh();
        }
    }

    private void setPlayerText(String title, String meta) {
        if (playerTitleLabel != null) {
            playerTitleLabel.setText(title);
        }
        if (playerMetaLabel != null) {
            playerMetaLabel.setText(meta);
        }
    }

    private void updateProgress(int currentTime, int duration) {
        int safeCurrentTime = Math.max(0, currentTime);
        int safeDuration = Math.max(0, duration);
        if (currentTimeLabel != null) {
            currentTimeLabel.setText(formatDuration(safeCurrentTime));
        }
        if (durationLabel != null) {
            durationLabel.setText(formatDuration(safeDuration));
        }
        if (playerProgressSlider != null) {
            playerProgressSlider.setMax(safeDuration > 0 ? safeDuration : 1);
            playerProgressSlider.setValue(Math.min(safeCurrentTime, safeDuration));
        }
    }

    private void updatePlayerButtons(boolean isPlaying) {
        if (player == null) {
            if (playButton != null) {
                playButton.setDisable(true);
            }
            if (stopButton != null) {
                stopButton.setDisable(true);
            }
            return;
        }
        Track playableTrack = selectedTrack != null ? selectedTrack : player.getCurrentTrack();
        if (playButton != null) {
            playButton.setDisable(isPlaying || !hasPlayableAudio(playableTrack));
        }
        if (stopButton != null) {
            stopButton.setDisable(!isPlaying);
        }
    }

    private boolean hasPlayableAudio(Track track) {
        return track != null
                && track.getDuration() > 0
                && track.getFilePath() != null
                && !track.getFilePath().isBlank();
    }

    private void resetProgressSlider() {
        if (playerProgressSlider != null) {
            playerProgressSlider.setMin(0);
            playerProgressSlider.setMax(1);
            playerProgressSlider.setValue(0);
        }
    }

    /* ===== util ===== */

    private String formatDuration(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        return String.format("%d:%02d", safeSeconds / 60, safeSeconds % 60);
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

    // US5 CA4 - limita il campo a un numero massimo di caratteri
    private void limitLength(TextField field, int maxLength) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return (newText.length() <= maxLength || newText.length() < change.getControlText().length())
                    ? change : null;
        }));
    }
}
