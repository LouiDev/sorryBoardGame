package linkedlist;

import models.GameFigure;
import models.Team;

import java.util.List;

public class ContentNode extends Node {
    private GameFigure content;

    public ContentNode() {
        next = new EndNode();
        content = null;
    }

    @Override
    public void collectAllGameFiguresFromTeam(Team team, Node rootNode, List<GameFigure> result) {
        if (content != null) {
            if (content.team() == team) {
                result.add(content);
            }
        }

        if (this == rootNode)
            return;

        next.collectAllGameFiguresFromTeam(team, rootNode, result);
    }

    @Override
    public Node getNodeForGameFigure(GameFigure figure) {
        if (content == figure)
            return this;
        return next.getNodeForGameFigure(figure);
    }

    public GameFigure content() {
        return content;
    }

    public void content(GameFigure content) {
        this.content = content;
    }
}
