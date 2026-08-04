package com.example.progetto_sad.strategy;

/**
 * US17/US18/US19 - Identificatore della modalità di riproduzione, esposto da ogni
 * ConcreteStrategy tramite {@link PlayModeStrategy#getMode()}.
 *
 * Esiste un'unica fonte di verità sulla modalità attiva: la strategia collegata al
 * {@link PlayModeContext}. Il Client non mantiene quindi alcun flag parallelo che
 * potrebbe divergere dalla strategia effettivamente in uso.
 *
 * Questo valore è pensato solo per la presentazione (evidenziare il pulsante della
 * modalità attiva): nessuna logica di riproduzione deve ramificare su di esso,
 * perché il comportamento è interamente incapsulato nella strategia concreta.
 */
public enum PlayMode {

    /** Riproduzione nell'ordine naturale della coda. */
    SEQUENTIAL,

    /** Riproduzione casuale entro i limiti della coda. */
    SHUFFLE,

    /** Rotazione infinita della coda. */
    LOOP
}
