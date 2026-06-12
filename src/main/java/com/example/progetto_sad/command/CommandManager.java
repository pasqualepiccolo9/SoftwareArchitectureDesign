package com.example.progetto_sad.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * US22 - Invoker del pattern Command: esegue i comandi e ne mantiene lo storico
 * per consentire l'annullamento (undo) a piu' livelli.
 *
 * I comandi eseguiti vengono conservati in una history list gestita come pila
 * (LIFO): l'annullamento riguarda quindi sempre l'ultima operazione effettuata,
 * come richiesto dalla US22. L'Invoker dialoga con i comandi esclusivamente
 * tramite l'interfaccia {@link Command}, senza conoscere i comandi concreti.
 *
 * Riferimento agli appunti del corso: "salvando i comandi eseguiti in una lista
 * storica (history list) e' possibile scorrerla avanti e indietro chiamando
 * rispettivamente execute() e unexecute()".
 */
public class CommandManager {

    // US22 - history list dei comandi eseguiti, gestita come pila (LIFO): l'ultimo
    // comando eseguito e' il primo a essere annullato. Si usa ArrayDeque perche'
    // preferito a java.util.Stack (legacy e inutilmente sincronizzata).
    private final Deque<Command> history = new ArrayDeque<>();

    /**
     * US22 - Esegue il comando e, se l'esecuzione va a buon fine, lo registra
     * nello storico per un eventuale annullamento.
     *
     * Se {@link Command#execute()} solleva un'eccezione, il comando NON viene
     * inserito nello storico (l'azione non e' avvenuta) e l'eccezione viene
     * propagata al chiamante, che la gestira' come di consueto.
     *
     * @param command il comando da eseguire
     * @throws IllegalArgumentException se command e' null
     */
    public void executeCommand(Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Il comando non puo' essere null");
        }
        command.execute();
        history.push(command);
    }

    /**
     * US22 - Annulla l'ultima operazione eseguita, rimuovendola dallo storico e
     * invocandone {@link Command#unexecute()}.
     *
     * Se lo storico e' vuoto la chiamata e' una no-op: non viene sollevata alcuna
     * eccezione (criterio di accettazione 3), cosi' il pulsante "Annulla" puo'
     * essere invocato senza rischi anche all'avvio dell'applicazione.
     */
    public void undo() {
        if (history.isEmpty()) {
            return;
        }
        Command command = history.pop();
        command.unexecute();
    }

    /**
     * US22 - Indica se esiste almeno un'operazione annullabile nello storico.
     * Usato dalla UI per abilitare o disabilitare il pulsante "Annulla".
     *
     * @return {@code true} se c'e' almeno un comando da annullare
     */
    public boolean canUndo() {
        return !history.isEmpty();
    }
}
