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

    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }
}
