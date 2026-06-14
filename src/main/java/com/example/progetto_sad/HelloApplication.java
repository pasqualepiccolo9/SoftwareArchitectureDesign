package com.example.progetto_sad;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HelloApplication extends Application {

    private static final int SPLASH_WIDTH = 640;
    private static final int SPLASH_HEIGHT = 360;
    private static final int SPLASH_SECONDS = 2;

    @Override
    public void start(Stage stage) {
        showSplash(stage);

        PauseTransition delay = new PauseTransition(Duration.seconds(SPLASH_SECONDS));
        delay.setOnFinished(event -> ApplicationBootstrap.showHome(stage));
        delay.play();
    }

    private void showSplash(Stage stage) {
        Label title = new Label("SAD Music Player");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        StackPane root = new StackPane(title);
        root.setStyle("-fx-background-color: #111827;");

        Scene scene = new Scene(root, SPLASH_WIDTH, SPLASH_HEIGHT);
        stage.setTitle("SAD Music Player");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
