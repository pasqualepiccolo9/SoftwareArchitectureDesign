package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.view.AddTrackDialogView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.Collections;
import java.util.List;

/**
 * US5-US8 - Controller della schermata "contenuto playlist".
 *
 * Ha un duplice ruolo, coerente con l'MVC del progetto:
 * - controller applicativo: delega a {@link PlaylistManager} / {@link Playlist} le operazioni
 *   su playlist e tracce (creazione, rimozione, aggiunta/rimozione traccia, contenuto);
 * - controller FXML della vista {@code PlaylistView.fxml}: gestisce i nodi @FXML, il rendering
 *   della lista e le azioni dell'utente. Osserva la playlist mostrata (pattern Observer) per
 *   aggiornare la UI quando il modello cambia.
 * Il caricamento dell'FXML e l'avvio sono delegati a {@code view/PlaylistView}.
 */
public class PlaylistController implements Observer {

    private final PlaylistManager manager;

    // Stato della vista: la schermata mostra UNA playlist alla volta.
    private Playlist currentPlaylist;
    private List<Track> availableTracks = List.of();
    private Runnable onBackAction;
    private HBox selectedRow;

    @FXML private Label playlistNameLabel;
    @FXML private Label summaryLabel;
    @FXML private VBox trackListVBox;
    @FXML private Label emptyLabel;
    @FXML private Button addTrackBtn;

    /**
     * Crea il controller con il gestore delle playlist.
     *
     * @param manager il gestore usato per creare e rimuovere playlist
     */
    public PlaylistController(PlaylistManager manager) {
        this.manager = manager;
    }

    /* ===== US5 - creazione/rimozione playlist ===== */

    /**
     * Crea una nuova playlist con il nome specificato.
     *
     * @param name il nome della playlist da creare
     */
    public void createPlaylist(String name) {
        try {
            manager.createPlaylist(name);
            System.out.println("Playlist '" + name + "' creata con successo.");
        } catch (IllegalArgumentException e) {
            System.err.println("Errore UI: " + e.getMessage());
        }
    }

    /**
     * Rimuove la playlist specificata.
     *
     * @param playlist la playlist da rimuovere
     */
    public void removePlaylist(Playlist playlist) {
        if (playlist != null) {
            manager.removePlaylist(playlist);
        }
    }

    /* ===== US6/US7 - aggiunta/rimozione traccia a playlist ===== */

    /**
     * Aggiunge una traccia alla playlist specificata.
     *
     * @param track    la traccia da aggiungere
     * @param playlist la playlist a cui aggiungere la traccia
     */
    public void addTrackToPlaylist(Track track, Playlist playlist) {
        if (playlist != null) {
            playlist.addTrack(track);
        }
    }

    /**
     * Rimuove una traccia dalla playlist specificata.
     *
     * @param track    la traccia da rimuovere
     * @param playlist la playlist da cui rimuovere la traccia
     */
    public void removeTrackFromPlaylist(Track track, Playlist playlist) {
        if (playlist != null) {
            playlist.removeTrack(track);
        }
    }

    /* ===== US8 - contenuto playlist ===== */

    /**
     * Restituisce le tracce contenute nella playlist specificata.
     * Se la playlist e' nulla, restituisce una lista vuota.
     *
     * @param playlist la playlist di cui leggere il contenuto
     * @return le tracce della playlist, oppure una lista vuota se la playlist e' nulla
     */
    public List<Track> getPlaylistTracks(Playlist playlist) {
        if (playlist == null) {
            return Collections.emptyList();
        }
        return playlist.getTracks();
    }

    /**
     * Verifica se la playlist selezionata e' nulla o non contiene tracce.
     *
     * @param playlist la playlist selezionata
     * @return true se la playlist e' nulla o vuota, false altrimenti
     */
    public boolean isPlaylistEmpty(Playlist playlist) {
        return playlist == null || playlist.getTracks().isEmpty();
    }

    /* ===== US8 - controller FXML della vista ===== */

