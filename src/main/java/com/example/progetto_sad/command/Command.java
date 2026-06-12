package com.example.progetto_sad.command;

/**
 * US22 - Pattern Command: interfaccia comune a tutti i comandi annullabili.
 *
 * Incapsula un'azione dell'utente come oggetto a se' stante, cosi' che possa
 * essere eseguita, registrata in uno storico e annullata. Secondo gli appunti
 * del corso, il requisito minimo dell'interfaccia e' il metodo {@link #execute()};
 * per supportare l'annullamento (undo) si aggiunge {@link #unexecute()}.
 *
 * Ruoli del pattern in questo progetto:
 * <ul>
 *   <li><b>Command</b> (questa interfaccia): l'astrazione comune con cui l'Invoker
 *       dialoga, senza conoscere il comando concreto.</li>
 *   <li><b>ConcreteCommand</b>: una classe per ogni azione su track/playlist;
 *       memorizza i parametri e un riferimento al Receiver e gli delega
 *       l'operazione effettiva.</li>
 *   <li><b>Receiver</b>: il modello (TrackLibrary / Playlist / PlaylistManager),
 *       che contiene la logica di business dell'azione.</li>
 *   <li><b>Invoker</b>: il CommandManager, che esegue i comandi e li conserva in
 *       una history list (stack) per l'annullamento.</li>
 *   <li><b>Client</b>: i controller, che creano e configurano i comandi.</li>
 * </ul>
 */
public interface Command {

    /**
     * Esegue l'azione incapsulata dal comando, delegando l'operazione effettiva
     * al Receiver.
     */
    void execute();

    /**
     * Annulla l'azione eseguita da {@link #execute()}, riportando il modello
     * allo stato immediatamente precedente all'esecuzione del comando.
     */
    void unexecute();
}
