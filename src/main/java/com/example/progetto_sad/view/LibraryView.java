package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.LibraryController;
import com.example.progetto_sad.controller.PlaylistSequenceController;
import com.example.progetto_sad.controller.TrackController;
import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * US4 - View della schermata principale (home / "Libreria tracce").
 *
 * Carica {@code LibraryView.fxml} e vi collega il {@link LibraryController}
 * (controller FXML). Espone {@link #load} per ottenere il nodo radice da inserire
 * in una scena e un avvio standalone ({@link App}) con dati d'esempio, sullo stile
 * di {@code AddTrackPreview} / {@code PlaylistView}.
 */
public class LibraryView {

    /**
     * Carica la home e restituisce il nodo radice pronto da inserire in una scena,
     * iniettando il {@link LibraryController} costruito sulle dipendenze fornite.
     *
     * @param library         libreria delle tracce
     * @param trackController controller applicativo per creare/eliminare tracce
     * @param playlistManager gestore delle playlist (sidebar e navigazione)
     * @param seqController   controller della sequenza di riproduzione condivisa (US14)
     * @param player          il player audio condiviso per i comandi di riproduzione
     * @return il nodo radice della home
     * @throws IllegalStateException se l'FXML non puo' essere caricato
     */
    public static Parent load(TrackLibrary library, TrackController trackController,
                              PlaylistManager playlistManager,
                              PlaylistSequenceController seqController,Player player) {
        try {
            FXMLLoader loader = new FXMLLoader(LibraryView.class.getResource("LibraryView.fxml"));
            loader.setControllerFactory(type ->
                    new LibraryController(library, trackController, playlistManager, seqController, player));
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare LibraryView.fxml", e);
        }
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    /**
     * Avvio standalone della home con dati d'esempio.
     *
     * La classe esterna NON estende Application (avvio affidabile da module-path e
     * classpath); l'app JavaFX e' questa classe annidata.
     */
    public static class App extends Application {

        @Override
        public void start(Stage stage) {
            TrackLibrary library = new TrackLibrary();
            TrackController trackController = new TrackController(library);
            PlaylistManager playlistManager = new PlaylistManager();
            // istanza standalone per la demo: nel task INT verra' sostituita
            // dall'istanza condivisa creata nel flusso principale dell'applicazione.
            PlaylistSequenceController seqController = new PlaylistSequenceController();
            
            com.example.progetto_sad.audio.AudioPlayer audio = new com.example.progetto_sad.audio.JavaFxAudioPlayer();
            Player sharedPlayer = new Player(audio);

            
            sharedPlayer.setOnEndOfTrack(() -> {
                seqController.onTrackFinished();
                Track next = seqController.getCurrentTrack();
                if (next != null) {
                    sharedPlayer.play(next);
                }
            });
            seedSampleData(library, playlistManager);

            Parent root = LibraryView.load(library, trackController, playlistManager, seqController, sharedPlayer);
            
            Scene mainScene = new Scene(root, 1150, 760);
            
            
            
            Runnable navigazioneVersoCoda = () -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("QueueView.fxml"));
                    Parent queueRoot = loader.load();
                    
                    com.example.progetto_sad.controller.QueueController queueCtrl = loader.getController();
                    // Iniettiamo la stessa istanza della coda condivisa
                    queueCtrl.setSequenceController(seqController);
                    queueCtrl.setTrackLibrary(library);
                    
                    // Definiamo l'azione per tornare indietro alla libreria
                    queueCtrl.setOnBackAction(() -> mainScene.setRoot(root));
                    
                    // Sostituiamo il contenuto della finestra senza distruggere la memoria
                    mainScene.setRoot(queueRoot);
                    queueCtrl.refresh();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            };
            
            stage.setTitle("Playlist Manager - Libreria");
            stage.setScene(mainScene);
            mainScene.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                    navigazioneVersoCoda.run();
                }
            });
            stage.show();
        }
                    

        // Dati d'esempio coerenti col mockup (tracce in libreria + alcune playlist).
        private void seedSampleData(TrackLibrary library, PlaylistManager playlistManager) {
            library.addTrack(new Track("Midnight Rain", "Taylor Swift", "Pop", 2022, null, 221));
            library.addTrack(new Track("Blinding Lights", "The Weeknd", "Synth-pop", 2019, null, 200));
            library.addTrack(new Track("Levitating", "Dua Lipa", "Pop", 2020, null, 203));
            library.addTrack(new Track("Stay", "The Kid LAROI", "Hip-hop", 2021, null, 141));
            library.addTrack(new Track("As It Was", "Harry Styles", "Pop", 2022, null, 157));
            library.addTrack(new Track("Heat Waves", "Glass Animals", "Indie", 2020, null, 239));

            playlistManager.createPlaylist("Chill Vibes");
            playlistManager.createPlaylist("Workout Mix");
            playlistManager.createPlaylist("Study Session");
            playlistManager.createPlaylist("Evening Jazz");
        }
    }
}
