package com.example.progetto_sad.view;

import com.example.progetto_sad.command.CommandManager;
import com.example.progetto_sad.controller.GeneratePlaylistController;
import com.example.progetto_sad.controller.PlayerBarController;
import com.example.progetto_sad.controller.PlaylistController;
import com.example.progetto_sad.controller.PlaylistSequenceController;
import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.TrackLibrary;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

/**
 * US26 - Launcher per la schermata "Genera playlist automatica".
 * Carica generate-playlist-view.fxml e configura GeneratePlaylistController.
 * Espone buildInlineView() per l'integrazione nella scena principale (Library)
 * e show() per l'apertura come finestra modale separata.
 *
 * Avviabile standalone tramite main() per sviluppo e verifica UI isolata.
 */
public class GeneratePlaylistDialogView {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    /**
     * Applicazione JavaFX per l'avvio standalone della schermata.
     * Crea libreria e controller vuoti; la schermata è pienamente funzionante
     * ma non troverà tracce finché non ne vengono aggiunte via TrackLibrary.
     */
    public static class App extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            TrackLibrary library = new TrackLibrary();
            PlaylistManager playlistManager = new PlaylistManager();
            CommandManager commandManager = new CommandManager();
            PlaylistController playlistController = new PlaylistController(playlistManager, commandManager);

            FXMLLoader loader = new FXMLLoader(App.class.getResource("generate-playlist-view.fxml"));
            Parent formRoot = loader.load();
            GeneratePlaylistController ctrl = loader.getController();
            ctrl.setStandaloneMode(library, playlistController, stage, null);

            stage.setTitle("Genera playlist automatica");
            stage.setResizable(false);
            stage.setScene(new Scene(formRoot, 560, 560));
            stage.show();
        }
    }

    /**
     * Carica la schermata in modalità inline: restituisce un Parent da passare a
     * {@code scene.setRoot()}, con la PlayerBar in fondo.
     * Il cleanup della PlayerBar avviene automaticamente al ritorno alla Library.
     */
    public Parent buildInlineView(TrackLibrary library,
                                  PlaylistController playlistController,
                                  Player player,
                                  PlaylistSequenceController seqController,
                                  Runnable onBack,
                                  Runnable onSuccess) {
        PlayerBarController[] pbcHolder = { null };
        Runnable cleanup = () -> {
            if (pbcHolder[0] != null) {
                pbcHolder[0].dispose();
                pbcHolder[0] = null;
            }
        };
        Runnable wrappedOnBack    = () -> { cleanup.run(); onBack.run(); };
        Runnable wrappedOnSuccess = () -> { cleanup.run(); onSuccess.run(); };

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/progetto_sad/view/generate-playlist-view.fxml"));
            Parent formRoot = loader.load();
            GeneratePlaylistController ctrl = loader.getController();
            ctrl.setInlineMode(library, playlistController, wrappedOnBack, wrappedOnSuccess);
            VBox.setVgrow(formRoot, Priority.ALWAYS);

            FXMLLoader pbLoader = new FXMLLoader(
                    getClass().getResource("/com/example/progetto_sad/view/PlayerBar.fxml"));
            HBox playerBarNode = pbLoader.load();
            pbcHolder[0] = pbLoader.getController();
            pbcHolder[0].bind(player, seqController, () -> null);

            VBox root = new VBox();
            root.setStyle("-fx-background-color: #141417;");
            root.getChildren().addAll(formRoot, playerBarNode);
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare generate-playlist-view.fxml", e);
        }
    }

    /**
     * Carica la schermata in modalità standalone: apre una finestra modale separata
     * e attende la chiusura prima di restituire il controllo.
     */
    public void show(TrackLibrary library,
                     PlaylistController playlistController,
                     Window owner,
                     Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/progetto_sad/view/generate-playlist-view.fxml"));
            Parent formRoot = loader.load();
            GeneratePlaylistController ctrl = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) stage.initOwner(owner);
            stage.setResizable(false);
            stage.setTitle("Genera playlist automatica");

            ctrl.setStandaloneMode(library, playlistController, stage, onSuccess);

            stage.setScene(new Scene(formRoot, 560, 560));
            stage.showAndWait();
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare generate-playlist-view.fxml", e);
        }
    }
}
