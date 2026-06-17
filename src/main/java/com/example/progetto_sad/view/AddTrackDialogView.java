package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.PlayerBarController;
import com.example.progetto_sad.controller.PlaylistController;
import com.example.progetto_sad.controller.PlaylistSequenceController;
import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * View responsabile della finestra di dialogo per l'aggiunta di tracce
 * a una playlist. Mostra le tracce disponibili, permette di filtrare
 * l'elenco e aggiunge alla playlist solo le tracce selezionate.
 */
public class AddTrackDialogView {

    /**
     * Rappresenta una riga della finestra di dialogo associando una traccia
     * al relativo checkbox, al nodo grafico e allo stato di presenza nella playlist.
     *
     * @param track la traccia rappresentata dalla riga
     * @param cb il checkbox usato per selezionare la traccia
     * @param row il contenitore grafico della riga
     * @param alreadyPresent indica se la traccia è già presente nella playlist
     */
    private record RowEntry(Track track, CheckBox cb, HBox row, boolean alreadyPresent) {}

    private Stage stage;
    private Playlist playlist;
    private PlaylistController controller;
    private List<Track> allTracks;
    private Consumer<Track> addAction;
    private String customTitle;

    private Runnable onBack;
    private Runnable onSuccess;
    private PlayerBarController inlinePlayerBarController;

    private final List<Track> selected = new ArrayList<>();
    private final List<RowEntry> rowEntries = new ArrayList<>();

    private VBox trackListBox;
    private Label selectedCountLabel;
    private Button addButton;

    /**
     * Inizializza la finestra di dialogo con le tracce disponibili,
     * la playlist di destinazione, il controller e la finestra proprietaria.
     *
     * @param availableTracks le tracce che possono essere mostrate nella finestra
     * @param playlist la playlist a cui aggiungere le tracce selezionate
     * @param controller il controller usato per aggiornare la playlist
     * @param owner la finestra proprietaria della finestra di dialogo
     */
    public void init(List<Track> availableTracks, Playlist playlist,
                     PlaylistController controller, Window owner) {
        this.playlist = playlist;
        this.controller = controller;
        this.addAction = track -> controller.addTrackToPlaylist(track, playlist);
        this.customTitle = null;
        initDialog(availableTracks, owner);
    }

    /**
     * Inizializza il dialog per aggiungere le tracce selezionate tramite un'azione
     * personalizzata, senza dipendere da una playlist.
     *
     * @param availableTracks le tracce mostrate nella finestra
     * @param title il titolo visualizzato nel dialog
     * @param addAction l'azione eseguita per ogni traccia selezionata
     * @param owner la finestra proprietaria della finestra di dialogo
     */
    public void initForQueue(List<Track> availableTracks, String title,
                             Consumer<Track> addAction, Window owner) {
        this.playlist = null;
        this.controller = null;
        this.addAction = addAction;
        this.customTitle = title;
        initDialog(availableTracks, owner);
    }

