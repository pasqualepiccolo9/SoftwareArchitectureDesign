package com.example.progetto_sad.view;

import com.example.progetto_sad.controller.AddTrackController;
import com.example.progetto_sad.controller.PlayerBarController;
import com.example.progetto_sad.controller.PlaylistSequenceController;
import com.example.progetto_sad.controller.TrackController;
import com.example.progetto_sad.model.Player;
import com.example.progetto_sad.model.TrackLibrary;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

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

    public static Parent buildInlineView(TrackController trackController,
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
            FXMLLoader loader = new FXMLLoader(App.class.getResource("add-track-view.fxml"));
            loader.setControllerFactory(type -> new AddTrackController(trackController));
            Parent formRoot = loader.load();
            AddTrackController controller = loader.getController();
            controller.setInlineMode(wrappedOnBack, wrappedOnSuccess);
            formRoot.setStyle("-fx-padding: 20;");
            VBox.setVgrow(formRoot, Priority.ALWAYS);

            Button backBtn = new Button("← Indietro");
            backBtn.getStyleClass().add("cancel-btn");
            backBtn.setOnAction(e -> wrappedOnBack.run());

            Label titleLabel = new Label("Aggiungi traccia");
            titleLabel.getStyleClass().add("dialog-title");

            HBox header = new HBox(12, backBtn, titleLabel);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setStyle("-fx-padding: 16 20 8 20; -fx-background-color: #141417;");

            FXMLLoader pbLoader = new FXMLLoader(App.class.getResource("PlayerBar.fxml"));
            HBox playerBarNode = pbLoader.load();
            pbcHolder[0] = pbLoader.getController();
            pbcHolder[0].bind(player, seqController, () -> null);

            VBox root = new VBox();
            root.setStyle("-fx-background-color: #141417;");
            var cssUrl = App.class.getResource("add-track-dialog.css");
            if (cssUrl != null) {
                root.getStylesheets().add(cssUrl.toExternalForm());
            }
            root.getChildren().addAll(header, formRoot, playerBarNode);
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare add-track-view.fxml", e);
        }
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
