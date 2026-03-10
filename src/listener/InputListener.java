package listener;

import models.Board;
import ui.BoardPanel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputListener implements KeyListener {
    private final Board board;
    private final BoardPanel panel;

    public InputListener(Board board, BoardPanel panel) {
        this.board = board;
        this.panel = panel;
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE -> board.update();
            case KeyEvent.VK_LEFT  -> board.navigateSelection(-1);
            case KeyEvent.VK_RIGHT -> board.navigateSelection(1);
        }
        panel.repaint();
    }
}
