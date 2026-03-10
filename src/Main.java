import models.Board;
import models.BoardBuilder;
import ui.BoardFrame;

import javax.swing.SwingUtilities;
import java.awt.*;

void main() {
    Board board = new BoardBuilder()
            .addTeam(Color.RED, "Rot")
            .addTeam(Color.YELLOW, "Gelb")
            .addTeam(Color.BLUE, "Blau")
            .addTeam(Color.GREEN, "Grün")
            .build();
    board.startGame();

    SwingUtilities.invokeLater(() -> new BoardFrame(board).setVisible(true));
}
