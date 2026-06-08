module com.example.progetto_sad {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires jaudiotagger;
    requires java.logging;


    opens com.example.progetto_sad to javafx.fxml;
    opens com.example.progetto_sad.controller to javafx.fxml;
    exports com.example.progetto_sad;
    // view aperto a javafx.graphics: il launcher JavaFX istanzia per reflection le
    // Application annidate (AddTrackPreview$App, ModificaTrackView$App, PlaylistView$App).
    // Senza graphics: IllegalAccessException. (I controller FXML stanno in controller/.)
    opens com.example.progetto_sad.view to javafx.fxml, javafx.graphics;
}