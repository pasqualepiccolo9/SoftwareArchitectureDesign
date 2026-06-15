package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.QueueController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * US16 - View della schermata "Coda di riproduzione".
 *
 * Carica {@code QueueView.fxml} e vi collega il {@link QueueController}
 * (controller FXML). Espone {@link #load} per ottenere il nodo radice da inserire
 * in una scena e un avvio standalone ({@link App}) con dati d'esempio, sullo stile
 * delle altre view del progetto ({@code LibraryView}, {@code PlaylistView}).
 */
public class QueueView {

    /**
     * Carica la vista della coda di riproduzione e restituisce il nodo radice.
     * Inietta il controller fornito come controller FXML e imposta l'azione "indietro".
     *
     * @param controller   il controller della schermata
     * @param onBackAction azione eseguita alla pressione di "← Indietro"
     * @return il nodo radice della vista
     * @throws IllegalStateException se l'FXML non puo' essere caricato
     */
    public static Parent load(QueueController controller, Runnable onBackAction) {
        try {
            FXMLLoader loader = new FXMLLoader(QueueView.class.getResource("QueueView.fxml"));
            loader.setControllerFactory(type -> {
                if (type == QueueController.class) {
                    return controller;
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Impossibile creare il controller " + type.getName(), e);
                }
            });
            Parent root = loader.load();
            controller.setOnBackAction(onBackAction);
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare QueueView.fxml", e);
        }
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    /**
     * Avvio standalone della schermata "Coda di riproduzione" con dati d'esempio.
     *
     * La classe esterna NON estende Application (avvio affidabile da module-path e
     * classpath); l'app JavaFX e' questa classe annidata.
     */
    public static class App extends Application {

        @Override
        public void start(Stage stage) {
            QueueController controller = new QueueController();
            Parent root = QueueView.load(controller, stage::close);

            // dati demo per la visualizzazione standalone (US16)
            controller.setTrackCount(4);
            controller.showEmpty(false);
            controller.setCurrentTrack("Midnight Rain", "Taylor Swift", "3:41");
            controller.addNextTrack(1, "Blinding Lights", "The Weeknd", "3:20");
            controller.addNextTrack(2, "Levitating", "Dua Lipa", "3:23");
            controller.addNextTrack(3, "Heat Waves", "Glass Animals", "3:59");

            stage.setTitle("US16 - Coda di riproduzione");
            stage.setScene(new Scene(root, 900, 640));
            stage.show();
        }
    }
}
