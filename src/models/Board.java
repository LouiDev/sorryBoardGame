package models;

import tree.ContentNode;
import tree.GarageRootNode;
import tree.Node;
import tree.TeamRootNode;

import java.util.Arrays;
import java.util.Random;

public class Board {
    private final Node root;
    private final Team[] teams;

    private int currentTeamIndex;

    public Board(Team[] teams) {
        this.teams = teams;
        root = createBoard();
        currentTeamIndex = -1;
    }

    public void startGame() {
        Random rnd = new Random();
        currentTeamIndex = 0;
    }

    public void update() {
        Team team = currentTeam();
        assert team != null;

        int roll = rollDice();

        // Häuschen voll
        if (team.isHomeFull()) {
            // Braucht 6 um Figur aufs Spielfeld setzen zu können
            if (roll != 6) {
                endTurn();
                return;
            }
            GameFigure[] home = team.home();
            GameFigure figure = home[0];
            home[0] = null;
            team.teamRootNode().content(figure);

            // Darf nochmal würfeln und bewegen
            // HIER LOGIK EINFÜGEN UM SPIELFIGUR ZU ERMITTELN
            // moveFigure();
        }
    }

    private void moveFigure(GameFigure figure) {

    }

    private void endTurn() {
        currentTeamIndex = currentTeamIndex == teams.length - 1 ? 0 : ++currentTeamIndex;
    }

    private Team currentTeam() {
        return currentTeamIndex == -1 ? null : teams[currentTeamIndex];
    }

    private int rollDice() {
        return new Random().nextInt(1, 7);
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
