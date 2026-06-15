package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistSequence;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.observer.Subject;
import com.example.progetto_sad.strategy.LoopModeStrategy;
import com.example.progetto_sad.strategy.PlayModeContext;
import com.example.progetto_sad.strategy.SequentialModeStrategy;
import com.example.progetto_sad.strategy.ShuffleModeStrategy;

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

    /** US13 - Rappresenta il range [start, end) di una playlist nella sequenza. */
    private static final class PlaylistSegment {
        int start;
        int end; // exclusive

        PlaylistSegment(int start, int end) {
            this.start = start;
            this.end = end;
        }

        boolean contains(int index) {
            return index >= start && index < end;
        }
    }

    private PlaylistSequence sequence;
    private Playlist sourcePlaylist;
    private int sourceTrackCount;
    private final List<Observer> observers;

    /** US13 - Segmenti playlist registrati nella sequenza, in ordine di inserimento. */
    private final List<PlaylistSegment> playlistSegments;

    /** US17 - Contesto Strategy usato per delegare la scelta del prossimo brano. */
    private PlayModeContext playModeContext;

    /** US18-UI/US19 - Modalità di riproduzione corrente, usata solo per la visualizzazione UI. */
    private enum PlayMode { SEQUENTIAL, SHUFFLE, LOOP }
    private PlayMode currentPlayMode;

    /**
     * Crea il controller senza alcuna sequenza attiva.
     * Inizializza il contesto Strategy con la modalità sequenziale come default.
     */
    public PlaylistSequenceController() {
        this.sequence = null;
        this.sourcePlaylist = null;
        this.sourceTrackCount = 0;
        this.observers = new ArrayList<>();
        this.playlistSegments = new ArrayList<>();
        this.playModeContext = new PlayModeContext(new SequentialModeStrategy());
        this.currentPlayMode = PlayMode.SEQUENTIAL;
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
        playlistSegments.clear();
        if (sourceTrackCount > 0) {
            playlistSegments.add(new PlaylistSegment(0, sourceTrackCount));
        }
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
            if (isShuffleMode()) {
                sequence.moveShuffledTrackNext(next);
            } else if (isLoopMode()) {
                sequence.advanceLooping();
            } else {
                sequence.advanceTo(next);
            }
        } else {
            // null in qualsiasi modalità = nessuna traccia futura: avanza allo stato terminato.
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
        if (isLoopMode()) {
            return sequence.getTracks().size() > 0;
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
     * US12/US18 - Sposta la sequenza al brano successivo secondo la modalità attiva e notifica gli observer.
     * In modalità sequenziale avanza di una posizione; in modalità shuffle sceglie una traccia casuale
     * tramite {@link PlayModeContext}, coerentemente con {@link #onTrackFinished()}.
     * Restituisce la nuova traccia corrente se lo spostamento riesce, {@code null} altrimenti.
     *
     * @return la nuova traccia corrente, oppure {@code null} se lo spostamento non è possibile
     */
    public Track goToNextTrack() {
        if (sequence == null) {
            return null;
        }
        if (isLoopMode()) {
            sequence.advanceLooping();
            notifyObservers();
            return sequence.getCurrentTrack();
        }
        Track next = playModeContext.getNextTrack(sequence.getTracks(), sequence.getCurrentIndex());
        if (next != null) {
            if (isShuffleMode()) {
                sequence.moveShuffledTrackNext(next);
            } else {
                sequence.advanceTo(next);
            }
            notifyObservers();
            return sequence.getCurrentTrack();
        }
        return null;
    }

    /**
     * US12 - Sposta la sequenza al brano precedente e notifica gli observer.
     * Restituisce la nuova traccia corrente se lo spostamento riesce, {@code null} altrimenti.
     *
     * @return la nuova traccia corrente, oppure {@code null} se lo spostamento non è possibile
     */
    public Track goToPreviousTrack() {
        if (sequence == null) {
            return null;
        }
        boolean moved = sequence.moveToPreviousTrack();
        if (moved) {
            notifyObservers();
            return sequence.getCurrentTrack();
        }
        return null;
    }

    /**
     * US12 - Sposta la sequenza sulla traccia indicata (selezione manuale / doppio click dalla coda),
     * notifica gli observer e restituisce la traccia corrente aggiornata.
     * Usa {@link PlaylistSequence#moveUpcomingTrackNext(Track)} per preservare le tracce
     * intermedie non ancora riprodotte: la traccia scelta viene portata subito dopo quella
     * corrente e la sequenza avanza di un passo, senza far sparire B in [A,B,C,D] → scelto C.
     * Restituisce {@code null} se la sequenza non è attiva, la traccia è null o non è presente.
     *
     * @param track la traccia di destinazione
     * @return la nuova traccia corrente, oppure {@code null} se lo spostamento non è possibile
     */
    public Track goToTrack(Track track) {
        if (sequence == null || track == null) {
            return null;
        }
        boolean moved = sequence.moveUpcomingTrackNext(track);
        if (moved) {
            notifyObservers();
            return sequence.getCurrentTrack();
        }
        return null;
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

        // Se la sequenza è vuota e non esiste ancora una playlist sorgente,
        // la prima playlist aggiunta diventa la sorgente: sourcePlaylist e
        // sourceTrackCount vengono impostati PRIMA di addTracks, così
        // canSkipPlaylist() può distinguere le tracce sorgente da quelle in coda.
        boolean isFirstPlaylist = sourcePlaylist == null && (sequence == null || sequence.isEmpty());

        if (sequence == null) {
            sequence = PlaylistSequence.empty();
        }

        int startIndex = sequence.getTracks().size();
        sequence.addTracks(tracks);
        int endIndex = sequence.getTracks().size();
        playlistSegments.add(new PlaylistSegment(startIndex, endIndex));

        if (isFirstPlaylist) {
            sourcePlaylist = playlist;
            sourcePlaylist.attach(this);
            sourceTrackCount = endIndex;
        }

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
     * Rimuove dalla sequenza tutte le tracce non piu' presenti nella libreria.
     * Serve a evitare che la coda mantenga riferimenti a brani eliminati.
     *
     * @param libraryTracks tracce ancora presenti nella libreria
     * @return {@code true} se la sequenza e' stata modificata
     */
    public boolean removeTracksNotInLibrary(List<Track> libraryTracks) {
        if (sequence == null) {
            return false;
        }
        List<Integer> removedIndices = sequence.removeTracksNotIn(libraryTracks);
        if (removedIndices.isEmpty()) {
            return false;
        }

        for (Integer removedIndex : removedIndices) {
            registerRemovedIndex(removedIndex);
        }
        notifyObservers();
        return true;
    }

    /**
     * Rimuove il brano attualmente in riproduzione dalla sequenza e notifica gli observer.
     * Se dopo la rimozione esiste un brano successivo, questo diventa il nuovo brano corrente
     * e viene restituito al chiamante affinche' il Player possa avviarlo.
     * Se la sequenza risulta terminata, restituisce {@code null}.
     * Aggiorna {@code sourceTrackCount} se il brano rimosso apparteneva alla playlist sorgente.
     *
     * @return la nuova traccia corrente dopo la rimozione, oppure {@code null} se
     *         la sequenza e' terminata o non era attiva
     */
    public Track removeCurrentTrack() {
        if (sequence == null || sequence.isFinished()) {
            return null;
        }
        if (sequence.getCurrentIndex() < sourceTrackCount) {
            sourceTrackCount = Math.max(0, sourceTrackCount - 1);
        }
        Track next = sequence.removeCurrentTrack();
        notifyObservers();
        return next;
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

    private void registerRemovedIndex(int removedIndex) {
        if (removedIndex < sourceTrackCount) {
            sourceTrackCount = Math.max(0, sourceTrackCount - 1);
        }

        for (PlaylistSegment segment : playlistSegments) {
            if (removedIndex < segment.start) {
                segment.start--;
                segment.end--;
            } else if (removedIndex < segment.end) {
                segment.end--;
            }
        }
        playlistSegments.removeIf(segment -> segment.start >= segment.end);
    }

    /**
     * US13 - Indica se il comando "Skip Playlist" può essere abilitato.
     * Restituisce {@code true} solo quando:
     * <ul>
     *   <li>esiste una sequenza attiva non terminata;</li>
     *   <li>esiste una playlist sorgente identificabile;</li>
     *   <li>la traccia corrente appartiene ancora alla parte della sequenza
     *       proveniente dalla playlist sorgente (indice &lt; sourceTrackCount);</li>
     *   <li>la sequenza contiene almeno una traccia accodata dopo la playlist sorgente.</li>
     * </ul>
     * Questo metodo non esegue lo skip: valuta soltanto la disponibilità del comando.
     *
     * @return {@code tr1ue} se lo skip della playlist avrebbe una destinazione valida
     */
    public boolean canSkipPlaylist() {
        if (sequence == null || sequence.isFinished()) {
            return false;
        }
        int currentIdx = sequence.getCurrentIndex();
        PlaylistSegment segment = findSegmentContaining(currentIdx);
        if (segment == null) {
            return false;
        }
        return segment.end < sequence.getTracks().size();
    }

    private PlaylistSegment findSegmentContaining(int index) {
        for (PlaylistSegment segment : playlistSegments) {
            if (segment.contains(index)) {
                return segment;
            }
        }
        return null;
    }

    /**
     * US13 - Salta la parte rimanente della playlist sorgente e sposta la sequenza
     * alla prima traccia accodata dopo di essa.
     *
     * Lo spostamento avviene direttamente per indice ({@code sourceTrackCount}) tramite
     * {@link PlaylistSequence#moveToIndex(int)}, evitando il rischio di posizionarsi
     * sull'occorrenza sbagliata in presenza di tracce duplicate tra playlist e coda.
     *
     * Non avvia audio e non conosce {@code Player}: restituisce la nuova traccia corrente
     * affinche' il chiamante possa avviarla se necessario.
     *
     * @return la prima traccia accodata dopo la playlist sorgente, oppure {@code null}
     *         se {@link #canSkipPlaylist()} restituisce {@code false}
     */
    public Track skipPlaylist() {
        if (!canSkipPlaylist()) {
            return null;
        }
        int currentIdx = sequence.getCurrentIndex();
        PlaylistSegment segment = findSegmentContaining(currentIdx);
        if (segment == null) {
            return null;
        }
        boolean moved = sequence.moveToIndex(segment.end);
        if (!moved) {
            return null;
        }
        notifyObservers();
        return sequence.getCurrentTrack();
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

    /**
     * US17-INT - Imposta esplicitamente la modalità di riproduzione sequenziale,
     * sostituendo la strategia attiva nel contesto con una nuova {@link SequentialModeStrategy}.
     * Notifica gli observer affinché la UI rifletta il cambio di modalità.
     */
    public void setSequentialMode() {
        playModeContext.setStrategy(new SequentialModeStrategy());
        currentPlayMode = PlayMode.SEQUENTIAL;
        notifyObservers();
    }

    /**
     * US18-M - Imposta la modalità di riproduzione casuale (shuffle),
     * sostituendo la strategia attiva nel contesto con una nuova {@link ShuffleModeStrategy}.
     * Notifica gli observer affinché la UI rifletta il cambio di modalità.
     */
    public void setShuffleMode() {
        playModeContext.setStrategy(new ShuffleModeStrategy());
        currentPlayMode = PlayMode.SHUFFLE;
        notifyObservers();
    }

    /** US18-UI - Indica se la modalità sequenziale è quella attiva. */
    public boolean isSequentialMode() {
        return currentPlayMode == PlayMode.SEQUENTIAL;
    }

    /** US18-UI - Indica se la modalità shuffle è quella attiva. */
    public boolean isShuffleMode() {
        return currentPlayMode == PlayMode.SHUFFLE;
    }

    /**
     * US19 - Imposta la modalità di riproduzione loop (rotazione infinita della coda),
     * sostituendo la strategia attiva con una nuova {@link LoopModeStrategy}.
     * Notifica gli observer affinché la UI rifletta il cambio di modalità.
     */
    public void setLoopMode() {
        playModeContext.setStrategy(new LoopModeStrategy());
        currentPlayMode = PlayMode.LOOP;
        notifyObservers();
    }

    /** US19 - Indica se la modalità loop è quella attiva. */
    public boolean isLoopMode() {
        return currentPlayMode == PlayMode.LOOP;
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
