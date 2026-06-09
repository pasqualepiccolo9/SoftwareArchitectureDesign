package com.example.progetto_sad.view;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * View responsabile della finestra di dialogo per l'aggiunta di una playlist
 * alla coda di riproduzione. Segue il pattern strutturale di {@link AddTrackDialogView}:
 * Java-only, usa {@code add-track-dialog.css}, stessa struttura
 * header/filtro/scroll/footer e stesse classi CSS sulle righe.
 *
 * <p>Usa {@link CheckBox} per la selezione, come {@link AddTrackDialogView}, ma
 * impone selezione singola: selezionare una playlist deseleziona automaticamente
 * le altre. Alla conferma viene chiamata la callback una sola volta.</p>
 *
 * <p>Indipendente dallo stato globale: riceve dall'esterno la lista di playlist
 * disponibili e la callback da invocare sulla playlist selezionata.</p>
 */
public class AddPlaylistDialogView {

    private record RowEntry(Playlist playlist, CheckBox cb, HBox row) {}

    private Stage stage;
    private List<Playlist> allPlaylists;
    private Consumer<Playlist> addAction;

    private final List<RowEntry> rowEntries = new ArrayList<>();
    private Playlist currentSelection = null;
    private boolean updatingSelection = false;

    private VBox playlistListBox;
    private Label selectedCountLabel;
    private Button addButton;

    /**
     * Inizializza il dialog con la lista di playlist disponibili e l'azione
     * da eseguire sulla playlist selezionata.
     *
     * @param availablePlaylists le playlist da mostrare nel dialog
     * @param addAction          azione chiamata con la playlist selezionata
     * @param owner              finestra proprietaria (per modalità APPLICATION_MODAL)
     */
    public void initForQueue(List<Playlist> availablePlaylists,
                             Consumer<Playlist> addAction,
                             Window owner) {
        this.allPlaylists = availablePlaylists != null
                ? new ArrayList<>(availablePlaylists) : new ArrayList<>();
        this.addAction = addAction;
        currentSelection = null;
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

    // ── costruzione righe ────────────────────────────────────────────────────

    private void buildRowEntries() {
        for (Playlist playlist : allPlaylists) {
            CheckBox cb = new CheckBox();
            cb.selectedProperty().addListener((obs, oldV, newV) -> {
                if (newV && !updatingSelection) {
                    // selezione singola: deseleziona tutte le altre
                    updatingSelection = true;
                    for (RowEntry other : rowEntries) {
                        if (other.cb() != cb) {
                            other.cb().setSelected(false);
                        }
                    }
                    currentSelection = playlist;
                    updatingSelection = false;
                } else if (!newV && !updatingSelection) {
                    if (currentSelection == playlist) {
                        currentSelection = null;
                    }
                }
                updateFooter();
            });
            rowEntries.add(new RowEntry(playlist, cb, buildRow(playlist, cb)));
        }
    }

    private HBox buildRow(Playlist playlist, CheckBox cb) {
        Label nameLabel = new Label(playlist.getName());
        nameLabel.getStyleClass().add("row-title");

        int count = playlist.getTracks().size();
        Label metaLabel = new Label("· " + count + (count == 1 ? " brano" : " brani"));
        metaLabel.getStyleClass().add("row-meta");

        HBox info = new HBox(6, nameLabel, metaLabel);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label durationLabel = new Label(formatTotalDuration(playlist.getTracks()));
        durationLabel.getStyleClass().add("row-duration");

        HBox row = new HBox(12, cb, info, durationLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add("track-row");
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
                buildPlaylistScroll(),
                buildFooter()
        );

        VBox root = new VBox(card);
        root.getStyleClass().add("dialog-root");
        return root;
    }

    private HBox buildHeader() {
        Label titleLabel = new Label("Aggiungi la playlist alla coda di riproduzione");
        titleLabel.getStyleClass().add("dialog-title");
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("dialog-close-btn");
        closeBtn.setOnAction(e -> stage.close());

        HBox header = new HBox(titleBox, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox buildFilterBar() {
        Label searchIcon = new Label("🔍");
        searchIcon.getStyleClass().add("search-icon");

        TextField filterField = new TextField();
        filterField.setPromptText("Filtra playlist...");
        filterField.getStyleClass().add("filter-field");
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

    private ScrollPane buildPlaylistScroll() {
        playlistListBox = new VBox(0);
        for (RowEntry entry : rowEntries) {
            playlistListBox.getChildren().add(entry.row());
        }

        ScrollPane scroll = new ScrollPane(playlistListBox);
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
        cancelBtn.setOnAction(e -> stage.close());

        addButton = new Button("Aggiungi");
        addButton.getStyleClass().add("add-btn");
        addButton.setDisable(true);
        addButton.setOnAction(e -> {
            if (currentSelection != null && addAction != null) {
                addAction.accept(currentSelection);
            }
            stage.close();
        });

        HBox footer = new HBox(12, selectedCountLabel, cancelBtn, addButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("dialog-footer");
        return footer;
    }

    // ── logica ───────────────────────────────────────────────────────────────

    private void applyFilter(String filter) {
        playlistListBox.getChildren().clear();
        for (RowEntry entry : rowEntries) {
            if (filter.isBlank()
                    || entry.playlist().getName().toLowerCase().contains(filter)) {
                playlistListBox.getChildren().add(entry.row());
            }
        }
    }

    private void updateFooter() {
        if (currentSelection != null) {
            selectedCountLabel.setText("1 selezionata");
            addButton.setDisable(false);
        } else {
            selectedCountLabel.setText("0 selezionate");
            addButton.setDisable(true);
        }
    }

    private String formatTotalDuration(List<Track> tracks) {
        int total = tracks.stream().mapToInt(Track::getDuration).sum();
        return String.format("%d:%02d", total / 60, total % 60);
    }
}
