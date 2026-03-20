package ui;

import listener.InputHandler;
import listener.InputListener;
import models.Board;
import models.StartTeamPicker;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class BoardFrame extends JFrame {

    private final InputHandler inputHandler;

    public BoardFrame(StartTeamPicker startRoll, InputHandler inputHandler) {
        super("Mensch ärgere dich nicht");
        this.inputHandler = inputHandler;

        StartTeamPickerPanel panel = new StartTeamPickerPanel(startRoll);
        startRoll.repaintCallback(panel::repaint);
        add(panel);
        addKeyListener(new InputListener(inputHandler, panel));

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }

    public void switchToBoard(Board board) {
        getContentPane().removeAll();
        BoardPanel panel = new BoardPanel(board);
        board.repaintCallback(panel::repaint);
        add(panel);
        addKeyListener(new InputListener(inputHandler, panel));
        revalidate();
        repaint();
    }
}
