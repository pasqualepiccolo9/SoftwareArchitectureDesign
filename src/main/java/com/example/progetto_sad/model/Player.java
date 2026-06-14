package com.example.progetto_sad.model;

import com.example.progetto_sad.audio.AudioPlayer;
import com.example.progetto_sad.audio.JavaFxAudioPlayer;
import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.observer.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * US9 / US11 - Modello del player per la riproduzione di una singola traccia.
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
 * Lo stato del player e' osservabile dalla UI tramite il pattern Observer, cosi'
 * i controller possono reagire ai cambiamenti senza spostare logica nella view.
 */
public class Player implements Subject {

    /**
     * US9 - Stati minimi del player per la riproduzione singola.
     * Esteso con lo stato IN_PAUSA dalla US11.
     */
    public enum PlayerState {
        /** Nessuna riproduzione attiva: stato iniziale, dopo lo stop e dopo la fine del brano. */
        FERMO,
        /** Una traccia e' attualmente in riproduzione. */
        IN_RIPRODUZIONE,
        /** Una traccia e' caricata ma la riproduzione e' temporaneamente sospesa (pausa). */
        IN_PAUSA
    }

    // Singleton - unica istanza condivisa nell'intero sistema (membro statico).
    // L'accesso passa sempre da getInstance(); il costruttore e' privato.
    private static Player instance;

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

    // US21 - observer interessati a stato, traccia corrente, tempo e durata.
    private final List<Observer> observers;

    /**
     * Singleton - Costruttore privato: vieta {@code new Player(...)} dall'esterno.
     * L'unica istanza si ottiene tramite {@link #getInstance()} in produzione,
     * oppure {@link #resetForTesting(AudioPlayer)} negli unit test.
     *
     * Il motore audio resta iniettato come astrazione {@link AudioPlayer} (DIP):
     * il model non dipende dalla classe concreta JavaFX e nei test si usa un doppio.
     *
     * @param audioPlayer il motore audio da usare per la riproduzione
     * @throws IllegalArgumentException se audioPlayer e' null
     */
    private Player(AudioPlayer audioPlayer) {
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
        this.observers = new ArrayList<>();
        this.audioPlayer.setOnEndOfTrack(this::handleEndOfTrack);
    }

    /**
     * Singleton - Punto di accesso globale all'unica istanza del Player.
     *
     * Crea l'istanza alla prima richiesta (lazy initialization) usando il motore
     * audio reale JavaFX. Il metodo e' {@code synchronized} per garantire l'unicita'
     * anche con piu' thread (il Player usa un clock su un thread separato): senza
     * sincronizzazione, due chiamate concorrenti con istanza ancora null potrebbero
     * crearne due, rompendo il pattern.
     *
     * @return l'unica istanza condivisa del Player
     */
    public static synchronized Player getInstance() {
        if (instance == null) {
            instance = new Player(new JavaFxAudioPlayer());
        }
        return instance;
    }

    /**
     * Solo per i test - Reinizializza il Singleton con un motore audio fornito
     * dall'esterno (tipicamente un FakeAudioPlayer), per consentire gli unit test
     * headless del Player. Realizza il "punto di sostituzione" previsto dal pattern
     * (l'implementazione del Singleton puo' essere sostituita per scopi di test o di
     * configurazione). Va invocato nel setUp dei test per isolare ogni caso dallo
     * stato globale del Singleton.
     *
     * @param audioPlayer il motore audio (doppio di test) da iniettare
     * @return l'istanza del Player ricreata per il test
     */
    public static synchronized Player resetForTesting(AudioPlayer audioPlayer) {
        instance = new Player(audioPlayer);
        return instance;
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
        notifyObservers();
    }

    /**
     * US9 - Carica e avvia la traccia selezionata dall'utente.
     *
     * Riusa il flusso gia' definito da {@link #load(Track)} e {@link #play()}:
     * il caricamento porta il tempo a 00:00 e prepara la traccia, poi l'avvio
     * delega al motore audio. Se un'altra traccia e' in riproduzione, il motore
     * audio la ferma e la rilascia durante il caricamento del nuovo file.
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
     * US11-M - Mette in pausa la traccia in riproduzione.
     * Il player passa allo stato IN_PAUSA e l'esecuzione viene bloccata
     * delegando l'azione al motore audio. Il valore di currentTime non viene 
     * azzerato, conservando così il secondo esatto per una futura ripresa.
     * Se il player non è attualmente in riproduzione, la chiamata viene ignorata.
     */
    public synchronized void pause() {
        if (this.state == PlayerState.IN_RIPRODUZIONE) {
            this.state = PlayerState.IN_PAUSA;
            stopClock(); // Spegne il timer così non gira a vuoto consumando risorse
            audioPlayer.pause();
            notifyObservers(); // Aggiorna immediatamente la UI dello stato di pausa
        }
    }
    
