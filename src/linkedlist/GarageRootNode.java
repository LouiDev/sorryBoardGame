package linkedlist;

import models.GameFigure;
import models.Team;

public class GarageRootNode extends TeamNode {
    private final GarageNode root;

    public GarageRootNode(Team team) {
        super(team);
        root = createGarage();
    }

    private GarageNode createGarage() {
        GarageNode n = new GarageNode(team);
        GarageNode prev = n;
        for (int i = 0; i < 3; i++) {
            GarageNode newNode = new GarageNode(team);
            prev.next(newNode);
            prev = newNode;
        }
        return n;
    }

    public GarageNode[] garage() {
        GarageNode[] arr = new GarageNode[4];
        root.getAllRec(arr, 0);
        return arr;
    }

    @Override
    public Node targetNode(Team team, int steps) {
        if (steps > 0)
            if (this.team == team)
                return root.targetNode(team, --steps);
            else
                return next.targetNode(team, --steps);

        GameFigure content = content();
        return content == null
                ? this
                : content.team() == team
                    ? null
                    : this;
    }

    @Override
    public boolean move(GameFigure figure, int steps) {
        if (steps > 0)
            if (this.team == figure.team())
                return root.move(figure, --steps);
            else
                return next.move(figure, --steps);

        GameFigure content = content();
        if (content != null)
            content.moveToHome();
        content(figure);

        return true;
    }

    @Override
    public GarageRootNode findGarageRootNode(Team team) {
        if (this.team == team)
            return this;
        return next.findGarageRootNode(team);
    }
}
