package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.Track;

import java.util.List;

/**
 * US17 - Pattern Strategy: Context per le modalità di riproduzione.
 *
 * Mantiene un riferimento alla strategia di riproduzione attiva e delega a essa
 * la scelta del prossimo brano, senza conoscere né implementare la logica concreta
 * (sequenziale, casuale, in loop).
 *
 * Ruoli del pattern in questo progetto:
 * <ul>
 *   <li><b>Strategy</b>: {@link PlayModeStrategy} — contratto comune alle modalità.</li>
 *   <li><b>Context</b> (questa classe): mantiene la strategia attiva e vi delega
 *       la selezione del prossimo brano.</li>
 *   <li><b>ConcreteStrategy</b>: SequentialModeStrategy, ShuffleModeStrategy,
 *       LoopModeStrategy — da implementare nei task successivi.</li>
 *   <li><b>Client</b>: i controller o i componenti applicativi che selezionano
 *       la modalità di riproduzione e la impostano tramite {@link #setStrategy}.</li>
 * </ul>
 */
public class PlayModeContext {

    /** Strategia di riproduzione attualmente attiva. */
    private PlayModeStrategy strategy;

    /**
     * Crea il contesto con la strategia di riproduzione iniziale.
     *
     * @param strategy la strategia iniziale; non può essere null
     * @throws IllegalArgumentException se {@code strategy} è null
     */
    public PlayModeContext(PlayModeStrategy strategy) {
        setStrategy(strategy);
    }

    /**
     * Sostituisce la strategia di riproduzione attiva.
     *
     * @param strategy la nuova strategia; non può essere null
     * @throws IllegalArgumentException se {@code strategy} è null
     */
    public void setStrategy(PlayModeStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("La strategia di riproduzione non può essere null.");
        }
        this.strategy = strategy;
    }

    /**
     * Restituisce la strategia di riproduzione attualmente attiva.
     *
     * @return la strategia attiva
     */
    public PlayModeStrategy getStrategy() {
        return strategy;
    }

    /**
     * Delega alla strategia attiva la selezione del prossimo brano da riprodurre.
     *
     * @param tracks       lista ordinata dei brani nella sequenza corrente; non deve essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return il prossimo {@link Track} da riprodurre, oppure {@code null} se la sequenza
     *         è terminata secondo la logica della strategia attiva
     */
    public Track getNextTrack(List<Track> tracks, int currentIndex) {
        return strategy.getNextTrack(tracks, currentIndex);
    }
}
