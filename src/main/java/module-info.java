module com.example.progetto_sad {
    requires javafx.controls;
    requires javafx.fxml;
    requires jaudiotagger;
    requires java.logging;


    opens com.example.progetto_sad to javafx.fxml;
    opens com.example.progetto_sad.controller to javafx.fxml;
    exports com.example.progetto_sad;
    // view aperto a javafx.fxml (injection @FXML di PlaylistView) e a javafx.graphics
    // (il launcher JavaFX istanzia per reflection le Application annidate come
    // AddTrackPreview$App e ModificaTrackView$App). Senza graphics: IllegalAccessException.
    opens com.example.progetto_sad.view to javafx.fxml, javafx.graphics;
}