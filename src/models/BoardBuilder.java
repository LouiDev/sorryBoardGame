package models;

import listener.InputHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BoardBuilder {
    private final List<Team> teams;

    public BoardBuilder() {
        teams = new ArrayList<>();
    }

    public BoardBuilder addTeam(Color color, String name) {
        teams.add(new Team(teams.size() + 1, color, name));
        return this;
    }

    public Board build(InputHandler inputHandler) {
        return new Board(teams.toArray(new Team[0]), inputHandler);
    }
}
