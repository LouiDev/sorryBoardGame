import models.Board;
import models.BoardBuilder;
import ui.BoardFrame;

import javax.swing.SwingUtilities;
import java.awt.*;

void main() {
    Board board = new BoardBuilder()
            .addTeam(Color.RED)
            .addTeam(Color.YELLOW)
            .addTeam(Color.BLUE)
            .addTeam(Color.GREEN)
            .build();
    board.startGame();

    SwingUtilities.invokeLater(() -> new BoardFrame(board).setVisible(true));
}
