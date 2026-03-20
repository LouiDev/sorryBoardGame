package listener;

import java.awt.event.KeyEvent;
import java.util.concurrent.CompletableFuture;

/**
 * Brücke zwischen dem Swing-EDT und dem Spiellogik-Thread.
 *
 * Der Spiellogik-Thread ruft {@link #awaitKeyPress()} auf und erhält ein
 * {@link CompletableFuture}, das abgeschlossen wird, sobald der Nutzer eine
 * Taste drückt. Der EDT-seitige {@link InputListener} ruft {@link #dispatch(KeyEvent)}
 * auf, um das wartende Future zu erfüllen.
 *
 * Es ist immer höchstens ein Future gleichzeitig aktiv. Wird {@code awaitKeyPress()}
 * erneut aufgerufen, bevor das vorherige Future abgeschlossen wurde, wird das alte
 * Future mit einer {@link java.util.concurrent.CancellationException} abgebrochen.
 */
public class InputHandler {

    private volatile CompletableFuture<KeyEvent> pending;

    /**
     * Erstellt ein neues Future und gibt es zurück. Das Future wird von
     * {@link #dispatch(KeyEvent)} erfüllt, sobald eine Taste gedrückt wird.
     */
    public CompletableFuture<KeyEvent> awaitKeyPress() {
        CompletableFuture<KeyEvent> old = pending;
        if (old != null && !old.isDone()) {
            old.cancel(true);
        }

        CompletableFuture<KeyEvent> future = new CompletableFuture<>();
        pending = future;
        return future;
    }

    /**
     * Wird vom EDT aufgerufen. Erfüllt das aktuell wartende Future mit dem
     * übergebenen {@link KeyEvent}. Wenn kein Future wartet, ist der Aufruf
     * ein No-Op.
     */
    public void dispatch(KeyEvent e) {
        CompletableFuture<KeyEvent> current = pending;
        if (current != null && !current.isDone()) {
            current.complete(e);
        }
    }
}
