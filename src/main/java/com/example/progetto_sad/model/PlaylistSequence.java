package com.example.progetto_sad.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * US10 - Modello della sequenza ordinata di tracce della playlist in riproduzione.
 *
 * Mantiene l'elenco ordinato dei brani e il riferimento alla traccia corrente.
 * La sequenza avanza automaticamente tramite {@link #advance()} (chiamato dal
 * motore di riproduzione), senza esporre comandi manuali di skip o navigazione.
 *
 * La lista interna e' immutabile dall'esterno: modifiche alla sorgente originale
 * (Playlist o List) successive alla creazione non si propagano alla sequenza.
 */
public class PlaylistSequence {

    private final List<Track> tracks;
    private int currentIndex;

    /**
     * Crea una sequenza dall'elenco ordinato di tracce fornito.
     * La lista viene copiata internamente: modifiche esterne successive non influenzano
     * la sequenza.
     *
     * @param tracks elenco ordinato di tracce; se null viene trattato come lista vuota
     */
    public PlaylistSequence(List<Track> tracks) {
        this.tracks = (tracks != null) ? new ArrayList<>(tracks) : new ArrayList<>();
        this.currentIndex = 0;
    }

    /**
     * Crea una sequenza a partire da una playlist.
     * L'ordine dei brani e' quello restituito da {@link Playlist#getTracks()}.
     *
     * @param playlist la playlist sorgente; se null restituisce una sequenza vuota
     * @return una nuova sequenza che riflette il contenuto attuale della playlist
     */
    public static PlaylistSequence from(Playlist playlist) {
        if (playlist == null) {
            return empty();
        }
        return new PlaylistSequence(playlist.getTracks());
    }

    /**
     * Crea una sequenza vuota, senza brani, pronta per ricevere tracce tramite
     * {@link #addTrack(Track)}. La sequenza risulta terminata finche' non viene
     * aggiunto almeno un brano.
     *
     * @return una nuova sequenza vuota
     */
    public static PlaylistSequence empty() {
        return new PlaylistSequence(List.of());
    }

    /**
     * Restituisce la traccia attualmente in riproduzione.
     *
     * @return la traccia corrente, oppure {@code null} se la sequenza e' vuota o terminata
     */
    public Track getCurrentTrack() {
        if (isFinished()) {
            return null;
        }
        return tracks.get(currentIndex);
    }

    /**
     * Restituisce l'elenco ordinato dei brani della sequenza.
     *
     * @return copia non modificabile della lista delle tracce
     */
    public List<Track> getTracks() {
        return Collections.unmodifiableList(new ArrayList<>(tracks));
    }

    /**
     * Indica se la sequenza non contiene brani.
     *
     * @return {@code true} se la sequenza e' vuota
     */
    public boolean isEmpty() {
        return tracks.isEmpty();
    }

    /**
     * Indica se la riproduzione e' terminata, ovvero se l'indice corrente ha superato
     * l'ultimo brano oppure la sequenza e' vuota.
     *
     * @return {@code true} se non ci sono piu' brani da riprodurre
     */
    public boolean isFinished() {
        return tracks.isEmpty() || currentIndex >= tracks.size();
    }

    /**
     * Avanza al brano successivo nella sequenza.
     * Se la sequenza e' gia' terminata, la chiamata non ha effetto.
     */
    public void advance() {
        if (!isFinished()) {
            currentIndex++;
        }
    }

    /**
     * US17 - Restituisce l'indice zero-based del brano attualmente in riproduzione.
     * Necessario per passare la posizione corrente a {@code PlayModeStrategy.getNextTrack}.
     *
     * @return l'indice corrente; può essere uguale a {@code getTracks().size()} se
     *         la sequenza è terminata
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * US17 - Sposta la posizione corrente della sequenza sulla traccia indicata,
     * consentendo a {@code PlaylistSequenceController} di applicare la traccia
     * scelta da {@code PlayModeContext} senza manipolare direttamente l'indice.
     *
     * Ricerca la traccia a partire dalla posizione immediatamente successiva a
     * quella corrente, per gestire correttamente le liste con duplicati. Se non
     * trovata in avanti, ricerca dall'inizio (necessario per strategie non lineari,
     * come loop o shuffle).
     *
     * @param track la traccia su cui posizionare la sequenza; se null o non presente
     *              nella lista, la chiamata non ha effetto
     * @return {@code true} se la sequenza è stata aggiornata, {@code false} altrimenti
     */
    public boolean advanceTo(Track track) {
        if (track == null) {
            return false;
        }

        if (currentIndex >= 0 && currentIndex < tracks.size() && tracks.get(currentIndex).equals(track)) {
            return true;
        }

        for (int i = currentIndex + 1; i < tracks.size(); i++) {
            if (tracks.get(i).equals(track)) {
                currentIndex = i;
                return true;
            }
        }

        int idx = tracks.indexOf(track);
        if (idx >= 0) {
            currentIndex = idx;
            return true;
        }

        return false;
    }

    /**
     * Restituisce i brani che seguono la traccia corrente nell'ordine della sequenza.
     * Se la sequenza e' vuota, terminata o non ha successivi, restituisce una lista vuota.
     *
     * @return copia non modificabile delle tracce successive a quella corrente
     */
    public List<Track> getNextTracks() {
        if (isFinished()) {
            return Collections.emptyList();
        }
        int nextIndex = currentIndex + 1;
        if (nextIndex >= tracks.size()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(tracks.subList(nextIndex, tracks.size())));
    }

