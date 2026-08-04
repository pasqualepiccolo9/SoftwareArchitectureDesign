package com.example.progetto_sad.strategy;

import com.example.progetto_sad.model.PlaylistSequence;
import com.example.progetto_sad.model.Track;

import java.util.List;
import java.util.Random;

/**
 * US18 - Pattern Strategy: modalità di riproduzione casuale (shuffle).
 *
 * ConcreteStrategy che riproduce in ordine casuale <em>tutti</em> i brani ancora da
 * riprodurre, ciascuno una sola volta, e poi termina: come la modalità sequenziale
 * copre l'intera coda, ma in ordine imprevedibile. Solo {@link LoopModeStrategy}
 * riparte all'infinito.
 *
 * La strategia è priva di stato interno: i brani non ancora riprodotti sono per
 * definizione quelli che seguono la posizione corrente, perché ogni brano estratto
 * viene portato subito dopo quello corrente da
 * {@link PlaylistSequence#moveShuffledTrackNext(Track)}. È quindi la coda stessa a
 * tenere traccia del ciclo: non serve invalidare alcuna cache quando la coda cambia,
 * e i brani accodati durante la riproduzione entrano automaticamente nel giro.
 *
 * Le occorrenze duplicate presenti più volte in coda vengono riprodotte tutte, perché
 * l'estrazione avviene per posizione e non per identità della traccia.
 */
public class ShuffleModeStrategy implements PlayModeStrategy {

    private final Random random;

    public ShuffleModeStrategy() {
        this(new Random());
    }

    /**
     * @param random generatore controllabile (utile nei test per risultati ripetibili)
     */
    public ShuffleModeStrategy(Random random) {
        this.random = random;
    }

    /** {@inheritDoc} */
    @Override
    public PlayMode getMode() {
        return PlayMode.SHUFFLE;
    }

    /**
     * US18 - Estrae casualmente uno dei brani non ancora riprodotti, ovvero uno di quelli
     * che seguono {@code currentIndex}, senza modificare la lista ricevuta.
     *
     * @param tracks       lista dei brani nella sequenza corrente; può essere null
     * @param currentIndex indice zero-based del brano attualmente in riproduzione
     * @return il brano estratto, oppure {@code null} se non restano brani da riprodurre
     *         o se i parametri non individuano una posizione valida nella coda
     */
    @Override
    public Track getNextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || currentIndex < 0 || currentIndex >= tracks.size()) {
            return null;
        }
        int firstUpcoming = currentIndex + 1;
        int upcomingCount = tracks.size() - firstUpcoming;
        if (upcomingCount <= 0) {
            return null;
        }
        return tracks.get(firstUpcoming + random.nextInt(upcomingCount));
    }

    /**
     * US18 - Porta il brano estratto subito dopo quello corrente e vi avanza, preservando
     * in coda tutti gli altri brani non ancora riprodotti.
     *
     * Poiché l'estrazione avviene sempre tra i brani successivi a quello corrente, lo
     * spostamento trova sempre il brano cercato: quando non resta nulla da riprodurre il
     * metodo si limita a segnalare la fine del ciclo.
     *
     * @param sequence la sequenza da far avanzare; può essere null
     * @return {@code true} se la sequenza è stata spostata sul brano estratto,
     *         {@code false} quando tutti i brani della coda sono stati riprodotti
     */
    @Override
    public boolean moveToNextTrack(PlaylistSequence sequence) {
        if (sequence == null) {
            return false;
        }
        Track next = getNextTrack(sequence.getTracks(), sequence.getCurrentIndex());
        if (next == null) {
            return false;
        }
        return sequence.moveShuffledTrackNext(next);
    }

    /**
     * Indica se resta almeno un brano da estrarre dopo quello corrente.
     *
     * @param sequence la sequenza corrente; può essere null
     * @return {@code true} se il ciclo casuale non è ancora completo
     */
    @Override
    public boolean hasNextTrack(PlaylistSequence sequence) {
        return sequence != null && sequence.hasNextTrack();
    }
}
