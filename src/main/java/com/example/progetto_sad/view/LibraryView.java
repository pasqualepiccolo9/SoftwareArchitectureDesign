package com.example.progetto_sad.view;

import com.example.progetto_sad.ApplicationBootstrap;
import com.example.progetto_sad.command.CommandManager;
import com.example.progetto_sad.controller.LibraryController;
import com.example.progetto_sad.controller.PlaylistSequenceController;
import com.example.progetto_sad.controller.TrackController;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.TrackLibrary;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * US4 - View della schermata principale (home / "Libreria tracce").
 *
 * Carica {@code LibraryView.fxml} e vi collega il {@link LibraryController}
 * (controller FXML). Espone {@link #load} per ottenere il nodo radice da inserire
 * in una scena e un avvio standalone ({@link App}) con bootstrap reale, sullo stile
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
     * Avvio standalone della home con lo stesso bootstrap dell'applicazione reale.
     */
    public static class App extends Application {

        @Override
        public void start(Stage stage) {
            ApplicationBootstrap.showHome(stage);
        }
    }
}
