package listener;

import javax.swing.JPanel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputListener implements KeyListener {
    private final InputHandler inputHandler;
    private final JPanel panel;

    public InputListener(InputHandler inputHandler, JPanel panel) {
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
