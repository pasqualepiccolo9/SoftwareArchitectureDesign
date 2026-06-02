package com.example.progetto_sad.model;

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
     * @return rappresentazione testuale sintetica nel formato "titolo - autore (anno)".
     */
    @Override
    public String toString() {
        return title + " - " + author + " (" + year + ")";
    }
}
