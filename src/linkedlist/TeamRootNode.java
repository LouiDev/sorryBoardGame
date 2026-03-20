package linkedlist;

import models.GameFigure;
import models.Team;

public class TeamRootNode extends TeamNode {
    public TeamRootNode(Team team) {
        super(team);
    }

    public boolean isOccupiedByFriendlyFigure() {
        GameFigure content = content();
        return content != null && content.team() == team;
    }
}
