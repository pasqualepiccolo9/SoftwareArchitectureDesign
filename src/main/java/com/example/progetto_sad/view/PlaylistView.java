package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.PlaylistController;
import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.observer.Observer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class PlaylistView implements Observer {

    @FXML private Label playlistNameLabel;
    @FXML private Label summaryLabel;
    @FXML private VBox trackListVBox;
    @FXML private Label emptyLabel;

    private Playlist playlist;
    private PlaylistController controller;
    private HBox selectedRow;

    public void init(Playlist playlist, PlaylistController controller) {
        this.playlist = playlist;
        this.controller = controller;
        if (playlist != null) {
            playlist.attach(this);
        }
        refresh();
    }

    @Override
    public void update() {
        Platform.runLater(this::refresh);
    }

    public void display() {
        refresh();
    }

    private void refresh() {
        if (controller == null || playlistNameLabel == null) return;

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
        removeBtn.setOnAction(e -> controller.removeTrackFromPlaylist(t, playlist));
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

    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
