package linkedlist;

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
}
