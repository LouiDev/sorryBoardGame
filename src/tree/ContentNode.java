package tree;

import models.GameFigure;

public class ContentNode extends Node {
    private GameFigure content;

    public ContentNode() {
        next = new EndNode();
        content = null;
    }

    public GameFigure content() {
        return content;
    }

    public void content(GameFigure content) {
        this.content = content;
    }
}
