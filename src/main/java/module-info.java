module com.example.progetto_sad {
    requires javafx.controls;
    requires javafx.fxml;
    requires jaudiotagger;
    requires java.logging;


    opens com.example.progetto_sad to javafx.fxml;
    opens com.example.progetto_sad.controller to javafx.fxml;
    opens com.example.progetto_sad.view to javafx.graphics;
    exports com.example.progetto_sad;
    opens com.example.progetto_sad.view to javafx.fxml;
}