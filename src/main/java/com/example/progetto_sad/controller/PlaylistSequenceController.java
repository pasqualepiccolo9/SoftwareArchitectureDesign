package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistSequence;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.observer.Subject;
import com.example.progetto_sad.strategy.PlayModeContext;
import com.example.progetto_sad.strategy.SequentialModeStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * US10 - Controller applicativo per la gestione della sequenza di riproduzione.
 *
 * Organizza la creazione della {@link PlaylistSequence} quando viene avviata una
 * playlist, mantiene la sequenza attiva ed espone la traccia corrente al resto
 * del sistema. Non contiene logica di UI nè dipendenze JavaFX.
 *
 * US17 - Collabora con {@link PlayModeContext} come Client del Pattern Strategy.
 * Espone il punto di aggancio per usare il Context nelle future operazioni di
 * avanzamento della sequenza, senza implementare direttamente la logica delle modalità.
 */
public class PlaylistSequenceController implements Subject, Observer {

    private PlaylistSequence sequence;
    private Playlist sourcePlaylist;
    private int sourceTrackCount;
    private final List<Observer> observers;

    /** US17 - Contesto Strategy usato per delegare la scelta del prossimo brano. */
    private PlayModeContext playModeContext;

    /**
     * Crea il controller senza alcuna sequenza attiva.
     * Inizializza il contesto Strategy con la modalità sequenziale come default.
     */
    public PlaylistSequenceController() {
        this.sequence = null;
        this.sourcePlaylist = null;
        this.sourceTrackCount = 0;
        this.observers = new ArrayList<>();
        this.playModeContext = new PlayModeContext(new SequentialModeStrategy());
    }

    /**
     * Avvia la riproduzione della playlist specificata creando una nuova
     * {@link PlaylistSequence}. La prima traccia della playlist diventa la traccia
     * corrente. Se la playlist è nulla o vuota la sequenza viene comunque creata
     * in stato terminato, senza causare crash.
     *
     * @param playlist la playlist da avviare; puo' essere null o vuota
     */
    public void startPlaylist(Playlist playlist) {
        detachSourcePlaylist();
        sourcePlaylist = playlist;
        if (sourcePlaylist != null) {
            sourcePlaylist.attach(this);
        }
        sequence = PlaylistSequence.from(playlist);
        sourceTrackCount = sequence.getTracks().size();
        notifyObservers();
    }

    /**
     * Restituisce la sequenza attualmente attiva.
     *
     * @return la sequenza corrente, oppure {@code null} se nessuna playlist è stata avviata
     */
    public PlaylistSequence getSequence() {
        return sequence;
    }

    /**
     * Restituisce la traccia attualmente in riproduzione.
     *
     * @return la traccia corrente, oppure {@code null} se nessuna sequenza è attiva
     *         o la sequenza è terminata
     */
    public Track getCurrentTrack() {
        if (sequence == null) {
            return null;
        }
        return sequence.getCurrentTrack();
    }

    /**
     * Notifica il controller che la traccia corrente è terminata naturalmente.
     * Delega a {@link PlayModeContext} la scelta del prossimo brano, poi aggiorna
     * la sequenza di conseguenza. Se la strategia non restituisce un brano successivo,
     * la sequenza viene portata in stato terminato.
     * Se la sequenza non è attiva o è già terminata, la chiamata non ha effetto.
     */
    public void onTrackFinished() {
        if (sequence == null || sequence.isFinished()) {
            return;
        }
        Track next = playModeContext.getNextTrack(sequence.getTracks(), sequence.getCurrentIndex());
        if (next != null) {
            sequence.advanceTo(next);
        } else {
            sequence.advance();
        }
        notifyObservers();
    }

    /**
     * Indica se esiste una sequenza attiva con almeno un brano ancora da riprodurre.
     *
     * @return {@code true} se la sequenza è stata avviata e non è terminata
     */
    public boolean hasActiveSequence() {
        return sequence != null && !sequence.isFinished();
    }

    /**
     * Indica se la sequenza è stata avviata ed è terminata (playlist vuota o
     * ultimo brano concluso).
     *
     * @return {@code true} se la sequenza esiste ma non ha piu' brani da riprodurre
     */
    public boolean isSequenceFinished() {
        return sequence != null && sequence.isFinished();
    }

    /**
     * Restituisce i brani che seguono la traccia corrente nella sequenza attiva.
     * Se nessuna sequenza e' attiva o non ci sono successivi, restituisce una lista vuota.
     *
     * @return copia non modificabile delle tracce successive a quella corrente
     */
    public List<Track> getNextTracks() {
        if (sequence == null) {
            return List.of();
        }
        return sequence.getNextTracks();
    }

