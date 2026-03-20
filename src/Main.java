import listener.InputHandler;
import models.Board;
import models.BoardBuilder;
import models.StartTeamPicker;
import ui.BoardFrame;

import javax.swing.SwingUtilities;
import java.awt.*;

void main() throws Exception {
    InputHandler inputHandler = new InputHandler();

    BoardBuilder builder = new BoardBuilder()
            .addTeam(Color.RED, "Rot")
            //.addTeam(Color.YELLOW, "Gelb")
            .addTeam(Color.BLUE, "Blau")
            .addTeam(Color.GREEN, "Grün");

    StartTeamPicker startRoll = new StartTeamPicker(builder.teams(), inputHandler);

    BoardFrame[] frameRef = new BoardFrame[1];
    SwingUtilities.invokeAndWait(() -> {
        frameRef[0] = new BoardFrame(startRoll, inputHandler);
        frameRef[0].setVisible(true);
    });

    int startIndex = startRoll.run();

    Board board = builder.build(inputHandler);
    board.startGame(startIndex);

    SwingUtilities.invokeLater(() -> frameRef[0].switchToBoard(board));

    board.gameLoop();
}
