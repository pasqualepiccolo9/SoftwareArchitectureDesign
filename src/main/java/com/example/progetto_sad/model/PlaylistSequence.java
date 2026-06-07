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
}
