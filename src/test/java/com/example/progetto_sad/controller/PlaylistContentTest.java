package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.observer.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica il comportamento del controller nella visualizzazione
 * del contenuto di una playlist e nell'aggiornamento tramite Observer.
 */
public class PlaylistContentTest {

    private PlaylistController controller;

    /**
     * Inizializza un nuovo controller prima di ogni test.
     */
    @BeforeEach
    void setUp() {
        controller = new PlaylistController(new PlaylistManager());
    }

    @Test
    void testGetTracksNullPlaylistReturnsEmpty() {
        List<Track> result = controller.getPlaylistTracks(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTracksEmptyPlaylistReturnsEmpty() {
        List<Track> result = controller.getPlaylistTracks(new Playlist("Rock"));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testIsPlaylistEmptyNullOrEmpty() {
        assertTrue(controller.isPlaylistEmpty(null));
        assertTrue(controller.isPlaylistEmpty(new Playlist("Jazz")));
    }

    @Test
    void testGetTracksOneTrack() {
        Playlist playlist = new Playlist("Pop");
        Track track = new Track("Song", "Artist", 200, "Pop", 2023);
        playlist.addTrack(track);
        List<Track> result = controller.getPlaylistTracks(playlist);
        assertEquals(1, result.size());
        assertTrue(result.contains(track));
    }

    @Test
    void testGetTracksMultipleTracks() {
        Playlist playlist = new Playlist("Mix");
        Track t1 = new Track("A", "Artist1", 180, "Pop", 2020);
        Track t2 = new Track("B", "Artist2", 240, "Rock", 2021);
        Track t3 = new Track("C", "Artist3", 300, "Jazz", 2022);
        playlist.addTrack(t1);
        playlist.addTrack(t2);
        playlist.addTrack(t3);
        List<Track> result = controller.getPlaylistTracks(playlist);
        assertEquals(3, result.size());
        assertEquals(List.of(t1, t2, t3), result);
    }

    @Test
    void testObserverNotifiedAfterAddTrack() {
        Playlist playlist = new Playlist("Indie");
        int[] count = {0};
        Observer obs = () -> count[0]++;
        playlist.attach(obs);
        controller.addTrackToPlaylist(new Track("Song", "Artist", 200, "Indie", 2024), playlist);
        assertEquals(1, count[0]);
    }

    @Test
    void testObserverNotifiedAfterRemoveTrack() {
        Playlist playlist = new Playlist("Indie");
        Track track = new Track("Song", "Artist", 200, "Indie", 2024);
        playlist.addTrack(track);
        int[] count = {0};
        Observer obs = () -> count[0]++;
        playlist.attach(obs);
        controller.removeTrackFromPlaylist(track, playlist);
        assertEquals(1, count[0]);
    }

    @Test
    void testListUpdatedAfterRemove() {
        Playlist playlist = new Playlist("Soul");
        Track t1 = new Track("First", "Artist", 180, "Soul", 2020);
        Track t2 = new Track("Second", "Artist", 200, "Soul", 2021);
        playlist.addTrack(t1);
        playlist.addTrack(t2);
        controller.removeTrackFromPlaylist(t1, playlist);
        List<Track> result = controller.getPlaylistTracks(playlist);
        assertEquals(1, result.size());
        assertTrue(result.contains(t2));
        assertFalse(result.contains(t1));
    }
}
