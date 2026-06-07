package com.example.progetto_sad.model;

import com.example.progetto_sad.audio.AudioPlayer;

/**
 * US9 - Modello del player per la riproduzione di una singola traccia.
 *
 * Incapsula lo stato necessario alla riproduzione di un brano: la traccia
 * correntemente caricata, il tempo trascorso (in secondi) e lo stato di
 * riproduzione.
 *
 * Espone il caricamento di una traccia ({@link #load(Track)}) e l'avvio della
 * riproduzione ({@link #play()} / {@link #play(Track)}) tramite un motore audio astratto
 * ({@link AudioPlayer}). Arresto/reset, motore-tempo e gestione della fine brano
 * sono implementati nelle schede US9 successive.
 *
 * Pausa e ripresa (stato IN_PAUSA) non fanno parte di questa classe: appartengono
 * alla US11.
 */
public class Player {

    /**
     * US9 - Stati minimi del player per la riproduzione singola.
     * Lo stato IN_PAUSA verra' aggiunto con la US11.
     */
    public enum PlayerState {
        /** Nessuna riproduzione attiva: stato iniziale, dopo lo stop e dopo la fine del brano. */
        FERMO,
        /** Una traccia e' attualmente in riproduzione. */
        IN_RIPRODUZIONE,
        /** Una traccia e' caricata ma la riproduzione e' temporaneamente sospesa (pausa). */
        IN_PAUSA   /* Attualmente non viene utilizzata ma la inserisco perché ha un overhead irrisorio e perché servirà
                      al mio collega quando si dedicherà alla US11 */
    }

    // US9 - traccia attualmente caricata nel player (null se nessuna)
    private Track currentTrack;

    // US9 - secondi trascorsi della traccia corrente
    private int currentTime;

    // US9 - stato corrente del player
    private PlayerState state;

    // US9 - motore audio (Adapter): astrazione iniettata per disaccoppiare il model
    // da JavaFX (DIP) e consentire i test con un doppio (FakeAudioPlayer).
    private final AudioPlayer audioPlayer;

    /**
     * US9 - Crea un player a riposo (nessuna traccia, tempo a 0, stato
     * {@link PlayerState#FERMO}) collegato al motore audio fornito.
     *
     * Il motore e' iniettato come astrazione {@link AudioPlayer} (DIP): il model
     * non dipende da JavaFX e nei test si puo' usare un doppio.
     *
     * @param audioPlayer il motore audio da usare per la riproduzione
     * @throws IllegalArgumentException se audioPlayer e' null
     */
    public Player(AudioPlayer audioPlayer) {
        if (audioPlayer == null) {
            throw new IllegalArgumentException("Il motore audio non puo' essere null");
        }
        this.audioPlayer = audioPlayer;
        this.currentTrack = null;
        this.currentTime = 0;
        this.state = PlayerState.FERMO;
    }

    /**
     * US9 - Carica nel player la traccia selezionata, collegandola al player.
     *
     * Dopo il caricamento il player "porta con se'" gli attributi della traccia
     * (accessibili tramite {@link #getCurrentTrack()} e {@link #getDuration()}),
     * con il tempo riportato a 0 (00:00) e lo stato {@link PlayerState#FERMO}:
     * la traccia e' pronta ma non ancora avviata.
     *
     * La validazione avviene prima di modificare lo stato, cosi' che con un input
     * non valido il player resti in una condizione stabile.
     *
     * @param track la traccia da caricare
     * @throws IllegalArgumentException se la traccia e' null
     */
    public void load(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("La traccia da caricare non puo' essere null");
        }
        this.currentTrack = track;
        this.currentTime = 0;
        this.state = PlayerState.FERMO;
    }

    /**
     * US9 - Carica e avvia la traccia selezionata dall'utente.
     *
     * Riusa il flusso gia' definito da {@link #load(Track)} e {@link #play()}:
     * il caricamento porta il tempo a 00:00 e prepara la traccia, poi l'avvio
     * delega al motore audio. La ripresa da pausa resta fuori scope (US11).
     *
     * @param track la traccia selezionata da riprodurre
     * @throws IllegalArgumentException se la traccia e' null
     * @throws IllegalStateException se il motore audio non riesce ad avviare la riproduzione
     */
    public void play(Track track) {
        load(track);
        play();
    }

    /**
     * US9 - Avvia la riproduzione della traccia attualmente caricata.
     *
     * Delega l'avvio al motore audio (Adapter) e porta il player nello stato
     * {@link PlayerState#IN_RIPRODUZIONE}. Avvia un nuovo brano dall'inizio: la
     * ripresa da pausa non e' gestita qui (US11).
     *
     * @throws IllegalStateException se non e' caricata alcuna traccia
     */
    public void play() {
        if (currentTrack == null) {
            throw new IllegalStateException("Nessuna traccia caricata da riprodurre");
        }
        audioPlayer.load(currentTrack.getFilePath());
        audioPlayer.play();
        this.state = PlayerState.IN_RIPRODUZIONE;
    }

    /**
     * US9 - Restituisce la traccia attualmente caricata nel player.
     *
     * @return la traccia corrente, oppure {@code null} se nessuna traccia e' caricata
     */
    public Track getCurrentTrack() {
        return currentTrack;
    }

    /**
     * US9 - Restituisce il tempo di riproduzione trascorso.
     *
     * @return i secondi trascorsi della traccia corrente
     */
    public int getCurrentTime() {
        return currentTime;
    }

    /**
     * US9 - Restituisce lo stato corrente del player.
     *
     * @return lo stato di riproduzione corrente
     */
    public PlayerState getState() {
        return state;
    }

    /**
     * US9 - Restituisce la durata della traccia corrente.
     *
     * La durata e' derivata dalla traccia caricata (e non memorizzata in un
     * campo a parte) per non duplicare il dato e mantenerlo sempre coerente
     * con la traccia.
     *
     * @return la durata in secondi della traccia corrente, oppure 0 se nessuna traccia e' caricata
     */
    public int getDuration() {
        return currentTrack != null ? currentTrack.getDuration() : 0;
    }
}
