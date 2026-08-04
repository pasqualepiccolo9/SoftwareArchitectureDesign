package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.PlaylistSequence;
import com.example.progetto_sad.model.Track;

import java.util.List;

/**
 * US17 - Pattern Strategy: interfaccia comune a tutte le modalità di riproduzione.
 *
 * Incapsula l'intero algoritmo di avanzamento della coda come oggetto a sé stante,
 * consentendo di variare la modalità di riproduzione in modo trasparente rispetto
 * al resto del sistema.
 *
 * Ruoli del pattern in questo progetto:
 * <ul>
 *   <li><b>Strategy</b> (questa interfaccia): definisce il contratto comune per la
 *       selezione e l'applicazione del prossimo brano, senza conoscere la modalità
 *       concreta adottata.</li>
 *   <li><b>ConcreteStrategy</b>: {@link SequentialModeStrategy}, {@link ShuffleModeStrategy},
 *       {@link LoopModeStrategy}, ognuna con la propria variante dell'algoritmo.</li>
 *   <li><b>Context</b>: {@link PlayModeContext}, che mantiene la strategia attiva e
 *       le delega ogni operazione di avanzamento.</li>
 *   <li><b>Client</b>: {@code PlaylistSequenceController}, che crea la strategia concreta
 *       e la passa al Context tramite {@link PlayModeContext#activate}.</li>
 * </ul>
 *
 * L'interfaccia dichiara <em>tutte</em> le operazioni che variano al variare della
 * modalità: né il Context né il Client devono ispezionare il tipo concreto della
 * strategia (nessun {@code instanceof}) né ramificare sulla modalità attiva.
 *
 * <b>Contratto comune a tutte le implementazioni:</b>
 * <ul>
 *   <li>{@code currentIndex} individua una posizione valida solo se compreso in
 *       {@code [0, tracks.size())}: fuori da questo intervallo, con lista nulla o vuota,
 *       {@link #getNextTrack} restituisce sempre {@code null}.</li>
 *   <li>{@code null} da {@link #getNextTrack} e {@code false} da {@link #moveToNextTrack}
 *       hanno un unico significato: <em>secondo questa modalità non esiste un brano
 *       successivo</em>. Sta al Client decidere cosa farne (terminare la sequenza al
 *       termine naturale di un brano, oppure restare fermo su uno skip manuale).</li>
 *   <li>Le modalità che coprono la coda una sola volta ({@link SequentialModeStrategy},
 *       {@link ShuffleModeStrategy}) esauriscono i brani e poi segnalano la fine; solo
 *       {@link LoopModeStrategy} è infinita e non segnala mai la fine a coda non vuota.
 *       È l'unica differenza voluta di comportamento tra le modalità.</li>
 * </ul>
 */
public interface PlayModeStrategy {

    /**
     * Identifica la modalità implementata dalla strategia concreta.
     *
     * Va usato esclusivamente per la presentazione (es. evidenziare il pulsante attivo):
     * la logica di riproduzione deve sempre passare per i metodi di questa interfaccia.
     *
     * @return la modalità di riproduzione rappresentata dalla strategia
     */
    PlayMode getMode();

    /**
     * Restituisce il prossimo brano da riprodurre in base alla strategia concreta attiva,
     * senza modificare la sequenza: è la sola parte di selezione dell'algoritmo.
     *
     * @param tracks       lista ordinata dei brani nella sequenza corrente; può essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return il prossimo {@link Track} da riprodurre, oppure {@code null} se la sequenza
     *         è terminata secondo la logica della strategia concreta
     */
    Track getNextTrack(List<Track> tracks, int currentIndex);

    /**
     * Applica alla sequenza l'avanzamento previsto dalla modalità: ogni strategia decide
     * sia quale brano scegliere sia come spostare la coda per raggiungerlo (avanzamento
     * lineare, spostamento della traccia estratta, rotazione della coda).
     *
     * Il metodo non porta mai la sequenza nello stato terminato: si limita a segnalare
     * al Client, tramite il valore di ritorno, che non esistono brani successivi.
     *
     * @param sequence la sequenza da far avanzare; può essere null
     * @return {@code true} se la sequenza è stata spostata su un nuovo brano corrente,
     *         {@code false} se secondo questa modalità non esiste un brano successivo
     */
    boolean moveToNextTrack(PlaylistSequence sequence);

    /**
     * Indica se, secondo questa modalità, esiste un brano successivo a quello corrente.
     *
     * @param sequence la sequenza corrente; può essere null
     * @return {@code true} se un successivo avanzamento è possibile
     */
    boolean hasNextTrack(PlaylistSequence sequence);

    /**
     * Hook invocato dal {@link PlayModeContext} quando questa strategia diventa quella attiva,
     * per preparare la coda alla modalità. L'implementazione predefinita non modifica nulla.
     *
     * @param sequence la sequenza corrente; può essere null
     * @return il numero di brani già riprodotti scartati dalla testa della coda, così che il
     *         Client possa riallineare i metadati collegati agli indici; {@code 0} se la coda
     *         non è stata modificata
     */
    default int onActivated(PlaylistSequence sequence) {
        return 0;
    }
}