    private void initDialog(List<Track> availableTracks, Window owner) {
        this.allTracks = availableTracks != null ? new ArrayList<>(availableTracks) : new ArrayList<>();
        selected.clear();
        rowEntries.clear();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setResizable(false);

        buildRowEntries();

        Scene scene = new Scene(buildRoot(), 580, 540);
        var cssUrl = getClass().getResource(
                "/com/example/progetto_sad/view/add-track-dialog.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setScene(scene);
    }

    /**
     * Mostra la finestra di dialogo in modalità bloccante fino alla sua chiusura.
     */
    public void show() {
        if (stage != null) {
            stage.showAndWait();
        }
    }

    /**
     * Costruisce la schermata inline "Aggiungi traccia" da usare come root della scena
     * principale (stessa tecnica di QueueView), senza aprire un nuovo Stage.
     *
     * @param availableTracks tracce selezionabili dalla libreria
     * @param title           titolo mostrato nell'header inline
     * @param addAction       azione eseguita per ogni traccia selezionata (es. seqController::addToQueue)
     * @param player          player condiviso, per bindare la PlayerBar
     * @param seqController   sequenza condivisa, per bindare la PlayerBar
     * @param onBack          callback: ← Indietro e Annulla tornano alla schermata precedente
     * @param onSuccess       callback: dopo conferma torna alla schermata precedente
     * @return il nodo radice da passare a {@code scene.setRoot()}
     */
    public Parent buildInlineView(List<Track> availableTracks, String title,
                                   Consumer<Track> addAction,
                                   Player player,
                                   PlaylistSequenceController seqController,
                                   Runnable onBack, Runnable onSuccess) {
        this.playlist   = null;
        this.controller = null;
        this.addAction  = addAction;
        this.allTracks  = availableTracks != null ? new ArrayList<>(availableTracks) : new ArrayList<>();
        selected.clear();
        rowEntries.clear();
        buildRowEntries();

        Runnable cleanup = () -> {
            if (inlinePlayerBarController != null) {
                inlinePlayerBarController.dispose();
                inlinePlayerBarController = null;
            }
        };
        this.onBack    = () -> { cleanup.run(); onBack.run(); };
        this.onSuccess = () -> { cleanup.run(); onSuccess.run(); };

        VBox card = new VBox(16);
        card.getStyleClass().add("dialog-card");
        VBox.setVgrow(card, Priority.ALWAYS);
        card.getChildren().addAll(
                buildInlineHeader(title),
                buildFilterBar(),
                buildTrackScroll(),
                buildFooter()
        );

        VBox formArea = new VBox(card);
        formArea.getStyleClass().add("dialog-root");
        VBox.setVgrow(formArea, Priority.ALWAYS);

        VBox root = new VBox();
        root.setStyle("-fx-background-color: #141417;");

        HBox playerBarNode = loadPlayerBar(player, seqController);
        root.getChildren().add(formArea);
        if (playerBarNode != null) {
            root.getChildren().add(playerBarNode);
        }

        var cssUrl = getClass().getResource("/com/example/progetto_sad/view/add-track-dialog.css");
        if (cssUrl != null) {
            root.getStylesheets().add(cssUrl.toExternalForm());
        }
        return root;
    }

    private HBox buildInlineHeader(String title) {
        Button backBtn = new Button("← Indietro");
        backBtn.getStyleClass().add("cancel-btn");
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title");

        HBox header = new HBox(12, backBtn, titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox loadPlayerBar(Player player, PlaylistSequenceController seqController) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/progetto_sad/view/PlayerBar.fxml"));
            HBox node = loader.load();
            inlinePlayerBarController = loader.getController();
            inlinePlayerBarController.bind(player, seqController, () -> null);
            return node;
        } catch (IOException e) {
            return null;
        }
    }

    // ── costruzione righe ────────────────────────────────────────────────────

    private void buildRowEntries() {
        List<Track> inPlaylist = controller != null && playlist != null
                ? controller.getPlaylistTracks(playlist)
                : List.of();
        for (Track track : allTracks) {
            boolean alreadyPresent = inPlaylist.contains(track);
            CheckBox cb = new CheckBox();
            cb.setDisable(alreadyPresent);                   // disabilitata se già presente
            if (!alreadyPresent) {
                cb.selectedProperty().addListener((obs, oldV, newV) -> {
                    if (newV) {
                        selected.add(track);
                    } else {
                        selected.remove(track);
                    }
                    updateFooter();
                });
            }
            rowEntries.add(new RowEntry(track, cb, buildRow(track, cb, alreadyPresent), alreadyPresent));
        }
    }

    private HBox buildRow(Track track, CheckBox cb, boolean alreadyPresent) {
        Label titleLabel = new Label(track.getTitle());
        titleLabel.getStyleClass().add("row-title");

        Label metaLabel = new Label("· " + track.getAuthor());
        metaLabel.getStyleClass().add("row-meta");

        HBox info = new HBox(6, titleLabel, metaLabel);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label durationLabel = new Label(formatDuration(track.getDuration()));
        durationLabel.getStyleClass().add("row-duration");

        HBox row = new HBox(12, cb, info, durationLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add(alreadyPresent ? "track-row-disabled" : "track-row");

        if (alreadyPresent) {
            Label badge = new Label("già presente");
            badge.getStyleClass().add("already-badge");
            row.getChildren().add(badge);             // badge solo se già presente
        }

        return row;
    }

    // ── layout ───────────────────────────────────────────────────────────────

    private VBox buildRoot() {
        VBox card = new VBox(16);
        card.getStyleClass().add("dialog-card");
        VBox.setVgrow(card, Priority.ALWAYS);
        card.getChildren().addAll(
                buildHeader(),
                buildFilterBar(),
                buildTrackScroll(),
                buildFooter()
        );

        VBox root = new VBox(card);
        root.getStyleClass().add("dialog-root");
        return root;
    }

    private HBox buildHeader() {
        HBox titleBox;
        if (customTitle != null) {
            Label titleLabel = new Label(customTitle);
            titleLabel.getStyleClass().add("dialog-title");
            titleBox = new HBox(titleLabel);
        } else {
            Label prefix = new Label("Aggiungi traccia a ");
            prefix.getStyleClass().add("dialog-title");

            Label nameLabel = new Label(playlist.getName());
            nameLabel.getStyleClass().addAll("dialog-title", "playlist-name-highlight");
            titleBox = new HBox(0, prefix, nameLabel);
        }
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("dialog-close-btn");
        closeBtn.setOnAction(e -> stage.close());     // X → chiude senza modifiche

        HBox header = new HBox(titleBox, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox buildFilterBar() {
        Label searchIcon = new Label("🔍");
        searchIcon.getStyleClass().add("search-icon");

        TextField filterField = new TextField();
        filterField.setPromptText("Filtra tracce...");
        filterField.getStyleClass().add("filter-field");
        // US5 CA4 - limite caratteri sul campo filtro
        filterField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= 20 ? change : null));
        HBox.setHgrow(filterField, Priority.ALWAYS);
        filterField.textProperty().addListener(
                (obs, old, val) -> applyFilter(val.toLowerCase()));

        HBox bar = new HBox(8, searchIcon, filterField);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("filter-bar");
        return bar;
    }

    private ScrollPane buildTrackScroll() {
        trackListBox = new VBox(0);
        for (RowEntry entry : rowEntries) {
            trackListBox.getChildren().add(entry.row());
        }

        ScrollPane scroll = new ScrollPane(trackListBox);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("dialog-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private HBox buildFooter() {
        selectedCountLabel = new Label("0 selezionate");
        selectedCountLabel.getStyleClass().add("selected-count");
        HBox.setHgrow(selectedCountLabel, Priority.ALWAYS);

        Button cancelBtn = new Button("Annulla");
        cancelBtn.getStyleClass().add("cancel-btn");
        cancelBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
            else stage.close();
        });

        addButton = new Button("Aggiungi");
        addButton.getStyleClass().add("add-btn");
        addButton.setDisable(true);                   // disabilitato finché selezione = 0
        addButton.setOnAction(e -> addSelected());

        HBox footer = new HBox(12, selectedCountLabel, cancelBtn, addButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("dialog-footer");
        return footer;
    }

    // ── logica ───────────────────────────────────────────────────────────────

    private void applyFilter(String filter) {
        trackListBox.getChildren().clear();
        for (RowEntry entry : rowEntries) {
            if (filter.isBlank()
                    || entry.track().getTitle().toLowerCase().contains(filter)   // filtra su titolo
                    || entry.track().getAuthor().toLowerCase().contains(filter)) // filtra su autore
            {
                trackListBox.getChildren().add(entry.row());
            }
        }
    }

    private void updateFooter() {
        int count = selected.size();
        selectedCountLabel.setText(count + " selezionate");
        addButton.setDisable(count == 0);             // Aggiungi abilitato solo se count > 0
    }

    private void addSelected() {
        for (Track track : new ArrayList<>(selected)) {
            if (addAction != null) {
                addAction.accept(track);
            }
        }
        if (onSuccess != null) {
            onSuccess.run();
        } else if (stage != null) {
            stage.close();
        }
    }

    private String formatDuration(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
