package com.example.progetto_sad.model;

import com.example.progetto_sad.audio.AudioPlayer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * US9 - Modello del player per la riproduzione di una singola traccia.
 *
 * Incapsula lo stato necessario alla riproduzione di un brano: la traccia
 * correntemente caricata, il tempo trascorso (in secondi) e lo stato di
 * riproduzione.
 *
 * Espone il caricamento di una traccia ({@link #load(Track)}) e l'avvio della
 * riproduzione ({@link #play()} / {@link #play(Track)}) tramite un motore audio astratto
 * ({@link AudioPlayer}). Il completamento naturale del brano riporta il player
 * in stato stabile e pubblica un evento per le integrazioni successive. Se
 * viene avviata una nuova traccia mentre un'altra e' in riproduzione, il motore
 * audio rilascia quella precedente prima di partire col nuovo file. Il tempo
 * corrente avanza tramite un clock interno durante la riproduzione. L'arresto
 * riporta la traccia corrente a 00:00 e lascia il player in stato stabile.
 * Gli stati non validi vengono intercettati prima di avviare il motore audio,
 * cosi' il player resta sempre in una condizione coerente.
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

    // US9 - evento di fine traccia per integrare in futuro la coda senza conoscerla qui.
    private Runnable onEndOfTrack;

    // US9 - scheduler del motore-tempo, attivo solo durante la riproduzione.
    private ScheduledExecutorService clockExecutor;
    private ScheduledFuture<?> clockTask;

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
        this.onEndOfTrack = null;
        this.clockExecutor = null;
        this.clockTask = null;
        this.audioPlayer.setOnEndOfTrack(this::handleEndOfTrack);
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
     * non valido il player resti in una condizione stabile. Se una traccia era
     * in riproduzione, il caricamento di una nuova traccia arresta prima il
     * brano corrente.
     *
     * @param track la traccia da caricare
     * @throws IllegalArgumentException se la traccia e' null
     */
    public synchronized void load(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("La traccia da caricare non puo' essere null");
        }
        stopActivePlayback();
        this.currentTrack = track;
        this.currentTime = 0;
        this.state = PlayerState.FERMO;
    }

    /**
     * US9 - Carica e avvia la traccia selezionata dall'utente.
     *
     * Riusa il flusso gia' definito da {@link #load(Track)} e {@link #play()}:
     * il caricamento porta il tempo a 00:00 e prepara la traccia, poi l'avvio
     * delega al motore audio. Se un'altra traccia e' in riproduzione, il motore
     * audio la ferma e la rilascia durante il caricamento del nuovo file. La
     * ripresa da pausa resta fuori scope (US11).
     *
     * Se la traccia selezionata non e' riproducibile, la chiamata non modifica
     * lo stato corrente.
     *
     * @param track la traccia selezionata da riprodurre
     */
    public synchronized void play(Track track) {
        if (!canPlay(track)) {
            return;
        }
        load(track);
        play();
    }

    /**
     * US9 - Registra l'azione da eseguire quando la traccia termina naturalmente.
     *
     * Serve come punto di integrazione per la coda nelle card successive, senza
     * introdurre logica di coda dentro il Player.
     *
     * @param onEndOfTrack callback di fine traccia; se null, non viene eseguita alcuna azione
     */
    public synchronized void setOnEndOfTrack(Runnable onEndOfTrack) {
        this.onEndOfTrack = onEndOfTrack;
    }

    /**
     * US9 - Avvia la riproduzione della traccia attualmente caricata.
     *
     * Delega l'avvio al motore audio (Adapter) e porta il player nello stato
     * {@link PlayerState#IN_RIPRODUZIONE}. Avvia un nuovo brano dall'inizio: la
     * ripresa da pausa non e' gestita qui (US11).
     *
     * Se non c'e' una traccia caricata, oppure la traccia caricata non ha durata
     * o percorso audio validi, la chiamata non avvia nulla e lascia il player
     * in stato stabile.
     */
    public synchronized void play() {
        if (!canPlay(currentTrack)) {
            stopActivePlayback();
            return;
        }
        stopActivePlayback();
        try {
            audioPlayer.load(currentTrack.getFilePath());
            audioPlayer.play();
        } catch (RuntimeException exception) {
            stopClock();
            audioPlayer.stop();
            this.currentTime = 0;
            this.state = PlayerState.FERMO;
            return;
        }
        this.currentTime = 0;
        this.state = PlayerState.IN_RIPRODUZIONE;
        startClock();
    }

    /**
     * US9 - Arresta la riproduzione corrente e riporta il tempo a 00:00.
     *
     * La traccia corrente resta caricata, cosi' puo' essere riavviata dall'inizio.
     * Se il player e' gia' fermo, l'operazione e' idempotente e mantiene lo
     * stato coerente.
     * Pausa e ripresa dallo stesso punto restano fuori scope (US11).
     */
    public synchronized void stop() {
        stopActivePlayback();
    }

    private void handleEndOfTrack() {
        Runnable callback;
        synchronized (this) {
            stopClock();
            audioPlayer.stop();
            this.currentTime = 0;
            this.state = PlayerState.FERMO;
            callback = onEndOfTrack;
        }
        if (callback != null) {
            callback.run();
        }
    }

    private synchronized void startClock() {
        stopClock();
        clockExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "player-clock");
            thread.setDaemon(true);
            return thread;
        });
        clockTask = clockExecutor.scheduleAtFixedRate(this::advanceTime, 1, 1, TimeUnit.SECONDS);
    }

    private synchronized void stopClock() {
        if (clockTask != null) {
            clockTask.cancel(false);
            clockTask = null;
        }
        if (clockExecutor != null) {
            clockExecutor.shutdownNow();
            clockExecutor = null;
        }
    }

    private void advanceTime() {
        boolean trackEnded = false;
        synchronized (this) {
            if (state != PlayerState.IN_RIPRODUZIONE) {
                return;
            }
            int duration = getDuration();
            if (duration <= 0) {
                trackEnded = true;
            } else {
                currentTime = Math.min(Math.max(currentTime, 0) + 1, duration);
                trackEnded = currentTime >= duration;
            }
        }
        if (trackEnded) {
            handleEndOfTrack();
        }
    }

    private void stopActivePlayback() {
        boolean wasPlaying = state == PlayerState.IN_RIPRODUZIONE;
        stopClock();
        if (wasPlaying) {
            audioPlayer.stop();
        }
        this.currentTime = 0;
        this.state = PlayerState.FERMO;
    }

    private boolean canPlay(Track track) {
        if (track == null) {
            return false;
        }
        if (track.getDuration() <= 0) {
            return false;
        }
        String filePath = track.getFilePath();
        return filePath != null && !filePath.isBlank();
    }

    private int clampCurrentTime() {
        int duration = getDuration();
        if (duration <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(currentTime, duration));
    }

    /**
     * US9 - Restituisce la traccia attualmente caricata nel player.
     *
     * @return la traccia corrente, oppure {@code null} se nessuna traccia e' caricata
     */
    public synchronized Track getCurrentTrack() {
        return currentTrack;
    }

    /**
     * US9 - Restituisce il tempo di riproduzione trascorso.
     *
     * @return i secondi trascorsi della traccia corrente
     */
    public synchronized int getCurrentTime() {
        currentTime = clampCurrentTime();
        return currentTime;
    }

    /**
     * US9 - Restituisce lo stato corrente del player.
     *
     * @return lo stato di riproduzione corrente
     */
    public synchronized PlayerState getState() {
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
    public synchronized int getDuration() {
        return currentTrack != null ? Math.max(0, currentTrack.getDuration()) : 0;
    }
}
