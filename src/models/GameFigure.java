package models;

public class GameFigure {
    private final Team team;

    public GameFigure(Team team) {
        this.team = team;
    }

    public Team team() {
        return team;
    }
}
