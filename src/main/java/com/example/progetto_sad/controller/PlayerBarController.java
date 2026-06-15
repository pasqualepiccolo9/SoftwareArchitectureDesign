package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.util.PlaybackTimeFormatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Controller riusabile della Player Bar.
 *
 * La barra puo' essere inclusa in piu' schermate, ma lavora sempre sullo stesso
 * {@link Player} condiviso e sullo stesso {@link PlaylistSequenceController}.
 */
public class PlayerBarController implements Observer {

    private Player player;
    private PlaylistSequenceController seqController;
    private Supplier<Track> primaryTrackSupplier;
    private Supplier<Track> fallbackTrackSupplier;
    private final AtomicBoolean refreshScheduled = new AtomicBoolean(false);
    private boolean updatingProgressSlider;
    private boolean userSeekingProgress;

    @FXML private Label playerTitleLabel;
    @FXML private Label playerMetaLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label durationLabel;
    @FXML private Slider playerProgressSlider;
    @FXML private Button sequentialModeButton;
    @FXML private Button shuffleModeButton;
    @FXML private Button loopModeButton;
    @FXML private Button playButton;
    @FXML private Button stopButton;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button skipPlaylistButton;

    @FXML
    private void initialize() {
        configureProgressSlider();
        resetProgressSlider();
        refreshPlayerBar();
    }

    /**
     * Collega la barra agli oggetti condivisi dell'applicazione.
     *
     * @param player                 player unico dell'applicazione
     * @param seqController          controller della coda/sequenza condivisa
     * @param primaryTrackSupplier traccia scelta esplicitamente dalla schermata corrente
     */
    public void bind(Player player, PlaylistSequenceController seqController,
                     Supplier<Track> primaryTrackSupplier) {
        bind(player, seqController, primaryTrackSupplier, null);
    }

    /**
     * Collega la barra agli oggetti condivisi distinguendo la traccia scelta
     * esplicitamente da quella pronta nella coda quando il player e' fermo.
     *
     * @param player               player unico dell'applicazione
     * @param seqController        controller della coda/sequenza condivisa
     * @param primaryTrackSupplier traccia scelta esplicitamente
     * @param fallbackTrackSupplier traccia pronta, usata solo se non c'e' una scelta primaria
     */
    public void bind(Player player, PlaylistSequenceController seqController,
                     Supplier<Track> primaryTrackSupplier,
                     Supplier<Track> fallbackTrackSupplier) {
        if (this.player != null) {
            this.player.detach(this);
        }
        if (this.seqController != null) {
            this.seqController.detach(this);
        }

        this.player = player;
        this.seqController = seqController;
        this.primaryTrackSupplier = primaryTrackSupplier;
        this.fallbackTrackSupplier = fallbackTrackSupplier;

        if (this.player != null) {
            this.player.attach(this);
        }
        if (this.seqController != null) {
            this.seqController.attach(this);
        }
        requestRefresh();
    }

    /**
     * Scollega la barra dagli observer quando la schermata non e' piu' attiva.
     */
    public void dispose() {
        if (player != null) {
            player.detach(this);
        }
        if (seqController != null) {
            seqController.detach(this);
        }
        player = null;
        seqController = null;
        primaryTrackSupplier = null;
        fallbackTrackSupplier = null;
    }

    @Override
    public void update() {
        requestRefresh();
    }

    public void requestRefresh() {
        if (!refreshScheduled.compareAndSet(false, true)) {
            return;
        }
        Platform.runLater(() -> {
            refreshScheduled.set(false);
            refreshPlayerBar();
        });
    }

    private void refreshPlayerBar() {
        if (player == null) {
            resetPlayerBar();
            return;
        }

        Track currentTrack = player.getCurrentTrack();
        Track primaryTrack = getPrimaryTrack();
        Track displayedTrack = currentTrack != null ? currentTrack
                : (primaryTrack != null ? primaryTrack : getFallbackTrack());
        Player.PlayerState state = player.getState();
        boolean isPlaying = state == Player.PlayerState.IN_RIPRODUZIONE;

        if (displayedTrack == null) {
            resetPlayerBar();
            return;
        } else if (isPlaying && currentTrack != null) {
            setPlayerText(currentTrack.getTitle(), currentTrack.getAuthor() + " • In riproduzione");
        } else if (!hasPlayableAudio(displayedTrack)) {
            setPlayerText(displayedTrack.getTitle(), displayedTrack.getAuthor() + " • File audio non disponibile");
        } else if (state == Player.PlayerState.IN_PAUSA && currentTrack != null) {
            setPlayerText(currentTrack.getTitle(), currentTrack.getAuthor() + " • In pausa");
        } else if (currentTrack != null) {
            setPlayerText(currentTrack.getTitle(), currentTrack.getAuthor() + " • Fermata");
        } else {
            setPlayerText(displayedTrack.getTitle(), displayedTrack.getAuthor() + " • Pronta");
        }

        int currentTime = player.getCurrentTime();
        int duration = currentTrack != null ? player.getDuration()
                : Math.max(0, displayedTrack.getDuration());
        boolean canSeek = currentTrack != null && duration > 0
                && (state == Player.PlayerState.IN_RIPRODUZIONE || state == Player.PlayerState.IN_PAUSA);
        updateProgress(currentTime, duration, canSeek);
        updatePlayerButtons(state, hasPlayableAudio(displayedTrack));
    }

