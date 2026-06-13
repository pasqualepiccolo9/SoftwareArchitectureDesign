package com.example.progetto_sad.command;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US22-T - test dell'Invoker del pattern Command.
class CommandManagerTest {

    @Test
    void newCommandManagerCannotUndo() {
        CommandManager manager = new CommandManager();

        assertFalse(manager.canUndo());
    }

    @Test
    void executeCommandRunsCommandAndMakesUndoAvailable() {
        CommandManager manager = new CommandManager();
        RecordingCommand command = new RecordingCommand();

        manager.executeCommand(command);

        assertEquals(1, command.executeCount);
        assertEquals(0, command.unexecuteCount);
        assertTrue(manager.canUndo());
    }

    @Test
    void executeCommandWithNullThrowsAndDoesNotEnableUndo() {
        CommandManager manager = new CommandManager();

        assertThrows(IllegalArgumentException.class, () -> manager.executeCommand(null));

        assertFalse(manager.canUndo());
    }

    @Test
    void undoWithoutHistoryDoesNotThrow() {
        CommandManager manager = new CommandManager();

        assertDoesNotThrow(manager::undo);

        assertFalse(manager.canUndo());
    }

    @Test
    void undoRunsUnexecuteOfLastCommand() {
        CommandManager manager = new CommandManager();
        RecordingCommand command = new RecordingCommand();

        manager.executeCommand(command);
        manager.undo();

        assertEquals(1, command.executeCount);
        assertEquals(1, command.unexecuteCount);
        assertFalse(manager.canUndo());
    }

    @Test
    void undoUsesLifoOrder() {
        CommandManager manager = new CommandManager();
        RecordingCommand first = new RecordingCommand();
        RecordingCommand second = new RecordingCommand();

        manager.executeCommand(first);
        manager.executeCommand(second);
        manager.undo();

        assertEquals(0, first.unexecuteCount);
        assertEquals(1, second.unexecuteCount);
        assertTrue(manager.canUndo());

        manager.undo();

        assertEquals(1, first.unexecuteCount);
        assertFalse(manager.canUndo());
    }

    @Test
    void commandThatFailsDuringExecuteIsNotAddedToHistory() {
        CommandManager manager = new CommandManager();
        RecordingCommand command = new RecordingCommand();
        command.failOnExecute = true;

        assertThrows(RuntimeException.class, () -> manager.executeCommand(command));

        assertEquals(1, command.executeCount);
        assertFalse(manager.canUndo());
    }

    @Test
    void commandThatFailsDuringUndoIsDiscardedWithoutPropagatingException() {
        CommandManager manager = new CommandManager();
        RecordingCommand command = new RecordingCommand();
        command.failOnUnexecute = true;

        manager.executeCommand(command);

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(err));
        try {
            assertDoesNotThrow(manager::undo);
        } finally {
            System.setErr(originalErr);
        }

        assertEquals(1, command.unexecuteCount);
        assertTrue(err.toString().contains("Annullamento non riuscito"));
        assertFalse(manager.canUndo());
    }

    private static final class RecordingCommand implements Command {
        private int executeCount;
        private int unexecuteCount;
        private boolean failOnExecute;
        private boolean failOnUnexecute;

        @Override
        public void execute() {
            executeCount++;
            if (failOnExecute) {
                throw new RuntimeException("execute fallita");
            }
        }

        @Override
        public void unexecute() {
            unexecuteCount++;
            if (failOnUnexecute) {
                throw new RuntimeException("unexecute fallita");
            }
        }
    }
}
