package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.PlaylistController;
import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * US8 - View della schermata "contenuto playlist".
 *
 * Carica {@code PlaylistView.fxml} e vi collega il {@link PlaylistController}
 * (che ne e' il controller FXML). Espone {@link #load} per ottenere il nodo radice
 * da inserire in una scena (navigazione) e un avvio standalone ({@link App}) con dati
 * d'esempio per provarla isolata, sullo stile di {@code AddTrackPreview}.
 */
public class PlaylistView {

    /**
     * Carica la vista del contenuto playlist e restituisce il nodo radice pronto da
     * inserire in una scena. Inietta il controller fornito (come controller FXML) e lo
     * inizializza con la playlist, le tracce disponibili e l'azione "indietro".
     *
     * @param playlist        la playlist da visualizzare
     * @param controller      il controller della schermata (anche controller FXML)
     * @param availableTracks tracce selezionabili per l'aggiunta alla playlist
     * @param onBackAction    azione eseguita alla pressione di "← Indietro"
     * @return il nodo radice della vista
     * @throws IllegalStateException se l'FXML non puo' essere caricato
     */
    public static Parent load(Playlist playlist, PlaylistController controller,
                              List<Track> availableTracks, Runnable onBackAction) {
        try {
            FXMLLoader loader = new FXMLLoader(PlaylistView.class.getResource("PlaylistView.fxml"));
            loader.setControllerFactory(type -> controller);
            Parent root = loader.load();
            controller.setAvailableTracks(availableTracks);
            controller.setOnBackAction(onBackAction);
            controller.init(playlist);
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare PlaylistView.fxml", e);
        }
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    /**
     * Avvio standalone della schermata "contenuto playlist" con dati d'esempio.
     *
     * La classe esterna NON estende Application (avvio affidabile da module-path e
     * classpath); l'app JavaFX e' questa classe annidata.
     */
    public static class App extends Application {

        @Override
        public void start(Stage stage) {
            PlaylistManager manager = new PlaylistManager();
            PlaylistController controller = new PlaylistController(manager);

            Playlist playlist = manager.createPlaylist("Chill Vibes");
            Track t1 = new Track("Midnight Drive", "Luna Ray", 224, "Synthwave", 2021);
            Track t2 = new Track("Coffee Break", "Soft Beats", 197, "Lo-fi", 2020);
            Track t3 = new Track("Ocean Lights", "Blue Note", 243, "Ambient", 2022);
            Track t4 = new Track("Late Night Walk", "Urban Echo", 159, "Indie", 2019);
            playlist.addTrack(t1);
            playlist.addTrack(t2);
            playlist.addTrack(t3);
            playlist.addTrack(t4);

            List<Track> availableTracks = List.of(
                    t1, t2, t3, t4,
                    new Track("Golden Hour", "Maya Sol", 211, "Pop", 2023),
                    new Track("Rainy Sunday", "LoFi Room", 186, "Lo-fi", 2022),
                    new Track("City Lights", "Neon Wave", 205, "Synthwave", 2020),
                    new Track("Blue Horizon", "Ocean Drift", 240, "Ambient", 2021)
            );

            Parent root = PlaylistView.load(playlist, controller, availableTracks, stage::close);
            stage.setTitle("US8 - Visualizzazione contenuto playlist");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        }
    }
}
