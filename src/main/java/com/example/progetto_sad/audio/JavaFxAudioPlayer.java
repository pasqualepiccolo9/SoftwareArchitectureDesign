package com.example.progetto_sad.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

/**
 * US9 - Unica implementazione reale di {@link AudioPlayer} (pattern Adapter).
 *
 * Avvolge {@link MediaPlayer}/{@link Media} di JavaFX traducendo le operazioni
 * di dominio nelle chiamate della libreria. E' l'unico punto del codice che
 * conosce JavaFX media, cosi' il resto dell'applicazione resta disaccoppiato.
 */
public class JavaFxAudioPlayer implements AudioPlayer {

    // US9 - player JavaFX corrente; ricreato a ogni caricamento di un nuovo file
    private MediaPlayer mediaPlayer;

    // US9 - callback di fine traccia propagata dal MediaPlayer verso il dominio
    private Runnable onEndOfTrack;

    /**
     * US9 - Crea un {@link Media} dal file indicato e prepara un nuovo
     * {@link MediaPlayer}. L'eventuale player precedente viene rilasciato per
     * non lasciare risorse audio aperte.
     *
     * @param filePath percorso del file audio da riprodurre
     * @throws IllegalArgumentException se il percorso e' nullo o vuoto
     */
    @Override
    public void load(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Il percorso del file audio non puo' essere vuoto");
        }
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        Media media = new Media(new File(filePath).toURI().toString());
        this.mediaPlayer = new MediaPlayer(media);
        this.mediaPlayer.setOnEndOfMedia(this::notifyEndOfTrack);
    }

    /**
     * US9 - Avvia la riproduzione del file caricato.
     *
     * @throws IllegalStateException se non e' stato caricato alcun file
     */
    @Override
    public void play() {
        if (mediaPlayer == null) {
            throw new IllegalStateException("Nessun file audio caricato: invocare prima load()");
        }
        mediaPlayer.play();
    }

    /**
     * US9 - Registra il callback da invocare quando JavaFX segnala la fine naturale del brano.
     *
     * @param onEndOfTrack callback di fine traccia; se null, non viene eseguita alcuna azione
     */
    @Override
    public void setOnEndOfTrack(Runnable onEndOfTrack) {
        this.onEndOfTrack = onEndOfTrack;
    }

    private void notifyEndOfTrack() {
        if (onEndOfTrack != null) {
            onEndOfTrack.run();
        }
    }
}
