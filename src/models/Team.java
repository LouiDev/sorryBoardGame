package models;

import tree.TeamRootNode;

import java.awt.*;

public class Team {
    private final int id;
    private final Color color;
    private final GameFigure[] home;

    private TeamRootNode rootNode;

    public Team(int id, Color color) {
        this.id = id;
        this.color = color;

        home = createHome();
    }

    public int id() {
        return id;
    }

    public Color color() {
        return color;
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
}
