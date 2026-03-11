package linkedlist;

import models.Team;

public class TeamNode extends ContentNode {
    protected final Team team;

    public TeamNode(Team team) {
        super();
        this.team = team;
    }

    public Team team() {
        return team;
    }
}
