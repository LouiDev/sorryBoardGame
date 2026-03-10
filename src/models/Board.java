package models;

import tree.ContentNode;
import tree.GarageRootNode;
import tree.Node;
import tree.TeamRootNode;

public class Board {
    private final Node root;
    private final Team[] teams;

    public Board(Team[] teams) {
        this.teams = teams;
        root = createBoard();
    }

    public Node root() {
        return root;
    }
    public Team[] teams() {
        return teams;
    }

    private Node createBoard() {
        TeamRootNode n = new TeamRootNode(teams[0]);
        Node prev = n;

        for (int i = 0; i < teams.length; i++) {
            // 8 reguläre Felder
            for (int j = 0; j < 8; j++) {
                ContentNode cn = new ContentNode();
                prev.next(cn);
                prev = cn;
            }

            Team nextTeam = i == teams.length - 1 ? teams[0] : teams[i + 1];

            // GarageRootNode für nächstes Team
            GarageRootNode grn = new GarageRootNode(nextTeam);
            prev.next(grn);
            prev = grn;

            // TeamRooNode für nächstes Team
            TeamRootNode trn = new TeamRootNode(nextTeam);
            prev.next(trn);
            prev = trn;
            nextTeam.teamRootNode(trn);
        }

        return n;
    }
}
