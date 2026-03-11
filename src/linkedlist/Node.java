package linkedlist;

import models.GameFigure;
import models.Team;

import java.util.List;

public abstract class Node {
    protected Node next;

    public Node() {
        next = new EndNode();
    }

    public Node getNodeForGameFigure(GameFigure figure) {
        return null;
    }

    public void collectAllGameFiguresFromTeam(Team team, Node rootNode, List<GameFigure> result) { }

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
