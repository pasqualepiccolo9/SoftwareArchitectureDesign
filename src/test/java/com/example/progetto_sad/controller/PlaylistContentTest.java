package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.observer.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlaylistContentTest {

    private PlaylistController controller;

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
        Playlist p = new Playlist("Pop");
        Track t = new Track("Song", "Artist", 200, "Pop", 2023);
        p.addTrack(t);
        List<Track> result = controller.getPlaylistTracks(p);
        assertEquals(1, result.size());
        assertTrue(result.contains(t));
    }

    @Test
    void testGetTracksMultipleTracks() {
        Playlist p = new Playlist("Mix");
        Track t1 = new Track("A", "Artist1", 180, "Pop", 2020);
        Track t2 = new Track("B", "Artist2", 240, "Rock", 2021);
        Track t3 = new Track("C", "Artist3", 300, "Jazz", 2022);
        p.addTrack(t1);
        p.addTrack(t2);
        p.addTrack(t3);
        List<Track> result = controller.getPlaylistTracks(p);
        assertEquals(3, result.size());
        assertEquals(List.of(t1, t2, t3), result);
    }

    @Test
    void testObserverNotifiedAfterAddTrack() {
        Playlist p = new Playlist("Indie");
        int[] count = {0};
        Observer obs = () -> count[0]++;
        p.attach(obs);
        controller.addTrackToPlaylist(new Track("Song", "Artist", 200, "Indie", 2024), p);
        assertEquals(1, count[0]);
    }

    @Test
    void testObserverNotifiedAfterRemoveTrack() {
        Playlist p = new Playlist("Indie");
        Track t = new Track("Song", "Artist", 200, "Indie", 2024);
        p.addTrack(t);
        int[] count = {0};
        Observer obs = () -> count[0]++;
        p.attach(obs);
        controller.removeTrackFromPlaylist(t, p);
        assertEquals(1, count[0]);
    }

    @Test
    void testListUpdatedAfterRemove() {
        Playlist p = new Playlist("Soul");
        Track t1 = new Track("First", "Artist", 180, "Soul", 2020);
        Track t2 = new Track("Second", "Artist", 200, "Soul", 2021);
        p.addTrack(t1);
        p.addTrack(t2);
        controller.removeTrackFromPlaylist(t1, p);
        List<Track> result = controller.getPlaylistTracks(p);
        assertEquals(1, result.size());
        assertTrue(result.contains(t2));
        assertFalse(result.contains(t1));
    }
}
