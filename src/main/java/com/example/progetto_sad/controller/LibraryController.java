package com.example.progetto_sad.controller;

import com.example.progetto_sad.command.CommandManager;
import com.example.progetto_sad.command.CreatePlaylistCommand;
import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
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
 * US3/US4/US9 - Controller della schermata principale (home / "Libreria tracce").
 * Gestisce la Player Bar con un layout moderno: Stop (reset) e Play/Pausa dinamico [INT-C].
 */
public class LibraryController implements Observer {

    private static final String ADD_TRACK_FXML = "/com/example/progetto_sad/view/add-track-view.fxml";
    private static final String MODIFICA_TRACK_FXML = "/com/example/progetto_sad/view/modifica-track-view.fxml";

    private final TrackLibrary library;
    private final TrackController trackController;
    private final PlaylistManager playlistManager;
    private final PlaylistSequenceController seqController; // US14
    private final Player player; // [INT-C] Player condiviso

    // US22 - storico comandi CONDIVISO dell'applicazione: il pulsante "Annulla"
    // della home e i controller che eseguono operazioni annullabili agiscono
    // tutti sulla stessa cronologia (un solo Invoker).
    private final CommandManager commandManager;

    private Track selectedTrack;
    private final Observer playerObserver;
    private final AtomicBoolean playerBarRefreshScheduled;
    private boolean updatingProgressSlider;
    private boolean userSeekingProgress;

    @FXML private VBox trackListVBox;
    @FXML private VBox playlistListVBox;
    @FXML private TextField searchField;
    @FXML private Label playerTitleLabel;
    @FXML private Label playerMetaLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label durationLabel;
    @FXML private Slider playerProgressSlider;
    @FXML private Button sequentialModeButton;
    @FXML private Button shuffleModeButton;

    // I due bottoni del nuovo design
    @FXML private Button playButton;
    @FXML private Button stopButton;
    @FXML private Button undoBtn;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button skipPlaylistButton;

    public LibraryController(TrackLibrary library, TrackController trackController,
                             PlaylistManager playlistManager,
                             PlaylistSequenceController seqController,
                             Player player) {
        this(library, trackController, playlistManager, seqController, player,
                new CommandManager());
    }

    /**
     * US22 - Variante con lo storico comandi condiviso dell'applicazione, usata
     * dal composition root ({@code LibraryView.load}) cosi' che il pulsante
     * "Annulla" agisca su un'unica cronologia per tutte le operazioni annullabili.
     *
     * @param commandManager l'Invoker condiviso che esegue e registra i comandi
     * @throws IllegalArgumentException se commandManager e' null
     */
    public LibraryController(TrackLibrary library, TrackController trackController,
                             PlaylistManager playlistManager,
                             PlaylistSequenceController seqController,
                             Player player, CommandManager commandManager) {
        if (commandManager == null) {
            throw new IllegalArgumentException("Il command manager non puo' essere null");
        }
        this.library = library;
        this.trackController = trackController;
        this.playlistManager = playlistManager;
        this.seqController = seqController;
        this.player = player;
        this.commandManager = commandManager;
        this.playerObserver = this::requestPlayerBarRefresh;
        this.playerBarRefreshScheduled = new AtomicBoolean(false);
        this.updatingProgressSlider = false;
        this.userSeekingProgress = false;
    }

    /**
     * US22 - Annulla l'ultima operazione: la View delega tutto all'Invoker
     * ({@link CommandManager#undo()}), nessuna logica di annullamento qui.
     * La libreria si aggiorna da sola (Observer su TrackLibrary); la sidebar
     * delle playlist va rinfrescata esplicitamente, come nelle altre azioni,
     * perche' PlaylistManager non e' un Subject osservabile.
     */
    @FXML
    private void onUndo() {
        commandManager.undo();
        refreshPlaylists();
        refreshUndoButton();
    }

    @FXML
    private void initialize() {
        library.attach(this);
        if (searchField != null) {
            limitLength(searchField, 20);
        }
        initializePlayerBar();
        refreshTracks();
        refreshPlaylists();
        refreshUndoButton();
    }

    @Override
    public void update() {
        Platform.runLater(this::refreshLibraryView);
    }

    private void refreshLibraryView() {
        refreshTracks();
        syncQueueWithLibrary();
        syncPlayerBarWithLibrary();
        refreshUndoButton();
    }