    /**
     * US11-M - Riprende la riproduzione dal punto di pausa.
     * Il player torna allo stato IN_RIPRODUZIONE e delega al motore audio
     * la ripartenza. Il brano non viene ricaricato tramite load() e 
     * il currentTime non viene azzerato, garantendo una ripresa fluida.
     * Se il player non si trova in stato IN_PAUSA, la chiamata viene ignorata.
     */
    public synchronized void resume() {
        if (this.state == PlayerState.IN_PAUSA) {
            this.state = PlayerState.IN_RIPRODUZIONE;
            audioPlayer.resume();
            startClock(); // Riaccende il timer esattamente dal secondo in cui era
            notifyObservers(); // Sveglia gli observer della UI
        }
    }

    /**
     * Sposta la traccia corrente alla posizione indicata dalla Player Bar.
     *
     * Il valore viene limitato dentro l'intervallo valido [0, durata]. Se il
     * brano e' in riproduzione o in pausa, il seek viene delegato anche al
     * motore audio reale; se invece non c'e' una traccia caricata, l'operazione
     * viene ignorata.
     *
     * @param seconds posizione richiesta in secondi dall'inizio del brano
     */
    public synchronized void seekTo(int seconds) {
        if (currentTrack == null || getDuration() <= 0) {
            return;
        }

        int duration = getDuration();
        int targetTime = Math.max(0, Math.min(seconds, duration));
        this.currentTime = targetTime;

        if (state == PlayerState.IN_RIPRODUZIONE || state == PlayerState.IN_PAUSA) {
            audioPlayer.seekTo(targetTime);
        }
        notifyObservers();
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
     * ripresa da pausa non e' gestita qui (vedere metodo resume).
     *
     * Se non c'e' una traccia caricata, oppure la traccia caricata non ha durata
     * o percorso audio validi, la chiamata non avvia nulla e lascia il player
     * in stato stabile.
     */
    public synchronized void play() {
        if (!canPlay(currentTrack)) {
            stopActivePlayback();
            notifyObservers();
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
            notifyObservers();
            return;
        }
        this.currentTime = 0;
        this.state = PlayerState.IN_RIPRODUZIONE;
        startClock();
        notifyObservers();
    }

    /**
     * US9 - Arresta la riproduzione corrente e riporta il tempo a 00:00.
     *
     * La traccia corrente resta caricata, cosi' puo' essere riavviata dall'inizio.
     * Se il player e' gia' fermo, l'operazione e' idempotente e mantiene lo
     * stato coerente.
     */
    public synchronized void stop() {
        stopActivePlayback();
        notifyObservers();
    }

    /**
     * Scarica la traccia corrente dal Player e riporta il player allo stato
     * iniziale. Va usato quando la traccia caricata non e' piu' valida nel
     * dominio, ad esempio perche' e' stata eliminata dalla libreria.
     */
    public synchronized void clearCurrentTrack() {
        stopActivePlayback();
        this.currentTrack = null;
        this.currentTime = 0;
        this.state = PlayerState.FERMO;
        notifyObservers();
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
        notifyObservers();
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
        boolean timeAdvanced = false;
        synchronized (this) {
            if (state != PlayerState.IN_RIPRODUZIONE) {
                return;
            }
            int duration = getDuration();
            if (duration <= 0) {
                trackEnded = true;
            } else {
                currentTime = Math.min(Math.max(currentTime, 0) + 1, duration);
                timeAdvanced = true;
                trackEnded = currentTime >= duration;
            }
        }
        if (trackEnded) {
            handleEndOfTrack();
        } else if (timeAdvanced) {
            notifyObservers();
        }
    }

    /**
     * US21 - Registra un osservatore interessato ai cambiamenti del Player.
     *
     * @param observer osservatore da registrare; se null viene ignorato
     */
    @Override
    public synchronized void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * US21 - Rimuove un osservatore precedentemente registrato.
     *
     * @param observer osservatore da rimuovere
     */
    @Override
    public synchronized void detach(Observer observer) {
        observers.remove(observer);
    }

    /**
     * US21 - Notifica alla UI che stato, traccia, tempo o durata potrebbero essere cambiati.
     */
    @Override
    public void notifyObservers() {
        List<Observer> observersSnapshot;
        synchronized (this) {
            observersSnapshot = new ArrayList<>(observers);
        }
        for (Observer observer : observersSnapshot) {
            observer.update();
        }
    }

    /**
     * Arresta definitivamente l'audio sia se il brano sta suonando,
     * sia se si trova momentaneamente in pausa.
     */
    private void stopActivePlayback() {
        boolean deveArrestareAudio = (state == PlayerState.IN_RIPRODUZIONE || state == PlayerState.IN_PAUSA);
        stopClock();
        if (deveArrestareAudio) {
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
