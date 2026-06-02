package com.example.progetto_sad;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * US2/US4 - Controller JavaFX della schermata libreria tracce (hello-view).
 *
 * Mostra l'elenco delle tracce e il form di modifica. Delega la logica a
 * {@link TrackController}; l'aggiornamento automatico della lista e' gestito
 * da {@link LibraryView}, registrata come Observer sulla libreria.
 */
public class HelloController {
    @FXML
    private Label welcomeText;

    /**
     * Inizializza libreria, controller applicativo e vista elenco tracce (US4),
     * e il listener che precompila il form alla selezione (US2).
     */
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
