package linkedlist;

import exceptions.InvalidTreeStructureException;
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
}
