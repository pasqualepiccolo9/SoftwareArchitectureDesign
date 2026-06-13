package com.example.progetto_sad.view;

import com.example.progetto_sad.command.CommandManager;
import com.example.progetto_sad.controller.LibraryController;
import com.example.progetto_sad.controller.PlaylistSequenceController;
import com.example.progetto_sad.controller.TrackController;
import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Player;
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
     * @param player          player di dominio collegato alla Player Bar (US9)
     * @return il nodo radice della home
     * @throws IllegalStateException se l'FXML non puo' essere caricato
     */
    public static Parent load(TrackLibrary library, TrackController trackController,
                              PlaylistManager playlistManager,
                              PlaylistSequenceController seqController,
                              Player player) {
        return load(library, trackController, playlistManager, seqController, player,
                new CommandManager());
    }

    /**
     * US22 - Variante con lo storico comandi condiviso fornito dal composition root:
     * lo STESSO {@link CommandManager} va passato anche ai controller che eseguono
     * operazioni annullabili (es. {@code new TrackController(library, manager)}),
     * cosi' il pulsante "Annulla" della home agisce su un'unica cronologia.
     */
    public static Parent load(TrackLibrary library, TrackController trackController,
                              PlaylistManager playlistManager,
                              PlaylistSequenceController seqController,
                              Player player, CommandManager commandManager) {
        try {
            FXMLLoader loader = new FXMLLoader(LibraryView.class.getResource("LibraryView.fxml"));
            loader.setControllerFactory(type ->
                    new LibraryController(library, trackController, playlistManager, seqController,
                            player, commandManager));
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare LibraryView.fxml", e);
        }
    }

    /**
     * Carica la home creando il player reale JavaFX per compatibilita' con i
     * chiamanti esistenti.
     */
    public static Parent load(TrackLibrary library, TrackController trackController,
                              PlaylistManager playlistManager,
                              PlaylistSequenceController seqController) {
        return load(library, trackController, playlistManager, seqController,
                Player.getInstance());
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    /**
     * Avvio standalone della home con dati d'esempio.
     */
    public static class App extends Application {

        @Override
        public void start(Stage stage) {
            TrackLibrary library = new TrackLibrary();
            // US22 - composition root: un UNICO storico comandi per tutta l'app,
            // condiviso tra il pulsante "Annulla" della home e tutti i controller
            // che eseguono operazioni annullabili (tracce e playlist).
            CommandManager commandManager = new CommandManager();
            TrackController trackController = new TrackController(library, commandManager);
            PlaylistManager playlistManager = new PlaylistManager();
            PlaylistSequenceController seqController = new PlaylistSequenceController();


            Player player = Player.getInstance();

            
            player.setOnEndOfTrack(() -> {
                seqController.onTrackFinished();
                Track next = seqController.getCurrentTrack();
                if (next != null) {
                    player.play(next);
                }
            });
            
            seedSampleData(library, playlistManager);

           
            Parent root = LibraryView.load(library, trackController, playlistManager, seqController,
                    player, commandManager);
            
            
            Scene mainScene = new Scene(root, 1150, 760);

            
            Runnable navigazioneVersoCoda = () -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("QueueView.fxml"));
                    Parent queueRoot = loader.load();
                    
                    com.example.progetto_sad.controller.QueueController queueCtrl = loader.getController();
                    queueCtrl.setSequenceController(seqController);
                    queueCtrl.setTrackLibrary(library);
                    queueCtrl.setPlaylistManager(playlistManager);
                    
                    queueCtrl.setOnBackAction(() -> mainScene.setRoot(root));
                    
                    mainScene.setRoot(queueRoot);
                    queueCtrl.refresh();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            };

            stage.setTitle("Playlist Manager - Libreria");
            stage.setScene(mainScene);
            
            // TRUCCO PER TESTARE: Premi SPAZIO sulla tastiera per passare alla Coda!
            mainScene.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                    navigazioneVersoCoda.run();
                }
            });
            
            stage.show();

        }

        // Dati d'esempio coerenti col mockup (tracce in libreria + alcune playlist).
        private void seedSampleData(TrackLibrary library, PlaylistManager playlistManager) {
            library.addTrack(new Track("A Me Me Piace O Blues", "Pino D'Aniele", "pop", 2022, "C:/Users/Gabbo/Desktop/SOFTWARE_ARCHITECTURE_DESIGN/File_MP3/A Me Me Piace O Blues (Remastered 2014).mp3.mpeg", 221));
            library.addTrack(new Track("Jailhouse Rock", "Elvis Presley", "Rock", 2019, "C:/Users/Gabbo/Desktop/SOFTWARE ARCHITECTURE DESIGN/File_MP3/Elvis Presley - Jailhouse Rock (Official Audio).mp3.mpeg", 200));
            library.addTrack(new Track("Beat It", "Michael Jackson", "Pop", 2020, null, 203));
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