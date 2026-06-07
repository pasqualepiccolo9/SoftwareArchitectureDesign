package com.example.progetto_sad.audio;

/**
 * US9 - Astrazione di dominio del motore audio (pattern Adapter).
 *
 * Definisce le operazioni di riproduzione di cui il model ha bisogno, senza
 * legarsi a una libreria specifica. Il model (es. {@code Player}) dipende solo
 * da questa interfaccia (DIP): cosi' l'accoppiamento con JavaFX resta confinato
 * nell'unica implementazione concreta ({@code JavaFxAudioPlayer}) ed e' possibile
 * usare un doppio nei test (FakeAudioPlayer).
 *
 * In questa fase espone caricamento, avvio ed evento di completamento naturale;
 * le altre operazioni (stop, pausa, seek) vengono aggiunte dalle card successive.
 */
public interface AudioPlayer {

    /**
     * US9 - Prepara il motore audio sul file indicato, pronto per essere avviato.
     *
     * @param filePath percorso del file audio da riprodurre
     * @throws IllegalArgumentException se il percorso e' nullo o vuoto
     */
    void load(String filePath);

    /**
     * US9 - Avvia la riproduzione del file precedentemente caricato.
     */
    void play();

    /**
     * US9 - Registra l'azione da eseguire quando il brano caricato termina in modo naturale.
     *
     * @param onEndOfTrack callback di fine traccia; se null, non viene eseguita alcuna azione
     */
    void setOnEndOfTrack(Runnable onEndOfTrack);
}
