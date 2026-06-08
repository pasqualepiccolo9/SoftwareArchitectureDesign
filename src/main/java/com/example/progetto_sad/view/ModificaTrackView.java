package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.ModificaTrackController;
import com.example.progetto_sad.controller.TrackController;
import com.example.progetto_sad.model.Playlist;
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
            Track sample = new Track("Midnight Rain", "Taylor Swift", "Pop", 2022, null, 174);
            library.addTrack(sample);

            // US3 - playlist d'esempio che contengono la traccia: rendono dimostrabile
            // la cascade. Inserendo la traccia, questa "sa" di stare in queste playlist
            // (sincronizzazione); eliminandola dal form viene tolta anche da loro, come
            // mostra l'output di console.
            seedSamplePlaylists(sample);

            FXMLLoader loader = new FXMLLoader(App.class.getResource("modifica-track-view.fxml"));
            loader.setControllerFactory(type -> new ModificaTrackController(trackController, sample));

            Parent root = loader.load();
            stage.setTitle("Modifica traccia");
            stage.setScene(new Scene(root, 560, 620));
            stage.show();
        }

        /**
         * US3 - Crea alcune playlist d'esempio contenenti la traccia, cosi' la rimozione
         * in cascata ha qualcosa da rimuovere nell'anteprima.
         *
         * @param sample la traccia inserita in ogni playlist d'esempio
         */
        private void seedSamplePlaylists(Track sample) {
            addSamplePlaylist("Preferiti", sample);
            addSamplePlaylist("Pop Hits", sample);
        }

        /**
         * US3 - Crea una playlist con la traccia indicata e vi collega un Observer di
         * console: ad ogni cambiamento stampa quante tracce restano, rendendo visibile
         * a runtime la rimozione in cascata quando la traccia viene eliminata.
         *
         * @param name  il nome della playlist da creare
         * @param track la traccia da inserire nella playlist
         */
        private void addSamplePlaylist(String name, Track track) {
            Playlist playlist = new Playlist(name);
            playlist.addTrack(track);
            playlist.attach(() -> System.out.println(
                    "[Playlist '" + name + "'] ora contiene " + playlist.getTracks().size() + " tracce"));
            System.out.println("[Playlist '" + name + "'] creata con " + playlist.getTracks().size() + " tracce");
        }
    }
}
