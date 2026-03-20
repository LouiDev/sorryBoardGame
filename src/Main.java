import listener.InputHandler;
import models.Board;
import models.BoardBuilder;
import ui.BoardFrame;

import javax.swing.SwingUtilities;
import java.awt.*;

void main() {
    InputHandler inputHandler = new InputHandler();

    Board board = new BoardBuilder()
            .addTeam(Color.RED, "Rot")
            .addTeam(Color.YELLOW, "Gelb")
            .addTeam(Color.BLUE, "Blau")
            .addTeam(Color.GREEN, "Grün")
            .build(inputHandler);

    board.startGame();

    SwingUtilities.invokeLater(() -> new BoardFrame(board, inputHandler).setVisible(true));

    Thread gameThread = new Thread(board::gameLoop, "spielloop");
    gameThread.setDaemon(true);
    gameThread.start();
}
