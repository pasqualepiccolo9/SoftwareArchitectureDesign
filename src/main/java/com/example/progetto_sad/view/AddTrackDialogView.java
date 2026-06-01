package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.PlaylistController;
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

public class AddTrackDialogView {

    private record RowEntry(Track track, CheckBox cb, HBox row, boolean alreadyPresent) {}

    private Stage stage;
    private Playlist playlist;
    private PlaylistController controller;
    private List<Track> allTracks;

    private final List<Track> selected = new ArrayList<>();
    private final List<RowEntry> rowEntries = new ArrayList<>();

    private VBox trackListBox;
    private Label selectedCountLabel;
    private Button addButton;

    public void init(List<Track> availableTracks, Playlist playlist,
                     PlaylistController controller, Window owner) {
        this.playlist = playlist;
        this.controller = controller;
        this.allTracks = new ArrayList<>(availableTracks);

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

    public void show() {
        stage.showAndWait();
    }

    // ── costruzione righe ────────────────────────────────────────────────────

    private void buildRowEntries() {
        List<Track> inPlaylist = controller.getPlaylistTracks(playlist);
        for (Track t : allTracks) {
            boolean already = inPlaylist.contains(t);
            CheckBox cb = new CheckBox();
            cb.setDisable(already);                   // disabilitata se già presente
            if (!already) {
                cb.selectedProperty().addListener((obs, oldV, newV) -> {
                    if (newV) selected.add(t);
                    else      selected.remove(t);
                    updateFooter();
                });
            }
            rowEntries.add(new RowEntry(t, cb, buildRow(t, cb, already), already));
        }
    }

    private HBox buildRow(Track t, CheckBox cb, boolean already) {
        Label titleLabel = new Label(t.getTitle());
        titleLabel.getStyleClass().add("row-title");

        Label metaLabel = new Label("· " + t.getAuthor());
        metaLabel.getStyleClass().add("row-meta");

        HBox info = new HBox(6, titleLabel, metaLabel);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label durationLabel = new Label(formatDuration(t.getDuration()));
        durationLabel.getStyleClass().add("row-duration");

        HBox row = new HBox(12, cb, info, durationLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add(already ? "track-row-disabled" : "track-row");

        if (already) {
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
        Label prefix = new Label("Aggiungi traccia a ");
        prefix.getStyleClass().add("dialog-title");

        Label nameLabel = new Label(playlist.getName());
        nameLabel.getStyleClass().addAll("dialog-title", "playlist-name-highlight");

        HBox titleBox = new HBox(0, prefix, nameLabel);
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
        for (RowEntry e : rowEntries) {
            trackListBox.getChildren().add(e.row());
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
        cancelBtn.setOnAction(e -> stage.close());    // Annulla → chiude senza modifiche

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
        for (RowEntry e : rowEntries) {
            if (filter.isBlank()
                    || e.track().getTitle().toLowerCase().contains(filter)   // filtra su titolo
                    || e.track().getAuthor().toLowerCase().contains(filter)) // filtra su autore
            {
                trackListBox.getChildren().add(e.row());
            }
        }
    }

    private void updateFooter() {
        int count = selected.size();
        selectedCountLabel.setText(count + " selezionate");
        addButton.setDisable(count == 0);             // Aggiungi abilitato solo se count > 0
    }

    private void addSelected() {
        for (Track t : new ArrayList<>(selected)) {
            controller.addTrackToPlaylist(t, playlist); // unico punto di aggiunta
        }
        stage.close();
    }

    private String formatDuration(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
