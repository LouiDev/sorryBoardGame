import models.Board;
import models.BoardBuilder;

import java.awt.*;

void main() {
    Board board = new BoardBuilder()
            .addTeam(Color.RED)
            .addTeam(Color.YELLOW)
            .addTeam(Color.BLUE)
            .addTeam(Color.GREEN)
            .build();
}