    /**
     * US12 - Indica se esiste un brano successivo a quello attualmente in riproduzione.
     * Restituisce {@code false} se nessuna sequenza è attiva.
     *
     * @return {@code true} se esiste un brano successivo nella sequenza corrente
     */
    public boolean hasNextTrack() {
        if (sequence == null) {
            return false;
        }
        return sequence.hasNextTrack();
    }

    /**
     * US12 - Indica se esiste un brano precedente a quello attualmente in riproduzione.
     * Restituisce {@code false} se nessuna sequenza è attiva.
     *
     * @return {@code true} se esiste un brano precedente nella sequenza corrente
     */
    public boolean hasPreviousTrack() {
        if (sequence == null) {
            return false;
        }
        return sequence.hasPreviousTrack();
    }

    /**
     * Aggiunge un singolo brano alla fine della sequenza di riproduzione senza
     * interrompere la traccia corrente. Se nessuna sequenza e' attiva, ne crea una
     * vuota e vi inserisce il brano come primo elemento. Se {@code track} e' null,
     * la chiamata non ha effetto.
     *
     * @param track il brano da accodare
     */
    public void addToQueue(Track track) {
        if (track == null) {
            return;
        }
        if (sequence == null) {
            sequence = PlaylistSequence.empty();
        }
        sequence.addTrack(track);
        notifyObservers();
    }

    /**
     * US15 - Aggiunge tutte le tracce di una playlist alla coda di riproduzione
     * mantenendone l'ordine. La traccia eventualmente in riproduzione non viene
     * interrotta.
     * Se {@code playlist} e' null o non contiene tracce, la chiamata non ha effetto
     * e la coda non viene modificata.
     *
     * @param playlist la playlist le cui tracce devono essere accodate
     */
    public void addPlaylistToQueue(Playlist playlist) {
        if (playlist == null) {
            return;
        }
        List<Track> tracks = playlist.getTracks();
        if (tracks.isEmpty()) {
            return;
        }
        if (sequence == null) {
            sequence = PlaylistSequence.empty();
        }
        sequence.addTracks(tracks);
        notifyObservers();
    }

    /**
     * Rimuove un brano successivo dalla sequenza attiva usando un indice relativo
     * ai soli brani successivi.
     *
     * @param nextIndex indice zero-based tra i brani successivi
     * @return {@code true} se il brano e' stato rimosso, {@code false} altrimenti
     */
    public boolean removeNextTrackAt(int nextIndex) {
        if (sequence == null) {
            return false;
        }
        boolean removed = sequence.removeNextTrackAt(nextIndex);
        if (removed) {
            notifyObservers();
        }
        return removed;
    }

    /**
     * US22 - Sincronizza la sequenza quando la playlist in riproduzione cambia
     * per effetto di un comando di annullamento. La sincronizzazione non controlla
     * il Player: aggiorna solo il modello della sequenza e lascia intatto l'audio.
     */
    @Override
    public void update() {
        if (sourcePlaylist == null || sequence == null) {
            return;
        }
        List<Track> sourceTracks = sourcePlaylist.getTracks();
        boolean changed = sequence.syncWithSourceTracksPreservingCurrent(
                mergeSourceTracksWithQueuedTail(sourceTracks));
        sourceTrackCount = sourceTracks.size();
        if (changed) {
            notifyObservers();
        }
    }

    private List<Track> mergeSourceTracksWithQueuedTail(List<Track> sourceTracks) {
        List<Track> mergedTracks = new ArrayList<>(sourceTracks);
        List<Track> currentTracks = sequence.getTracks();
        int tailStart = Math.min(Math.max(sourceTrackCount, 0), currentTracks.size());
        mergedTracks.addAll(currentTracks.subList(tailStart, currentTracks.size()));
        return mergedTracks;
    }

    /**
     * US17 - Imposta il contesto Strategy da usare per le modalità di riproduzione.
     *
     * @param playModeContext contesto Strategy; non può essere null
     * @throws IllegalArgumentException se playModeContext è null
     */
    public void setPlayModeContext(PlayModeContext playModeContext) {
        if (playModeContext == null) {
            throw new IllegalArgumentException("Il contesto Strategy non può essere null.");
        }
        this.playModeContext = playModeContext;
    }

    /**
     * US17 - Restituisce il contesto Strategy associato al controller.
     *
     * @return il contesto Strategy, oppure {@code null} se non ancora configurato
     */
    public PlayModeContext getPlayModeContext() {
        return playModeContext;
    }

    /**
     * US17 - Indica se il controller ha già un contesto Strategy configurato.
     *
     * @return {@code true} se il contesto Strategy è presente, {@code false} altrimenti
     */
    public boolean hasPlayModeContext() {
        return playModeContext != null;
    }

    private void detachSourcePlaylist() {
        if (sourcePlaylist != null) {
            sourcePlaylist.detach(this);
            sourcePlaylist = null;
            sourceTrackCount = 0;
        }
    }

    @Override
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
