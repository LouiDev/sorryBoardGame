package models;

import listener.InputHandler;

import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;

public class StartTeamPicker {
    private final Team[] teams;
    private final InputHandler inputHandler;

    private volatile int selectedIndex = 0;
    private volatile Runnable repaintCallback;

    public StartTeamPicker(Team[] teams, InputHandler inputHandler) {
        this.teams = teams;
        this.inputHandler = inputHandler;
    }

    public void repaintCallback(Runnable callback) {
        this.repaintCallback = callback;
    }

    public int run() {
        repaint();
        while (true) {
            try {
                KeyEvent e = inputHandler.awaitKeyPress().get();
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT) {
                    selectedIndex = Math.max(0, selectedIndex - 1);
                    repaint();
                } else if (code == KeyEvent.VK_RIGHT) {
                    selectedIndex = Math.min(teams.length - 1, selectedIndex + 1);
                    repaint();
                } else if (code == KeyEvent.VK_SPACE) {
                    repaint();
                    return selectedIndex;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Spielloop-Thread wurde unterbrochen", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Fehler beim Warten auf Tastendruck", e);
            }
        }
    }

    private void repaint() {
        Runnable cb = repaintCallback;
        if (cb != null) {
            SwingUtilities.invokeLater(cb);
        }
    }

    public Team[] teams() {
        return teams;
    }

    public int selectedIndex() {
        return selectedIndex;
    }
}