    private void refreshTracks() {
        if (trackListVBox == null) return;
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
        edit.setScaleX(-1);
        edit.setOnAction(e -> openEditTrack(t));
        return edit;
    }

    private void onDeleteTrack(Track t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(currentWindow());
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Eliminare la traccia \"" + t.getTitle() + "\"?");
        alert.setContentText("Verra' rimossa anche da tutte le playlist in cui compare.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                trackController.deleteTrack(t);
            }
        });
    }

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
            dialog.showAndWait();
        } catch (IOException e) {
            showError("Impossibile aprire la schermata di modifica: " + e.getMessage());
        }
    }

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
            dialog.showAndWait();
        } catch (IOException e) {
            showError("Impossibile aprire il form: " + e.getMessage());
        }
    }

    @FXML
    private void onNewPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        limitLength(dialog.getEditor(), 20);
        dialog.initOwner(currentWindow());
        dialog.setTitle("Nuova playlist");
        dialog.setHeaderText("Crea una nuova playlist");
        dialog.setContentText("Nome:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                // US22 - anche la creazione dalla home transita dallo storico condiviso
                // come comando annullabile (prima chiamava il manager direttamente).
                commandManager.executeCommand(new CreatePlaylistCommand(playlistManager, name));
                refreshPlaylists();
                refreshUndoButton();
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        });
    }

    private void refreshPlaylists() {
        if (playlistListVBox == null) return;
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
        if (scene == null) return;

        Parent libraryRoot = scene.getRoot();
        QueueController queueController = new QueueController();
        queueController.setSequenceController(seqController);
        queueController.setPlayer(player);
        queueController.setTrackLibrary(library);
        queueController.setPlaylistManager(playlistManager);
        // US12 - doppio click su una traccia della coda: sposta la sequenza e avvia la riproduzione
        queueController.setOnPlayTrackAction(track -> {
            Track resolved = seqController.goToTrack(track);
            if (resolved != null) {
                player.play(resolved);
            }
            requestPlayerBarRefresh();
        });
        // X sulla riga "In riproduzione": rimuove la traccia corrente dalla sequenza.
        // Se esiste una successiva, la avvia; altrimenti ferma il Player.
        queueController.setOnRemoveCurrentTrackAction(() -> {
            Track next = seqController.removeCurrentTrack();
            if (next != null) {
                player.play(next);
            } else {
                player.stop();
            }
            requestPlayerBarRefresh();
        });
        Parent queueRoot = QueueView.load(queueController, () -> {
            queueController.setSequenceController(null);
            queueController.setPlayer(null);
            scene.setRoot(libraryRoot);
        });
        queueController.refresh();
        scene.setRoot(queueRoot);
    }

    private void openPlaylist(Playlist playlist) {
        Scene scene = (trackListVBox != null) ? trackListVBox.getScene() : null;
        if (scene == null) return;
        Parent libraryRoot = scene.getRoot();
        // US22 - la schermata playlist condivide lo stesso storico comandi della home.
        PlaylistController playlistController = new PlaylistController(playlistManager, commandManager);
        Parent playlistRoot = PlaylistView.load(
                playlist, playlistController, trackController.getTracks(),
                () -> {
                    scene.setRoot(libraryRoot);
                    refreshPlaylists();
                    refreshUndoButton();
                });
        scene.setRoot(playlistRoot);
    }

    /**
     * US22 - Sincronizza lo stato visivo del pulsante "Annulla" con lo storico:
     * il bottone e' disabilitato quando non esistono operazioni annullabili.
     */
    private void refreshUndoButton() {
        if (undoBtn != null) {
            undoBtn.setDisable(!commandManager.canUndo());
        }
    }

    /* ===== US9 - Player Bar ===== */

    private void initializePlayerBar() {
        if (player == null) return;
        player.attach(playerObserver);
        if (seqController != null) {
            seqController.attach(playerObserver);
        }
        configureProgressSlider();
        resetProgressSlider();
        refreshPlayerBar();
    }

    private void selectTrack(Track track) {
        selectedTrack = track;
        refreshTracks();
        requestPlayerBarRefresh();
    }

    private void requestPlayerBarRefresh() {
        if (!playerBarRefreshScheduled.compareAndSet(false, true)) {
            return;
        }
        Platform.runLater(() -> {
            playerBarRefreshScheduled.set(false);
            refreshPlayerBar();
        });
    }

    private void refreshPlayerBar() {
        if (player == null) return;
        
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
        } else if (state == Player.PlayerState.IN_PAUSA) {
            setPlayerText(currentTrack.getTitle(), currentTrack.getAuthor() + " • In pausa");
        } else if (currentTrack != null) {
            setPlayerText(currentTrack.getTitle(), currentTrack.getAuthor() + " • Fermata");
        } else {
            setPlayerText(displayedTrack.getTitle(), displayedTrack.getAuthor() + " • Pronta");
        }

        int currentTime = player.getCurrentTime();
        int duration = currentTrack != null ? player.getDuration()
                : (displayedTrack != null ? Math.max(0, displayedTrack.getDuration()) : 0);
        boolean canSeek = currentTrack != null && duration > 0
                && (state == Player.PlayerState.IN_RIPRODUZIONE || state == Player.PlayerState.IN_PAUSA);
        updateProgress(currentTime, duration, canSeek);
        
        // Sincronizza lo stato dei bottoni dinamici
        updatePlayerButtons(state, hasPlayableAudio(displayedTrack));
    }

    private void resetPlayerBar() {
        setPlayerText("—", "Seleziona una traccia");
        updateProgress(0, 0, false);
        updatePlayerButtons(Player.PlayerState.FERMO, false);
    }

    private void syncPlayerBarWithLibrary() {
        boolean selectedTrackRemoved = selectedTrack != null
                && !trackController.getTracks().contains(selectedTrack);
        boolean currentTrackRemoved = player != null
                && player.getCurrentTrack() != null
                && !trackController.getTracks().contains(player.getCurrentTrack());

        if (selectedTrackRemoved) selectedTrack = null;
        if (currentTrackRemoved) player.clearCurrentTrack();
        if (selectedTrackRemoved || currentTrackRemoved) {
            requestPlayerBarRefresh();
        }
    }

    private void syncQueueWithLibrary() {
        if (seqController != null) {
            seqController.removeTracksNotInLibrary(trackController.getTracks());
        }
    }

    private void setPlayerText(String title, String meta) {
        if (playerTitleLabel != null) playerTitleLabel.setText(title);
        if (playerMetaLabel != null) playerMetaLabel.setText(meta);
    }

    private void updateProgress(int currentTime, int duration, boolean canSeek) {
        int safeCurrentTime = Math.max(0, currentTime);
        int safeDuration = Math.max(0, duration);
        if (currentTimeLabel != null) currentTimeLabel.setText(formatDuration(safeCurrentTime));
        if (durationLabel != null) durationLabel.setText(formatDuration(safeDuration));
        if (playerProgressSlider != null) {
            updatingProgressSlider = true;
            playerProgressSlider.setDisable(!canSeek);
            playerProgressSlider.setMax(safeDuration > 0 ? safeDuration : 1);
            if (!userSeekingProgress) {
                playerProgressSlider.setValue(Math.min(safeCurrentTime, safeDuration));
            }
            updatingProgressSlider = false;
        }
    }

    private void configureProgressSlider() {
        if (playerProgressSlider == null) return;
        playerProgressSlider.setFocusTraversable(false);
        playerProgressSlider.valueChangingProperty().addListener((observable, wasChanging, isChanging) -> {
            userSeekingProgress = isChanging;
            if (!isChanging) {
                seekToSliderValue();
            }
        });
        playerProgressSlider.setOnMousePressed(event -> userSeekingProgress = true);
        playerProgressSlider.setOnMouseReleased(event -> {
            userSeekingProgress = false;
            seekToSliderValue();
        });
        playerProgressSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!updatingProgressSlider && userSeekingProgress && currentTimeLabel != null) {
                currentTimeLabel.setText(formatDuration(newValue.intValue()));
            }
        });
    }

    private void seekToSliderValue() {
        if (player == null || playerProgressSlider == null || playerProgressSlider.isDisabled()) {
            return;
        }
        player.seekTo((int) Math.round(playerProgressSlider.getValue()));
        requestPlayerBarRefresh();
    }

    /**
     * Gestisce l'abilitazione e il cambio icona dinamico del pulsante Play [INT-C]
     * e aggiorna i pulsanti Previous/Next (US12) in base alla sequenza corrente.
     */
    private void updatePlayerButtons(Player.PlayerState state, boolean hasAudio) {
        if (playButton == null || stopButton == null) return;

        playButton.setDisable(!hasAudio);

        switch (state) {
            case IN_RIPRODUZIONE:
                playButton.setText("⏸");
                stopButton.setDisable(false);
                break;
            case IN_PAUSA:
                playButton.setText("▶");
                stopButton.setDisable(false);
                break;
            case FERMO:
                playButton.setText("▶");
                stopButton.setDisable(true);
                break;
        }

        if (previousButton != null) {
            previousButton.setDisable(seqController == null || !seqController.hasPreviousTrack());
        }
        if (nextButton != null) {
            nextButton.setDisable(seqController == null || !seqController.hasNextTrack());
        }
        if (skipPlaylistButton != null) {
            skipPlaylistButton.setDisable(seqController == null || !seqController.canSkipPlaylist());
        }
        // US18-UI - la modalità attiva è indicata dalla style class play-mode-btn-active
        if (sequentialModeButton != null) {
            sequentialModeButton.setDisable(false);
            sequentialModeButton.getStyleClass().remove("play-mode-btn-active");
            if (seqController != null && seqController.isSequentialMode()) {
                sequentialModeButton.getStyleClass().add("play-mode-btn-active");
            }
        }
        if (shuffleModeButton != null) {
            shuffleModeButton.setDisable(false);
            shuffleModeButton.getStyleClass().remove("play-mode-btn-active");
            if (seqController != null && seqController.isShuffleMode()) {
                shuffleModeButton.getStyleClass().add("play-mode-btn-active");
            }
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
            playerProgressSlider.setDisable(true);
        }
    }

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

    private void limitLength(TextField field, int maxLength) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return (newText.length() <= maxLength || newText.length() < change.getControlText().length())
                    ? change : null;
        }));
    }
    

    
    /**
     * Tasto dinamico: alterna Play, Pausa e Resume a seconda dello stato.
     */
    @FXML
    private void handlePlayPauseAction() {
        if (player == null) return;

        // Se l'utente clicca su una traccia diversa, azzera la pausa e fa partire la nuova
        if (selectedTrack != null && selectedTrack != player.getCurrentTrack()) {
            player.play(selectedTrack);
        } 
        else if (player.getState() == Player.PlayerState.IN_RIPRODUZIONE) {
            player.pause();
        } 
        else if (player.getState() == Player.PlayerState.IN_PAUSA) {
            player.resume(); 
        } 
        else if (player.getState() == Player.PlayerState.FERMO) {
            Track trackToPlay = selectedTrack != null ? selectedTrack : seqController.getCurrentTrack();
            if (trackToPlay != null) {
                player.play(trackToPlay);
            }
        }
        requestPlayerBarRefresh(); 
    }

    /**
     * US12 - Salta al brano successivo nella sequenza di riproduzione.
     */
    @FXML
    private void onNextTrack() {
        if (seqController == null || player == null) return;
        Track next = seqController.goToNextTrack();
        if (next != null) {
            player.play(next);
        }
        requestPlayerBarRefresh();
    }

    /**
     * US12 - Torna al brano precedente nella sequenza di riproduzione.
     */
    @FXML
    private void onPreviousTrack() {
        if (seqController == null || player == null) return;
        Track prev = seqController.goToPreviousTrack();
        if (prev != null) {
            player.play(prev);
        }
        requestPlayerBarRefresh();
    }

    /**
     * US13 - Salta la parte rimanente della playlist sorgente e avvia la prima traccia accodata.
     */
    @FXML
    private void onSkipPlaylist() {
        if (seqController == null || player == null) return;
        Track next = seqController.skipPlaylist();
        if (next != null) {
            player.play(next);
        }
        requestPlayerBarRefresh();
    }

    /**
     * US17-INT - Imposta esplicitamente la modalità di riproduzione sequenziale.
     */
    @FXML
    private void onSequentialMode() {
        if (seqController == null) return;
        seqController.setSequentialMode();
        requestPlayerBarRefresh();
    }

    /**
     * US18-INT - Imposta la modalità di riproduzione casuale (shuffle).
     */
    @FXML
    private void onShuffleMode() {
        if (seqController == null) return;
        seqController.setShuffleMode();
        requestPlayerBarRefresh();
    }

    /**
     * Tasto reset: arresta l'audio e riporta il timer a 00:00
     */
    @FXML
    private void onStopPlayer() {
        if (player == null) return;
        player.stop(); 
        requestPlayerBarRefresh();
    }
}