    /**
     * US12 - Indica se esiste un brano successivo a quello attualmente in riproduzione.
     * Restituisce {@code false} se la sequenza è terminata o se il brano corrente è l'ultimo.
     *
     * @return {@code true} se ci sono brani dopo quello corrente e la sequenza non è terminata
     */
    public boolean hasNextTrack() {
        return !isFinished() && currentIndex + 1 < tracks.size();
    }

    /**
     * US12 - Indica se esiste un brano precedente a quello attualmente in riproduzione.
     * Restituisce {@code false} se la sequenza è terminata o se il brano corrente è il primo.
     *
     * @return {@code true} se ci sono brani prima di quello corrente e la sequenza non è terminata
     */
    public boolean hasPreviousTrack() {
        return !isFinished() && currentIndex > 0;
    }

    /**
     * US12 - Sposta la posizione corrente al brano successivo.
     *
     * @return {@code true} se lo spostamento è avvenuto, {@code false} se non esiste un brano successivo
     */
    public boolean moveToNextTrack() {
        if (!hasNextTrack()) {
            return false;
        }
        currentIndex++;
        return true;
    }

    /**
     * US12 - Sposta la posizione corrente al brano precedente.
     *
     * @return {@code true} se lo spostamento è avvenuto, {@code false} se non esiste un brano precedente
     */
    public boolean moveToPreviousTrack() {
        if (!hasPreviousTrack()) {
            return false;
        }
        currentIndex--;
        return true;
    }

    /**
     * US13 - Sposta la posizione corrente all'indice assoluto indicato, senza modificare
     * la lista né notificare observer. Usato per saltare direttamente a una posizione
     * nota (es. prima traccia accodata dopo la playlist sorgente) evitando il rischio
     * di posizionarsi sull'occorrenza sbagliata in presenza di tracce duplicate.
     *
     * @param index indice zero-based di destinazione
     * @return {@code true} se lo spostamento è avvenuto, {@code false} se l'indice non è valido
     */
    public boolean moveToIndex(int index) {
        if (index < 0 || index >= tracks.size()) {
            return false;
        }
        currentIndex = index;
        return true;
    }

    /**
     * US22 - Riallinea la sequenza al contenuto aggiornato della playlist sorgente
     * preservando la traccia corrente, cosi' un annullamento non interrompe ne'
     * fa saltare l'audio in corso.
     *
     * @param updatedTracks contenuto aggiornato della playlist sorgente
     * @return {@code true} se la sequenza e' cambiata, {@code false} altrimenti
     */
    public boolean syncWithSourceTracksPreservingCurrent(List<Track> updatedTracks) {
        List<Track> updated = (updatedTracks != null) ? new ArrayList<>(updatedTracks) : new ArrayList<>();

        if (isFinished()) {
            boolean changed = !tracks.equals(updated) || currentIndex != updated.size();
            if (changed) {
                tracks.clear();
                tracks.addAll(updated);
                currentIndex = tracks.size();
            }
            return changed;
        }

        Track current = tracks.get(currentIndex);
        int newCurrentIndex = updated.indexOf(current);
        if (newCurrentIndex < 0) {
            newCurrentIndex = Math.min(currentIndex, updated.size());
            updated.add(newCurrentIndex, current);
        }

        boolean changed = !tracks.equals(updated) || currentIndex != newCurrentIndex;
        if (changed) {
            tracks.clear();
            tracks.addAll(updated);
            currentIndex = newCurrentIndex;
        }
        return changed;
    }

    /**
     * Aggiunge un brano alla fine della sequenza senza alterare la posizione corrente
     * né interrompere la riproduzione in corso. Se la sequenza era terminata, il brano
     * aggiunto diventa il prossimo da riprodurre.
     * Se {@code track} e' null, la chiamata non ha effetto.
     *
     * @param track il brano da accodare
     */
    public void addTrack(Track track) {
        if (track == null) {
            return;
        }
        tracks.add(track);
    }

    /**
     * Aggiunge piu' brani alla fine della sequenza mantenendo l'ordine ricevuto.
     * Le tracce nulle vengono ignorate.
     *
     * @param tracksToAdd brani da accodare
     */
    public void addTracks(List<Track> tracksToAdd) {
        if (tracksToAdd == null) {
            return;
        }
        for (Track track : tracksToAdd) {
            addTrack(track);
        }
    }

    /**
     * Rimuove il brano attualmente in riproduzione dalla sequenza.
     * Dopo la rimozione, l'indice corrente punta al brano che era immediatamente
     * successivo (o la sequenza risulta terminata se non ne esistono altri).
     *
     * @return la nuova traccia corrente dopo la rimozione, oppure {@code null} se
     *         la sequenza era terminata o non rimangono brani
     */
    public Track removeCurrentTrack() {
        if (isFinished()) {
            return null;
        }
        tracks.remove(currentIndex);
        if (isFinished()) {
            return null;
        }
        return tracks.get(currentIndex);
    }

    /**
     * Rimuove un brano successivo a quello corrente usando un indice relativo alla
     * lista restituita da {@link #getNextTracks()}. La traccia corrente e la sua
     * posizione non vengono modificate.
     *
     * @param nextIndex indice zero-based tra i brani successivi
     * @return {@code true} se il brano e' stato rimosso, {@code false} altrimenti
     */
    public boolean removeNextTrackAt(int nextIndex) {
        if (isFinished() || nextIndex < 0) {
            return false;
        }
        int absoluteIndex = currentIndex + 1 + nextIndex;
        if (absoluteIndex < 0 || absoluteIndex >= tracks.size()) {
            return false;
        }
        tracks.remove(absoluteIndex);
        return true;
    }
}
