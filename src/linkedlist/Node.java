package linkedlist;

import models.GameFigure;
import models.Team;

import java.util.List;

public abstract class Node {
    protected Node next;

    public Node() {
        next = new EndNode();
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

    public boolean move(GameFigure figure, int steps) {
        return false;
    }

    public Node targetNode(Team team, int steps) {
        return null;
    }

    public Node findNode(GameFigure content, Node searchRoot) {
        return null;
    }

    public GarageRootNode findGarageRootNode(Team team) {
        return next.findGarageRootNode(team);
    }
}
