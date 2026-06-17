package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import com.example.progetto_sad.observer.Observer;
import java.util.function.Consumer;
import com.example.progetto_sad.view.AddPlaylistDialogView;
import com.example.progetto_sad.view.AddTrackDialogView;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * US16 - Controller FXML della schermata "Coda di riproduzione".
 *
 * Gestisce i nodi @FXML e il rendering della vista: stato vuoto, brano corrente,
 * elenco brani successivi e contatore. Non contiene logica di dominio.
 *
 * Puo' operare in due modalita':
 * - standalone/demo: i metodi pubblici di presentazione ({@link #setCurrentTrack},
 *   {@link #addNextTrack}, ecc.) vengono chiamati direttamente da {@code QueueView.App};
 * - reale: {@link #setSequenceController} riceve il {@link PlaylistSequenceController}
 *   condiviso e {@link #refresh()} legge i dati da esso per aggiornare la UI.
 *   Il wiring verra' completato nel task INT.
 */
public class QueueController implements Observer {

    private Runnable onBackAction;
    private Consumer<Track> onPlayTrackAction;
    private Runnable onRemoveCurrentTrackAction;
    private PlaylistSequenceController seqController;
    private Player player;
    private TrackLibrary trackLibrary;
    private PlaylistManager playlistManager;

    @FXML private Label trackCountLabel;
    @FXML private VBox emptyStateVBox;
    @FXML private ScrollPane queueScrollPane;
    @FXML private Label currentSectionLabel;
    @FXML private HBox currentTrackRow;
    @FXML private Label currentTitleLabel;
    @FXML private Label currentAuthorLabel;
    @FXML private Label currentDurationLabel;
    @FXML private Label nextSectionLabel;
    @FXML private VBox nextTracksVBox;
    @FXML private PlayerBarController playerBarController;

    @FXML
    private void initialize() {
        bindPlayerBar();
    }

    /**
     * Imposta l'azione da eseguire alla pressione del pulsante "← Indietro".
     *
     * @param action l'azione di navigazione indietro
     */
    public void setOnBackAction(Runnable action) {
        this.onBackAction = action;
    }

    /**
     * US12 - Imposta il callback da invocare quando l'utente fa doppio click su una traccia.
     * Il callback riceve la traccia selezionata e delega la riproduzione al chiamante,
     * che conosce Player.
     *
     * @param action callback che riceve la traccia su cui è avvenuto il doppio click
     */
    public void setOnPlayTrackAction(Consumer<Track> action) {
        this.onPlayTrackAction = action;
    }

    /**
     * Imposta il callback da invocare quando l'utente clicca la X sulla riga
     * "In riproduzione". Il chiamante e' responsabile di rimuovere la traccia corrente
     * dalla sequenza e di aggiornare il Player di conseguenza.
     *
     * @param action il callback di rimozione della traccia corrente
     */
    public void setOnRemoveCurrentTrackAction(Runnable action) {
        this.onRemoveCurrentTrackAction = action;
    }

    @FXML
    private void handleBack() {
        if (onBackAction != null) {
            onBackAction.run();
        }
    }

    @FXML
    private void onAddTrack() {
        if (trackLibrary == null || seqController == null
                || trackCountLabel == null || trackCountLabel.getScene() == null) {
            return;
        }

        Scene scene = trackCountLabel.getScene();
        Parent queueRoot = scene.getRoot();
        AddTrackDialogView dialogView = new AddTrackDialogView();
        Parent addTrackRoot = dialogView.buildInlineView(
                trackLibrary.getTracks(),
                "Aggiungi traccia alla coda",
                seqController::addToQueue,
                player, seqController,
                () -> scene.setRoot(queueRoot),
                () -> scene.setRoot(queueRoot)
        );
        scene.setRoot(addTrackRoot);
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

    /**
     * Inietta il controller della sequenza condivisa. Deve essere chiamato prima di
     * {@link #refresh()} nella modalita' reale. Se {@code controller} e' null,
     * {@link #refresh()} non ha effetto (la modalita' standalone rimane invariata).
     *
     * @param controller il controller della sequenza di riproduzione condivisa
     */
    public void setSequenceController(PlaylistSequenceController controller) {
        if (this.seqController != null) {
            this.seqController.detach(this);
        }
        this.seqController = controller;
        if (this.seqController != null) {
            this.seqController.attach(this);
        }
        bindPlayerBar();
    }

    /**
     * Inietta il Player condiviso. La sezione "In riproduzione" della Coda deve
     * derivare dal Player, non dalla prima traccia presente nella sequenza.
     *
     * @param player il player condiviso dell'applicazione
     */
    public void setPlayer(Player player) {
        if (this.player != null) {
            this.player.detach(this);
        }
        this.player = player;
        if (this.player != null) {
            this.player.attach(this);
        }
        bindPlayerBar();
    }

    /**
     * Inietta la libreria da cui selezionare le tracce da aggiungere alla coda.
     *
     * @param library la libreria condivisa delle tracce
     */
    public void setTrackLibrary(TrackLibrary library) {
        this.trackLibrary = library;
    }

    /**
     * Inietta il gestore delle playlist da cui selezionare una playlist da aggiungere
     * alla coda. Deve essere chiamato prima dell'uso del comando "+ Playlist".
     *
     * @param manager il gestore condiviso delle playlist
     */
    public void setPlaylistManager(PlaylistManager manager) {
        this.playlistManager = manager;
    }

    private void bindPlayerBar() {
        if (playerBarController != null) {
            playerBarController.bind(player, seqController, () -> null,
                    () -> seqController != null ? seqController.getCurrentTrack() : null);
        }
    }

    @FXML
    private void onAddPlaylist() {
        if (playlistManager == null || seqController == null
                || trackCountLabel == null || trackCountLabel.getScene() == null) {
            return;
        }

        AddPlaylistDialogView dialog = new AddPlaylistDialogView();
        dialog.initForQueue(
                playlistManager.getPlaylists(),
                seqController::addPlaylistToQueue,
                trackCountLabel.getScene().getWindow()
        );
        dialog.show();
    }

    /**
     * Aggiorna la vista quando la sequenza condivisa cambia.
     */
    @Override
    public void update() {
        if (Platform.isFxApplicationThread()) {
            refresh();
        } else {
            Platform.runLater(this::refresh);
        }
    }

    /**
     * Aggiorna la UI leggendo la traccia in riproduzione dal Player e le tracce
     * accodate dalla sequenza. La prima traccia della sequenza non viene mai
     * promossa a "In riproduzione" se il Player non la sta realmente suonando.
     */
    public void refresh() {
        if (seqController == null && player == null) {
            return;
        }
        Track playingTrack = getActivePlayerTrack();
        boolean playbackAlignedWithSequence = isPlaybackAlignedWithSequence(playingTrack);
        List<Track> queuedTracks = resolveQueuedTracks(playingTrack, playbackAlignedWithSequence);
        int totale = queuedTracks.size() + (playingTrack != null ? 1 : 0);

        if (totale == 0) {
            setTrackCount(0);
            setCurrentTrackVisible(false);
            if (nextTracksVBox != null) {
                nextTracksVBox.getChildren().clear();
            }
            showEmpty(true);
            return;
        }

        showEmpty(false);
        setTrackCount(totale);
        renderCurrentTrack(playingTrack, playbackAlignedWithSequence);
        renderQueuedTracks(queuedTracks, playingTrack != null, playbackAlignedWithSequence);
    }

    private Track getActivePlayerTrack() {
        if (player == null || player.getCurrentTrack() == null) {
            return null;
        }
        Player.PlayerState state = player.getState();
        if (state == Player.PlayerState.IN_RIPRODUZIONE || state == Player.PlayerState.IN_PAUSA) {
            return player.getCurrentTrack();
        }
        return null;
    }

    private boolean isPlaybackAlignedWithSequence(Track playingTrack) {
        return playingTrack != null && seqController != null && seqController.getCurrentTrack() == playingTrack;
    }

    private List<Track> resolveQueuedTracks(Track playingTrack, boolean playbackAlignedWithSequence) {
        if (seqController == null || seqController.getSequence() == null
                || seqController.getSequence().isFinished()) {
            return List.of();
        }
        if (playbackAlignedWithSequence) {
            return seqController.getNextTracks();
        }

        List<Track> queuedTracks = getPendingSequenceTracks();
        if (playingTrack != null) {
            queuedTracks.remove(playingTrack);
        }
        return queuedTracks;
    }

    private List<Track> getPendingSequenceTracks() {
        List<Track> tracks = seqController.getSequence().getTracks();
        int start = Math.max(0, Math.min(seqController.getSequence().getCurrentIndex(), tracks.size()));
        return new ArrayList<>(tracks.subList(start, tracks.size()));
    }

    private void renderCurrentTrack(Track playingTrack, boolean playbackAlignedWithSequence) {
        boolean hasCurrentTrack = playingTrack != null;
        setCurrentTrackVisible(hasCurrentTrack);
        if (!hasCurrentTrack) {
            return;
        }

        setCurrentTrack(playingTrack.getTitle(), playingTrack.getAuthor(), formatDuration(playingTrack.getDuration()));

        if (currentTrackRow == null) {
            return;
        }
        currentTrackRow.getChildren().removeIf(n -> n instanceof Button);
        if (playbackAlignedWithSequence) {
            if (onRemoveCurrentTrackAction != null) {
                Button removeCurrentBtn = new Button("✕");
                removeCurrentBtn.getStyleClass().add("remove-btn");
                removeCurrentBtn.setOnAction(e -> onRemoveCurrentTrackAction.run());
                // Consuma il mouse click per evitare che l'evento bubbli alla riga
                removeCurrentBtn.setOnMouseClicked(Event::consume);
                currentTrackRow.getChildren().add(removeCurrentBtn);
            }

            if (onPlayTrackAction != null) {
                final Track currentRef = playingTrack;
                currentTrackRow.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !(e.getTarget() instanceof ButtonBase)) {
                        onPlayTrackAction.accept(currentRef);
                    }
                });
            } else {
                currentTrackRow.setOnMouseClicked(null);
            }
        } else {
            currentTrackRow.setOnMouseClicked(null);
        }
    }

    private void setCurrentTrackVisible(boolean visible) {
        if (currentSectionLabel != null) {
            currentSectionLabel.setVisible(visible);
            currentSectionLabel.setManaged(visible);
        }
        if (currentTrackRow != null) {
            currentTrackRow.setVisible(visible);
            currentTrackRow.setManaged(visible);
            if (!visible) {
                currentTrackRow.getChildren().removeIf(n -> n instanceof Button);
                currentTrackRow.setOnMouseClicked(null);
            }
        }
    }

    private void renderQueuedTracks(List<Track> tracks, boolean hasPlayingTrack,
                                    boolean playbackAlignedWithSequence) {
        if (nextSectionLabel != null) {
            nextSectionLabel.setText(hasPlayingTrack ? "Successivi" : "In coda");
        }
        if (nextTracksVBox != null) {
            nextTracksVBox.getChildren().clear();
        }
        for (int i = 0; i < tracks.size(); i++) {
            Track t = tracks.get(i);
            Integer removableNextIndex = playbackAlignedWithSequence ? i : null;
            HBox row = buildNextTrackRow(i + 1, t.getTitle(), t.getAuthor(),
                    formatDuration(t.getDuration()), removableNextIndex);
            // US12 - doppio click per avviare il brano successivo selezionato.
            // Guard: ignora se l'evento proviene da un ButtonBase (es. click sulla X).
            if (onPlayTrackAction != null) {
                final Track trackRef = t;
                row.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !(e.getTarget() instanceof ButtonBase)) {
                        onPlayTrackAction.accept(trackRef);
                    }
                });
            }
            nextTracksVBox.getChildren().add(row);
        }
    }

    private String formatDuration(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private HBox buildNextTrackRow(int index, String title, String author, String duration) {
        return buildNextTrackRow(index, title, author, duration, null);
    }

    private HBox buildNextTrackRow(int index, String title, String author,
                                   String duration, Integer nextIndex) {
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

        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("remove-btn");
        if (nextIndex == null || seqController == null) {
            removeBtn.setDisable(true);
        } else {
            removeBtn.setOnAction(e -> seqController.removeNextTrackAt(nextIndex));
            // Consuma il mouse click per evitare che l'evento bubbli alla riga
            // e venga interpretato come doppio click sulla riga stessa.
            removeBtn.setOnMouseClicked(Event::consume);
        }

        HBox row = new HBox(12, numLabel, info, durationLabel, removeBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add("track-row");
        return row;
    }
}
