package com.example.progetto_sad.command;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * US22 - Comando concreto (ConcreteCommand) per la creazione di una playlist.
 * Incapsula l'azione e i dati necessari ad annullarla.
 *
 * Receiver: {@link PlaylistManager}. L'esecuzione delega la creazione al receiver
 * ({@code createPlaylist}) e memorizza la playlist creata; l'annullamento la rimuove,
 * riportando il sistema allo stato precedente.
 */
public class CreatePlaylistCommand implements Command {

    // Receiver del pattern: il gestore delle playlist.
    private final PlaylistManager manager;
    // Parametro dell'azione: il nome della playlist da creare.
    private final String name;
    // Parametro opzionale: tracce iniziali per playlist generate automaticamente.
    private final List<Track> initialTracks;
    // Stato salvato per il ripristino: la playlist creata, da rimuovere in undo.
    private Playlist created;

    /**
     * @param manager il gestore delle playlist (Receiver)
     * @param name il nome della playlist da creare
     * @throws IllegalArgumentException se manager o name sono null
     */
    public CreatePlaylistCommand(PlaylistManager manager, String name) {
        this(manager, name, List.of());
    }

    /**
     * Crea una playlist gia' popolata con le tracce indicate.
     * Usato dalle funzionalita' di generazione automatica che restano comunque
     * una creazione playlist annullabile.
     *
     * @param manager il gestore delle playlist (Receiver)
     * @param name il nome della playlist da creare
     * @param initialTracks tracce iniziali da inserire nella playlist
     * @throws IllegalArgumentException se manager, name o initialTracks sono null,
     *         oppure se initialTracks contiene null o duplicati
     */
    public CreatePlaylistCommand(PlaylistManager manager, String name, List<Track> initialTracks) {
        if (manager == null || name == null) {
            throw new IllegalArgumentException("Manager e nome non possono essere null");
        }
        if (initialTracks == null) {
            throw new IllegalArgumentException("Le tracce iniziali non possono essere null");
        }
        this.manager = manager;
        this.name = name;
        this.initialTracks = copyAndValidateInitialTracks(initialTracks);
    }

    @Override
    public void execute() {
        created = manager.createPlaylist(name);
        for (Track track : initialTracks) {
            created.addTrack(track);
        }
    }

    @Override
    public void unexecute() {
        // US22 - casi limite: se il comando non e' mai stato eseguito (created null)
        // o la playlist non e' piu' registrata nel gestore (gia' eliminata da
        // un'altra operazione), non c'e' nulla da disfare: si ignora senza lanciare
        // (removePlaylist lancerebbe per playlist inesistente).
        if (created != null && manager.getPlaylists().contains(created)) {
            manager.removePlaylist(created);
        }
    }

    /**
     * Restituisce la playlist creata dall'ultima execute().
     *
     * @return playlist creata, oppure null se il comando non e' ancora stato eseguito
     */
    public Playlist getCreatedPlaylist() {
        return created;
    }

    private List<Track> copyAndValidateInitialTracks(List<Track> tracks) {
        List<Track> copy = new ArrayList<>();
        for (Track track : tracks) {
            if (track == null) {
                throw new IllegalArgumentException("Le tracce iniziali non possono contenere null");
            }
            if (copy.contains(track)) {
                throw new IllegalArgumentException("Le tracce iniziali non possono contenere duplicati");
            }
            copy.add(track);
        }
        return List.copyOf(copy);
    }
}
