package com.example.progetto_sad.util;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * US1 - Estrae la durata (in secondi) da un file audio leggendone gli header.
 *
 * Usata al momento del caricamento del file (Sfoglia) per ricavare la durata
 * automaticamente, senza che l'utente la debba digitare. L'implementazione si
 * appoggia a jaudiotagger ed e' isolata in questa classe per poter essere
 * sostituita facilmente con un'altra libreria.
 */
public final class AudioDurationExtractor {

    static {
        // jaudiotagger logga in modo molto verboso: silenziamo i suoi messaggi.
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    private AudioDurationExtractor() {
    }

    /**
     * Estrae la durata del file audio.
     *
     * @param file file audio selezionato (puo' essere null)
     * @return la durata in secondi, oppure 0 se il file e' nullo, inesistente
     *         o non leggibile/non supportato
     */
    public static int extractSeconds(File file) {
        if (file == null || !file.isFile()) {
            return 0;
        }
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            return Math.max(0, audioFile.getAudioHeader().getTrackLength());
        } catch (Exception e) {
            // file non leggibile o formato non supportato: durata sconosciuta
            return 0;
        }
    }
}