    /**
     * Inizializza la vista con la playlist da mostrare: registra il controller come
     * Observer della playlist e aggiorna immediatamente il contenuto visualizzato.
     *
     * @param playlist la playlist da visualizzare
     */
    public void init(Playlist playlist) {
        this.currentPlaylist = playlist;
        if (playlist != null) {
            playlist.attach(this);
        }
        if (addTrackBtn != null) {
            addTrackBtn.setOnAction(e -> openAddTrackDialog());
        }
        refresh();
    }

    /**
     * Imposta l'elenco delle tracce disponibili che possono essere aggiunte alla
     * playlist visualizzata.
     *
     * @param tracks la lista delle tracce disponibili
     */
    public void setAvailableTracks(List<Track> tracks) {
        this.availableTracks = tracks != null ? tracks : List.of();
    }

    /**
     * Imposta l'azione da eseguire alla pressione del pulsante "indietro".
     *
     * @param onBackAction l'azione di navigazione indietro
     */
    public void setOnBackAction(Runnable onBackAction) {
        this.onBackAction = onBackAction;
    }

    /**
     * Aggiorna la vista quando la playlist osservata cambia. L'aggiornamento viene
     * eseguito sul JavaFX Application Thread.
     */
    @Override
    public void update() {
        Platform.runLater(this::refresh);
    }

    private void refresh() {
        if (playlistNameLabel == null || summaryLabel == null
                || trackListVBox == null || emptyLabel == null) {
            return; // FXML non ancora iniettato (es. uso solo applicativo del controller)
        }

        if (addTrackBtn != null) {
            boolean canAdd = currentPlaylist != null && !availableTracks.isEmpty();
            addTrackBtn.setDisable(!canAdd);
        }

        if (currentPlaylist == null) {
            playlistNameLabel.setText("—");
            summaryLabel.setText("");
            trackListVBox.getChildren().clear();
            showEmpty(true);
            return;
        }

        playlistNameLabel.setText(currentPlaylist.getName());

        List<Track> tracks = getPlaylistTracks(currentPlaylist);
        int totalSeconds = tracks.stream().mapToInt(Track::getDuration).sum();
        summaryLabel.setText(tracks.size() + " tracce · " + formatDuration(totalSeconds) + " totali");

        trackListVBox.getChildren().clear();
        selectedRow = null;

        if (isPlaylistEmpty(currentPlaylist)) {
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
        if (currentPlaylist == null || availableTracks.isEmpty()
                || addTrackBtn == null || addTrackBtn.getScene() == null) {
            return;
        }
        Window owner = addTrackBtn.getScene().getWindow();
        AddTrackDialogView dialog = new AddTrackDialogView();
        dialog.init(availableTracks, currentPlaylist, this, owner);
        dialog.show();
    }

    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void confirmAndRemoveTrack(Track track) {
        if (track == null || currentPlaylist == null) {
            return;
        }

        ButtonType cancelButton = new ButtonType("Annulla");
        ButtonType removeButton = new ButtonType("Rimuovi");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma rimozione");
        alert.setHeaderText("Rimuovere la traccia dalla playlist?");
        alert.setContentText(
                "Vuoi davvero rimuovere \"" + track.getTitle() + "\" dalla playlist \""
                        + currentPlaylist.getName() + "\"?"
        );

        alert.getButtonTypes().setAll(cancelButton, removeButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == removeButton) {
                removeTrackFromPlaylist(track, currentPlaylist);
            }
        });
    }

    @FXML
    private void handleBack() {
        if (onBackAction != null) {
            onBackAction.run();
        }
    }

    // US5 - elimina l'intera playlist visualizzata (previa conferma) e torna alla schermata
    // precedente; le tracce restano in libreria, viene rimossa solo la playlist.
    @FXML
    private void onDeletePlaylist() {
        if (currentPlaylist == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma eliminazione playlist");
        alert.setHeaderText("Eliminare la playlist \"" + currentPlaylist.getName() + "\"?");
        alert.setContentText("Le tracce restano nella libreria; viene rimossa solo la playlist.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                removePlaylist(currentPlaylist);
                if (onBackAction != null) {
                    onBackAction.run();
                }
            }
        });
    }
}
