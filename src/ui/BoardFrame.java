package ui;

import listener.InputHandler;
import listener.InputListener;
import models.Board;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class BoardFrame extends JFrame {

    public BoardFrame(Board board, InputHandler inputHandler) {
        super("Mensch ärgere dich nicht");

        BoardPanel panel = new BoardPanel(board);
        board.repaintCallback(panel::repaint);
        add(panel);
        addKeyListener(new InputListener(inputHandler, panel));

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }
}
