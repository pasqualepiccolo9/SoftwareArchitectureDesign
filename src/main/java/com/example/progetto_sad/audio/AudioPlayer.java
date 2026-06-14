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
 * Espone caricamento, avvio, pausa, seek, arresto/reset ed evento di
 * completamento naturale.
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
     * US11 Mette in pausa la riproduzione corrente.
     * Blocca l'avanzamento dell'audio mantenendo in memoria la posizione attuale
     * per consentire una futura ripresa.
     */
    void pause();
    
    /**
     * US11 Riprende la riproduzione dall'esatto punto in cui era stata sospesa.
     * Non ricarica il file audio e non riavvia il brano dall'inizio.
     */
    void resume();

    /**
     * Sposta la riproduzione alla posizione indicata.
     *
     * @param seconds posizione richiesta in secondi dall'inizio del brano
     */
    void seekTo(int seconds);

    /**
     * US9 - Arresta la riproduzione corrente e riporta il brano caricato all'inizio.
     */
    void stop();

    /**
     * US9 - Registra l'azione da eseguire quando il brano caricato termina in modo naturale.
     *
     * @param onEndOfTrack callback di fine traccia; se null, non viene eseguita alcuna azione
     */
    void setOnEndOfTrack(Runnable onEndOfTrack);
}
