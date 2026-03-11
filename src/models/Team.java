package models;

import linkedlist.TeamRootNode;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

public class Team {
    private final int id;
    private final Color color;
    private final String name;
    private final GameFigure[] home;

    private TeamRootNode rootNode;

    public Team(int id, Color color, String name) {
        this.id = id;
        this.color = color;
        this.name = name;

        home = createHome();
    }

    public boolean isHomeFull() {
        return Arrays.stream(home).allMatch(Objects::nonNull);
    }

    public boolean isGameFigurInHome(GameFigure figure) {
        return Arrays.stream(home).anyMatch(x -> figure == x);
    }

    public int id() {
        return id;
    }

    public Color color() {
        return color;
    }

    public String name() {
        return name;
    }

    public GameFigure[] home() {
        return home;
    }

    public TeamRootNode rootNode() {
        return rootNode;
    }

    private GameFigure[] createHome() {
        return new GameFigure[] {
                new GameFigure(this),
                new GameFigure(this),
                new GameFigure(this),
                new GameFigure(this),
        };
    }

    public void teamRootNode(TeamRootNode node) {
        rootNode = node;
    }

    public TeamRootNode teamRootNode() {
        return rootNode;
    }
}
