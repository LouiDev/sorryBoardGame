package linkedlist;

import models.GameFigure;

public class EndNode extends Node {
    public EndNode() {
        super(true);
    }

    @Override
    public Node getNodeForGameFigure(GameFigure figure) {
        return this;
    }

    @Override
    public Node next() {
        return this;
    }
}
