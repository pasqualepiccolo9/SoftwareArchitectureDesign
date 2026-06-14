package com.example.progetto_sad;

import com.example.progetto_sad.command.CommandManager;
import com.example.progetto_sad.controller.PlaylistSequenceController;
import com.example.progetto_sad.controller.TrackController;
import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import com.example.progetto_sad.persistence.JsonDataRepository;
import com.example.progetto_sad.persistence.PersistenceService;
import com.example.progetto_sad.view.LibraryView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Composition root dell'applicazione.
 *
 * Crea e collega model, controller, player e persistenza senza caricare logica
 * applicativa nelle View o nel launcher JavaFX.
 */
public final class ApplicationBootstrap {

    private ApplicationBootstrap() {
    }

    /**
     * Costruisce la home reale dell'applicazione e la mostra nello stage.
     *
     * @param stage stage JavaFX su cui mostrare la home
     */
    public static void showHome(Stage stage) {
        TrackLibrary library = new TrackLibrary();
        PlaylistManager playlistManager = new PlaylistManager();
        JsonDataRepository repository = new JsonDataRepository();
        repository.loadSafelyInto(library, playlistManager);

        CommandManager commandManager = new CommandManager();
        TrackController trackController = new TrackController(library, commandManager);
        PlaylistSequenceController seqController = new PlaylistSequenceController();
        Player player = Player.getInstance();
        PersistenceService persistenceService = new PersistenceService(library, playlistManager, repository);

        player.setOnEndOfTrack(() -> {
            seqController.onTrackFinished();
            Track next = seqController.getCurrentTrack();
            if (next != null) {
                player.play(next);
            }
        });

        persistenceService.startAutoSave();

        Parent root = LibraryView.load(library, trackController, playlistManager, seqController,
                player, commandManager);
        Scene scene = new Scene(root, 1150, 760);
        stage.setTitle("Playlist Manager - Libreria");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setOnCloseRequest(event -> persistenceService.saveNow());
        stage.show();
    }
}
