package models;

import java.util.Arrays;

public class GameFigure {
    private final Team team;

    public GameFigure(Team team) {
        this.team = team;
    }

    public Team team() {
        return team;
    }

    public void moveToHome() {
        GameFigure[] home = team.home();
        for (int i = 0; i < home.length; i++) {
            if (home[i] != null)
                continue;
            home[i] = this;
            break;
        }
    }

    public boolean isInHome() {
        return Arrays.stream(team.home()).anyMatch(x -> x == this);
    }
}
