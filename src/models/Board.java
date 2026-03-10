package models;

import tree.ContentNode;
import tree.EndNode;
import tree.GarageRootNode;
import tree.Node;
import tree.TeamRootNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
    private final Node root;
    private final Team[] teams;

    private int currentTeamIndex;
    private GameState state;
    private int lastRoll;

    private List<GameFigure> selectableFigures;
    private int selectionIndex;

    public Board(Team[] teams) {
        this.teams = teams;
        root = createBoard();
        currentTeamIndex = -1;
        state = GameState.WAITING_FOR_ROLL;
        lastRoll = 0;
        selectableFigures = new ArrayList<>();
        selectionIndex = 0;
    }

    public void startGame() {
        currentTeamIndex = 0;
    }


    public void update() {
        Team team = currentTeam();
        if (team == null)
            return;

        if (state == GameState.WAITING_FOR_ROLL) {
            handleRoll(team);
        } else if (state == GameState.WAITING_FOR_FIGURE_SELECTION) {
            confirmSelection();
        }
    }

    public void navigateSelection(int direction) {
        if (state != GameState.WAITING_FOR_FIGURE_SELECTION)
            return;

        if (selectableFigures.isEmpty())
            return;

        selectionIndex = (selectionIndex + direction + selectableFigures.size()) % selectableFigures.size();
    }


    private void handleRoll(Team team) {
        lastRoll = rollDice();

        if (team.isHomeFull()) {
            // Alle Figuren noch im Häuschen
            if (lastRoll != 6) {
                endTurn();
                return;
            }

            // 6 gewürfelt: erste Heimfigur auf das Startfeld setzen
            placeFirstHomeOnStart(team);
            return;
        }

        // Wählbare Figuren ermitteln
        List<GameFigure> onBoard = figuresOnBoard(team);
        if (onBoard.isEmpty()) {
            endTurn();
            return;
        }

        selectableFigures = onBoard;
        selectionIndex = 0;
        state = GameState.WAITING_FOR_FIGURE_SELECTION;
    }

    private void confirmSelection() {
        if (selectableFigures.isEmpty()) {
            state = GameState.WAITING_FOR_ROLL;
            endTurn();
            return;
        }
        GameFigure figure = selectableFigures.get(selectionIndex);
        moveFigure(figure, lastRoll);
        selectableFigures = new ArrayList<>();
        selectionIndex = 0;
        state = GameState.WAITING_FOR_ROLL;
        endTurn();
    }

    private void placeFirstHomeOnStart(Team team) {
        GameFigure[] home = team.home();
        for (int i = 0; i < home.length; i++) {
            if (home[i] != null) {
                GameFigure figure = home[i];
                home[i] = null;
                team.teamRootNode().content(figure);
                return;
            }
        }
    }

    private void moveFigure(GameFigure figure, int steps) {
        Node currentNode = findNode(figure);
        if (!(currentNode instanceof ContentNode cn))
            return;

        cn.content(null);

        Node target = currentNode;
        for (int i = 0; i < steps; i++) {
            Node next = target.next();
            if (next instanceof EndNode) break;
            target = next;
        }

        if (target instanceof ContentNode targetCn) {
            targetCn.content(figure);
        }
    }

    private Node findNode(GameFigure figure) {
        Node current = root;
        int maxNodes = teams.length * 11 + 10;
        for (int i = 0; i < maxNodes; i++) {
            if (current instanceof ContentNode cn && cn.content() == figure) {
                return current;
            }
            current = current.next();
            if (current == root)
                break;
        }
        return new EndNode();
    }

    private List<GameFigure> figuresOnBoard(Team team) {
        List<GameFigure> result = new ArrayList<>();
        Node current = root;
        int maxNodes = teams.length * 11 + 10;
        for (int i = 0; i < maxNodes; i++) {
            if (current instanceof ContentNode cn) {
                GameFigure fig = cn.content();
                if (fig != null && fig.team() == team) {
                    result.add(fig);
                }
            }
            current = current.next();
            if (current == root)
                break;
        }
        return result;
    }

    private void endTurn() {
        currentTeamIndex = currentTeamIndex == teams.length - 1 ? 0 : ++currentTeamIndex;
        state = GameState.WAITING_FOR_ROLL;
    }

    private int rollDice() {
        return new Random().nextInt(1, 7);
    }


    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Node root() {
        return root;
    }

    public Team[] teams() {
        return teams;
    }

    public GameState state() {
        return state;
    }

    public int lastRoll() {
        return lastRoll;
    }

    public Team currentTeam() {
        return currentTeamIndex == -1 ? null : teams[currentTeamIndex];
    }

    public GameFigure selectedFigure() {
        if (selectableFigures.isEmpty()) return null;
        return selectableFigures.get(selectionIndex);
    }


    // -------------------------------------------------------------------------
    // Brettaufbau
    // -------------------------------------------------------------------------

    private Node createBoard() {
        // Erster TeamRootNode für teams[0] — wird als root gespeichert
        TeamRootNode n = new TeamRootNode(teams[0]);
        teams[0].teamRootNode(n);
        Node prev = n;

        for (int i = 0; i < teams.length; i++) {
            // 8 reguläre Felder
            for (int j = 0; j < 8; j++) {
                ContentNode cn = new ContentNode();
                prev.next(cn);
                prev = cn;
            }

            // Nächstes Team bestimmen (letztes Team zeigt zurück auf teams[0])
            Team nextTeam = i == teams.length - 1 ? teams[0] : teams[i + 1];

            // GarageRootNode für das nächste Team (Zieleingang)
            GarageRootNode grn = new GarageRootNode(nextTeam);
            prev.next(grn);
            prev = grn;

            if (i < teams.length - 1) {
                // Neuen TeamRootNode für das nächste Team anlegen
                TeamRootNode trn = new TeamRootNode(nextTeam);
                prev.next(trn);
                prev = trn;
                nextTeam.teamRootNode(trn);
            } else {
                // Letzten GarageRootNode mit dem ursprünglichen root verknüpfen → zirkuläre Liste
                prev.next(n);
            }
        }

        return n;
    }
}
