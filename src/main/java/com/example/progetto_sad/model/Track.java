package com.example.progetto_sad.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * US1 - Modello di una traccia musicale.
 *
 * Rappresenta un brano della libreria con i suoi dati: titolo, autore,
 * durata, genere e anno di pubblicazione.
 *
 * La durata e' espressa in secondi ed e' in sola lettura (campo final):
 * viene estratta automaticamente dal file audio al momento della creazione
 * (vedi TrackFactory) e non puo' essere modificata dall'utente.
 * Titolo, autore, genere e anno possono essere aggiornati in modifica (US2).
 */
public class Track {

    private String title;
    private String author;
    private final int duration; // durata in secondi, sola lettura (US1/US4)
    private String genre;
    private int year;

    // US3 - playlist in cui questa traccia e' inserita. Serve a rimuoverla da tutte
    // le sue playlist quando viene eliminata (rimozione in cascata), senza interrogare
    // un gestore esterno. La lista e' mantenuta sincronizzata da Playlist.addTrack /
    // Playlist.removeTrack e da PlaylistManager.removePlaylist.
    private final List<Playlist> playlists = new ArrayList<>();

    /**
     * Crea una traccia con i dati forniti.
     *
     * @param title    titolo della traccia
     * @param author   autore/artista
     * @param duration durata in secondi (estratta dal file audio, sola lettura)
     * @param genre    genere musicale
     * @param year     anno di pubblicazione
     */
    public Track(String title, String author, int duration, String genre, int year) {
        this.title = title;
        this.author = author;
        this.duration = duration;
        this.genre = genre;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getDuration() {
        return duration;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    /**
     * US3 - Registra che questa traccia è stata inserita nella playlist indicata.
     * Va invocato solo da {@link Playlist#addTrack(Track)} per mantenere sincronizzato
     * il legame bidirezionale traccia-playlist; non inserisce duplicati.
     *
     * @param playlist la playlist in cui la traccia e' stata inserita
     * @throws IllegalArgumentException se la playlist e' null
     */
    void addPlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("La playlist non puo' essere null");
        }
        if (!playlists.contains(playlist)) {
            playlists.add(playlist);
        }
    }

    /**
     * US3 - Registra che questa traccia non e' piu' presente nella playlist indicata.
     * Va invocato solo da {@link Playlist#removeTrack(Track)} o da
     * {@link PlaylistManager#removePlaylist(Playlist)} per mantenere sincronizzato il legame.
     *
     * @param playlist la playlist da cui la traccia e' stata rimossa
     */
    void removePlaylist(Playlist playlist) {
        playlists.remove(playlist);
    }

    /**
     * US3 - Restituisce le playlist in cui questa traccia compare.
     *
     * @return copia non modificabile della lista delle playlist
     */
    public List<Playlist> getPlaylists() {
        return Collections.unmodifiableList(new ArrayList<>(playlists));
    }

    /**
     * @return rappresentazione testuale sintetica nel formato "titolo - autore (anno)".
     */
    @Override
    public String toString() {
        return title + " - " + author + " (" + year + ")";
    }
}
