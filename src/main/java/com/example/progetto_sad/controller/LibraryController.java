package com.example.progetto_sad.controller;

import com.example.progetto_sad.command.CommandManager;
import com.example.progetto_sad.command.CreatePlaylistCommand;
import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.view.GeneratePlaylistDialogView;
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

    @FXML private VBox trackListVBox;
    @FXML private VBox playlistListVBox;
    @FXML private TextField searchField;
    @FXML private Button undoBtn;
    @FXML private PlayerBarController playerBarController;

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
    private void onGeneratePlaylist() {
        Scene scene = (trackListVBox != null) ? trackListVBox.getScene() : null;
        if (scene == null) return;

        Parent libraryRoot = scene.getRoot();
        PlaylistController playlistController = new PlaylistController(playlistManager, commandManager);
        GeneratePlaylistDialogView generateView = new GeneratePlaylistDialogView();
        Parent generateRoot = generateView.buildInlineView(
                library, playlistController, player, seqController,
                () -> scene.setRoot(libraryRoot),
                () -> {
                    refreshPlaylists();
                    refreshUndoButton();
                    scene.setRoot(libraryRoot);
                }
        );
        scene.setRoot(generateRoot);
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
        if (playerBarController != null) {
            playerBarController.bind(player, seqController, () -> selectedTrack);
        }
    }

    private void selectTrack(Track track) {
        selectedTrack = track;
        refreshTracks();
        requestPlayerBarRefresh();
    }

    private void requestPlayerBarRefresh() {
        if (playerBarController != null) {
            playerBarController.requestRefresh();
        }
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
}
