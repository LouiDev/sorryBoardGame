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
    public Node targetNode(Team team, int steps) {
        if (steps > 0)
            return next.targetNode(team, --steps);

        return content == null
            ? this
            : content.team() == team
                ? null
                : this;
    }

    @Override
    public Node findNode(GameFigure figure, Node searchRoot) {
        if (content == figure)
            return this;
        if (searchRoot == this)
            return null;
        return next.findNode(figure, searchRoot);
    }

    @Override
    public boolean move(GameFigure figure, int steps) {
        if (steps > 0)
            return next.move(figure, --steps);

        if (content != null)
            content.moveToHome();
        content(figure);

        return true;
    }

    public GameFigure content() {
        return content;
    }

    public void content(GameFigure content) {
        this.content = content;
    }
}
