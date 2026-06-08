package com.example.progetto_sad.support;

import com.example.progetto_sad.audio.AudioPlayer;

/**
 * Doppio di {@link AudioPlayer} per i test del {@link com.example.progetto_sad.model.Player}.
 *
 * Non usa JavaFX ne' file audio reali: registra le operazioni invocate e consente
 * di simulare la fine naturale del brano senza attendere il tempo reale.
 */
public class FakeAudioPlayer implements AudioPlayer {

    private String loadedPath;
    private Runnable onEndOfTrack;
    private boolean playing;

    @Override
    public void load(String filePath) {
        this.loadedPath = filePath;
        this.playing = false;
    }

    @Override
    public void play() {
        if (loadedPath == null) {
            throw new IllegalStateException("Nessun file audio caricato: invocare prima load()");
        }
        this.playing = true;
    }

    @Override
    public void setOnEndOfTrack(Runnable onEndOfTrack) {
        this.onEndOfTrack = onEndOfTrack;
    }

    /**
     * Simula il completamento naturale del brano, come farebbe il motore reale.
     */
    public void simulateEndOfTrack() {
        playing = false;
        if (onEndOfTrack != null) {
            onEndOfTrack.run();
        }
    }

    public String getLoadedPath() {
        return loadedPath;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void reset() {
        loadedPath = null;
        onEndOfTrack = null;
        playing = false;
    }
}
