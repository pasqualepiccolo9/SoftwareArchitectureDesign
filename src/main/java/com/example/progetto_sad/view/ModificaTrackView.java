package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.ModificaTrackController;
import com.example.progetto_sad.controller.TrackController;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * US2/US3 - Avvio standalone della schermata "Modifica traccia" (per provarla isolata).
 *
 * La classe esterna NON estende Application e ha un main normale (avvio affidabile
 * sia da module-path sia da classpath); l'app JavaFX e' la classe annidata {@link App}.
 * Costruisce una traccia di esempio e apre il form di modifica collegato al TrackController.
 */
public class ModificaTrackView {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    public static class App extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            TrackLibrary library = new TrackLibrary();
            TrackController trackController = new TrackController(library);

            // Traccia di esempio da modificare/eliminare nell'anteprima.
            Track sample = new Track("Midnight Rain", "Taylor Swift", 174, "Pop", 2022);
            library.addTrack(sample);

            FXMLLoader loader = new FXMLLoader(App.class.getResource("modifica-track-view.fxml"));
            loader.setControllerFactory(type -> new ModificaTrackController(trackController, sample));

            Parent root = loader.load();
            stage.setTitle("Modifica traccia");
            stage.setScene(new Scene(root, 560, 620));
            stage.show();
        }
    }
}
