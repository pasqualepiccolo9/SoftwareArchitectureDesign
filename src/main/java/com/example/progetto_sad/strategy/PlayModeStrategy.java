package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.Track;

import java.util.List;

/**
 * US17 - Pattern Strategy: interfaccia comune a tutte le modalità di riproduzione.
 *
 * Incapsula l'algoritmo di selezione del prossimo brano come oggetto a sé stante,
 * consentendo di variare la modalità di riproduzione in modo trasparente rispetto
 * al resto del sistema.
 *
 * Ruoli del pattern in questo progetto:
 * <ul>
 *   <li><b>Strategy</b> (questa interfaccia): definisce il contratto comune per la
 *       selezione del prossimo brano, senza conoscere la modalità concreta adottata.</li>
 *   <li><b>ConcreteStrategy</b>: le classi che implementeranno la logica specifica
 *       (SequentialModeStrategy, ShuffleModeStrategy, LoopModeStrategy), da sviluppare
 *       nei task successivi.</li>
 *   <li><b>Context</b>: {@code PlayModeContext}, il componente che manterrà la strategia
 *       attiva e delegherà a essa la scelta del prossimo brano.</li>
 *   <li><b>Client</b>: i controller o i componenti applicativi che selezionano la modalità
 *       di riproduzione e la impostano nel Context.</li>
 * </ul>
 */
public interface PlayModeStrategy {

    /**
     * Restituisce il prossimo brano da riprodurre in base alla strategia concreta attiva.
     *
     * @param tracks       lista ordinata dei brani nella sequenza corrente; non deve essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return il prossimo {@link Track} da riprodurre, oppure {@code null} se la sequenza
     *         è terminata secondo la logica della strategia concreta
     */
    Track getNextTrack(List<Track> tracks, int currentIndex);
}
