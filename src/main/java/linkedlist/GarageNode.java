package linkedlist;

import exceptions.InvalidTreeStructureException;
import models.GameFigure;
import models.Team;

public class GarageNode extends TeamNode {
    public GarageNode(Team team) {
        super(team);
    }

    public void getAllRec(GarageNode[] arr, int index) {
        arr[index] = this;

        if (next instanceof EndNode)
            return;

        if (!(next instanceof GarageNode garageNode))
            throw new InvalidTreeStructureException("Es wurde ein GarageNode erwartet aber nicht gefunden.");

        garageNode.getAllRec(arr, ++index);
    }

    @Override
    public boolean move(GameFigure figure, int steps) {
        if (steps > 0)
            return next.move(figure, --steps);

        if (content() != null)
            return false;
        content(figure);

        return true;
    }
}
