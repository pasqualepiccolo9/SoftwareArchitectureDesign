package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.PlaylistSequence;
import com.example.progetto_sad.model.Track;

import java.util.List;

/**
 * US17 - Pattern Strategy: Context per le modalità di riproduzione.
 *
 * Mantiene un riferimento alla strategia di riproduzione attiva e comunica con essa
 * esclusivamente attraverso l'interfaccia {@link PlayModeStrategy}: non conosce il
 * tipo concreto della strategia collegata né come l'algoritmo venga eseguito.
 *
 * Ruoli del pattern in questo progetto:
 * <ul>
 *   <li><b>Strategy</b>: {@link PlayModeStrategy} — contratto comune alle modalità.</li>
 *   <li><b>Context</b> (questa classe): mantiene la strategia attiva e vi delega ogni
 *       operazione di avanzamento della coda.</li>
 *   <li><b>ConcreteStrategy</b>: {@link SequentialModeStrategy}, {@link ShuffleModeStrategy},
 *       {@link LoopModeStrategy}.</li>
 *   <li><b>Client</b>: {@code PlaylistSequenceController}, che crea la strategia concreta
 *       e la passa al Context tramite {@link #setStrategy} o {@link #activate}.</li>
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
     * Restituisce la modalità corrispondente alla strategia attiva.
     * È l'unica fonte di verità sulla modalità corrente: il Client non deve mantenere
     * flag paralleli, che potrebbero divergere dalla strategia effettivamente collegata.
     *
     * @return la modalità di riproduzione attiva
     */
    public PlayMode getMode() {
        return strategy.getMode();
    }

    /**
     * Collega una nuova strategia e le lascia preparare la coda alla propria modalità.
     *
     * @param strategy la nuova strategia da attivare; non può essere null
     * @param sequence la sequenza corrente; può essere null se nessuna coda è attiva
     * @return il numero di brani già riprodotti scartati dalla testa della coda dalla
     *         nuova strategia, così che il Client possa riallineare i metadati collegati
     *         agli indici; {@code 0} se la coda non è stata modificata
     * @throws IllegalArgumentException se {@code strategy} è null
     */
    public int activate(PlayModeStrategy strategy, PlaylistSequence sequence) {
        setStrategy(strategy);
        if (sequence == null) {
            return 0;
        }
        return this.strategy.onActivated(sequence);
    }

    /**
     * Delega alla strategia attiva la selezione del prossimo brano da riprodurre,
     * senza modificare la sequenza.
     *
     * @param tracks       lista ordinata dei brani nella sequenza corrente; può essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return il prossimo {@link Track} da riprodurre, oppure {@code null} se la sequenza
     *         è terminata secondo la logica della strategia attiva
     */
    public Track getNextTrack(List<Track> tracks, int currentIndex) {
        return strategy.getNextTrack(tracks, currentIndex);
    }

    /**
     * Delega alla strategia attiva l'avanzamento della sequenza sul brano successivo.
     *
     * @param sequence la sequenza da far avanzare; può essere null
     * @return {@code true} se la sequenza è stata spostata su un nuovo brano corrente,
     *         {@code false} se secondo la modalità attiva non esiste un brano successivo
     */
    public boolean moveToNextTrack(PlaylistSequence sequence) {
        return strategy.moveToNextTrack(sequence);
    }

    /**
     * Delega alla strategia attiva la verifica dell'esistenza di un brano successivo.
     *
     * @param sequence la sequenza corrente; può essere null
     * @return {@code true} se un ulteriore avanzamento è possibile nella modalità attiva
     */
    public boolean hasNextTrack(PlaylistSequence sequence) {
        return strategy.hasNextTrack(sequence);
    }
}
