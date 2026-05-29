package com.example.progetto_sad.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistManager {

    private final List<Playlist> playlists = new ArrayList<>();

    public Playlist createPlaylist(String name) {
        // TODO US5: validare il nome (non nullo/non vuoto) prima di creare la playlist.
        Playlist playlist = new Playlist(name);
        playlists.add(playlist);
        return playlist;
    }

    public void removePlaylist(Playlist p) {
        playlists.remove(p);
    }

    public List<Playlist> getPlaylists() {
        return Collections.unmodifiableList(new ArrayList<>(playlists));
    }
}
