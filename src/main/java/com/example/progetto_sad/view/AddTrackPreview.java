package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.AddTrackController;
import com.example.progetto_sad.controller.TrackController;
import com.example.progetto_sad.model.TrackLibrary;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * US1 - Avvio standalone del form "Aggiungi traccia".
 *
 * La classe esterna NON estende Application e ha un main normale: cosi' l'avvio
 * funziona sia dal module-path sia dal classpath senza l'errore
 * "JavaFX runtime components are missing". L'app JavaFX vera e propria e' la
 * classe annidata {@link App}.
 *
 * Mostra il form collegato a TrackController; nell'app integrata lo stesso form
 * potra' essere aperto come dialog dalla vista principale.
 */
public class AddTrackPreview {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    /**
     * Applicazione JavaFX: costruisce libreria e controller, carica l'FXML
     * e inietta TrackController nel controller del form.
     */
    public static class App extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            TrackLibrary library = new TrackLibrary();
            TrackController trackController = new TrackController(library);

            // Dimostra la coerenza UI-modello: stampa il numero di tracce a ogni modifica.
            library.attach(() -> System.out.println("[Libreria] tracce: " + library.getTracks().size()));

            FXMLLoader loader = new FXMLLoader(App.class.getResource("add-track-view.fxml"));
            // Inietta TrackController nel controller del form (ha un costruttore con dipendenza).
            loader.setControllerFactory(type -> new AddTrackController(trackController));

            Parent root = loader.load();
            stage.setTitle("Aggiungi traccia");
            stage.setScene(new Scene(root, 900, 640));
            stage.show();
        }
    }
}