    private Track getPrimaryTrack() {
        return primaryTrackSupplier != null ? primaryTrackSupplier.get() : null;
    }

    private Track getFallbackTrack() {
        return fallbackTrackSupplier != null ? fallbackTrackSupplier.get() : null;
    }

    private void resetPlayerBar() {
        setPlayerText("—", "Seleziona una traccia");
        updateProgress(0, 0, false);
        updatePlayerButtons(Player.PlayerState.FERMO, false);
    }

    private void setPlayerText(String title, String meta) {
        if (playerTitleLabel != null) playerTitleLabel.setText(title);
        if (playerMetaLabel != null) playerMetaLabel.setText(meta);
    }

    private void updateProgress(int currentTime, int duration, boolean canSeek) {
        int safeCurrentTime = Math.max(0, currentTime);
        int safeDuration = Math.max(0, duration);
        if (currentTimeLabel != null) {
            currentTimeLabel.setText(PlaybackTimeFormatter.formatSeconds(safeCurrentTime));
        }
        if (durationLabel != null) {
            durationLabel.setText(PlaybackTimeFormatter.formatSeconds(safeDuration));
        }
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
                currentTimeLabel.setText(PlaybackTimeFormatter.formatSeconds(newValue.intValue()));
            }
        });
    }

    private void seekToSliderValue() {
        if (player == null || playerProgressSlider == null || playerProgressSlider.isDisabled()) {
            return;
        }
        player.seekTo((int) Math.round(playerProgressSlider.getValue()));
        requestRefresh();
    }

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
        updatePlayModeButton(sequentialModeButton, seqController != null && seqController.isSequentialMode());
        updatePlayModeButton(shuffleModeButton, seqController != null && seqController.isShuffleMode());
        updatePlayModeButton(loopModeButton, seqController != null && seqController.isLoopMode());
    }

    private void updatePlayModeButton(Button button, boolean active) {
        if (button == null) {
            return;
        }
        button.setDisable(false);
        button.getStyleClass().remove("play-mode-btn-active");
        if (active) {
            button.getStyleClass().add("play-mode-btn-active");
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

    @FXML
    private void handlePlayPauseAction() {
        if (player == null) return;

        Track primaryTrack = getPrimaryTrack();
        if (primaryTrack != null && primaryTrack != player.getCurrentTrack()) {
            player.play(primaryTrack);
        } else if (player.getState() == Player.PlayerState.IN_RIPRODUZIONE) {
            player.pause();
        } else if (player.getState() == Player.PlayerState.IN_PAUSA) {
            player.resume();
        } else if (player.getState() == Player.PlayerState.FERMO) {
            Track fallbackTrack = getFallbackTrack();
            Track trackToPlay = primaryTrack != null ? primaryTrack
                    : (fallbackTrack != null ? fallbackTrack
                    : (seqController != null ? seqController.getCurrentTrack() : null));
            if (trackToPlay != null) {
                player.play(trackToPlay);
            }
        }
        requestRefresh();
    }

    @FXML
    private void onNextTrack() {
        if (seqController == null || player == null) return;
        Track next = seqController.goToNextTrack();
        if (next != null) {
            player.play(next);
        }
        requestRefresh();
    }

    @FXML
    private void onPreviousTrack() {
        if (seqController == null || player == null) return;
        Track previous = seqController.goToPreviousTrack();
        if (previous != null) {
            player.play(previous);
        }
        requestRefresh();
    }

    @FXML
    private void onSkipPlaylist() {
        if (seqController == null || player == null) return;
        Track next = seqController.skipPlaylist();
        if (next != null) {
            player.play(next);
        }
        requestRefresh();
    }

    @FXML
    private void onSequentialMode() {
        if (seqController == null) return;
        seqController.setSequentialMode();
        requestRefresh();
    }

    @FXML
    private void onShuffleMode() {
        if (seqController == null) return;
        seqController.setShuffleMode();
        requestRefresh();
    }

    @FXML
    private void onLoopMode() {
        if (seqController == null) return;
        seqController.setLoopMode();
        requestRefresh();
    }

    @FXML
    private void onStopPlayer() {
        if (player == null) return;
        player.stop();
        requestRefresh();
    }
}
