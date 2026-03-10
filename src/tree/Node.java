package tree;

import models.GameFigure;

public abstract class Node {
    protected Node next;

    public Node() {
        next = new EndNode();
    }

    public Node getNodeForGameFigure(GameFigure figure) {
        return null;
    }

    protected Node(boolean skipInit) {
        next = null;
    }

    public Node next() {
        return next;
    }

    public void next(Node node) {
        next = node;
    }
}
