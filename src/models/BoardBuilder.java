package models;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BoardBuilder {
    private final List<Team> teams;

    public BoardBuilder() {
        teams = new ArrayList<>();
    }

    public BoardBuilder addTeam(Color color) {
        teams.add(new Team(teams.size() + 1, color));
        return this;
    }

    public Board build() {
        return new Board(teams.toArray(new Team[0]));
    }
}
