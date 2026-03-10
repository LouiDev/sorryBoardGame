package ui;

import models.Board;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class BoardFrame extends JFrame {

    public BoardFrame(Board board) {
        super("Mensch ärgere dich nicht");

        BoardPanel panel = new BoardPanel(board);
        add(panel);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }
}
