package com.example.progetto_sad.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistManager {

    private final List<Playlist> playlists;
    
    public PlaylistManager() {
        this.playlists = new ArrayList<>();
    }

    public Playlist createPlaylist(String name) {
        Playlist playlist = new Playlist(name);
        playlists.add(playlist);
        return playlist;
    }
    
public void removePlaylist(Playlist p) {
        if (p == null || !playlists.contains(p)) {
            throw new IllegalArgumentException("Impossibile rimuovere: la playlist specificata non esiste.");
        }
        playlists.remove(p);
    }

    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }
}
