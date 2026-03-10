package tree;

import models.GameFigure;

public class ContentNode extends Node {
    private GameFigure content;

    public ContentNode() {
        next = new EndNode();
        content = null;
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
