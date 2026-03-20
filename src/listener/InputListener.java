package listener;

import ui.BoardPanel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputListener implements KeyListener {
    private final InputHandler inputHandler;
    private final BoardPanel panel;

    public InputListener(InputHandler inputHandler, BoardPanel panel) {
        this.inputHandler = inputHandler;
        this.panel = panel;
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) {
        inputHandler.dispatch(e);
        panel.repaint();
    }
}
