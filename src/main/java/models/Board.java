package models;

import linkedlist.*;
import listener.InputHandler;

import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Board {
    private final Node root;
    private final Team[] teams;
    private final InputHandler inputHandler;

    private int currentTeamIndex;
    private int lastRoll;

    private List<GameFigure> selectableFigures;
    private int selectionIndex;

    private volatile String currentPhase;
    private Runnable repaintCallback;

    public Board(Team[] teams, InputHandler inputHandler) {
        this.teams = teams;
        this.inputHandler = inputHandler;
        root = createBoard();
        currentTeamIndex = -1;
        lastRoll = 0;
        selectableFigures = new ArrayList<>();
        selectionIndex = 0;
        currentPhase = "SPACE: Würfeln";
    }

    public void repaintCallback(Runnable callback) {
        this.repaintCallback = callback;
    }

    public void startGame(int startTeamIndex) {
        currentTeamIndex = startTeamIndex;
        for (Team team : teams) {
            GameFigure figure = team.home()[0];
            team.home()[0] = null;
            team.teamRootNode().content(figure);
        }
    }

    public void gameLoop() {
        while (true) {
            Team winner = checkWinner();
            if (winner != null) {
                setPhase(winner.name() + " hat gewonnen!");
                return;
            }

            Team team = currentTeam();
            if (team == null)
                return;

            runTurn(team);
        }
    }

    private void runTurn(Team team) {
        lastRoll = 0;
        setPhase("SPACE: Würfeln");
        awaitSpace();

        lastRoll = rollDice();
        repaint();

        selectableFigures = moveableFigures(team, lastRoll);
        if (selectableFigures.isEmpty()) {
            if (figuresOnBoard(team).isEmpty()) {
                // Noch zwei Versuche um 6 zu Würfeln
                for (int i = 2; i > 0; i--) {
                    setPhase("Noch " + i + " Versuch(e)! SPACE: weiter");
                    awaitSpace();

                    lastRoll = 0;
                    setPhase("SPACE: Würfeln");
                    awaitSpace();

                    lastRoll = rollDice();
                    repaint();

                    if (lastRoll == 6) {
                        selectableFigures = moveableFigures(team, lastRoll);
                        break;
                    }
                }

                if (selectableFigures.isEmpty()) {
                    setPhase("Kein Zug möglich! SPACE: Weiter");
                    awaitSpace();
                    endTurn();
                    return;
                }
            } else {
                setPhase("Kein Zug möglich! SPACE: Weiter");
                awaitSpace();
                endTurn();
                return;
            }
        }

        selectionIndex = 0;

        setPhase("← → Wählen   SPACE: OK");
        awaitSelection();
    }

    private void awaitSpace() {
        awaitKey(KeyEvent.VK_SPACE);
    }

    private void awaitSelection() {
        while (true) {
            KeyEvent e = awaitAnyKey();
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> { navigate(-1); repaint(); }
                case KeyEvent.VK_RIGHT -> { navigate(1); repaint(); }
                case KeyEvent.VK_SPACE -> { confirmSelection(); repaint(); return; }
            }
        }
    }

    private void awaitKey(int keyCode) {
        while (true) {
            KeyEvent e = awaitAnyKey();
            if (e.getKeyCode() == keyCode)
                return;
        }
    }

    private KeyEvent awaitAnyKey() {
        try {
            return inputHandler.awaitKeyPress().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Spielloop-Thread wurde unterbrochen", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Fehler beim Warten auf Tastendruck", e);
        }
    }

    private void navigate(int direction) {
        if (selectableFigures.isEmpty()) return;
        selectionIndex = (selectionIndex + direction + selectableFigures.size()) % selectableFigures.size();
    }

    private void confirmSelection() {
        if (selectableFigures.isEmpty()) {
            endTurn();
            return;
        }

        GameFigure figure = selectableFigures.get(selectionIndex);
        Team team = currentTeam();

        if (figure.isInHome()) {
            moveFigureToBoard(team);
        } else {
            moveFigure(figure, lastRoll);
        }

        selectableFigures = new ArrayList<>();
        selectionIndex = 0;
        endTurn();
    }

    private void moveFigureToBoard(Team team) {
        GameFigure[] home = team.home();
        for (int i = 0; i < home.length; i++) {
            if (home[i] != null) {
                GameFigure figure = home[i];
                home[i] = null;
                TeamRootNode trn = team.teamRootNode();
                GameFigure toKick = trn.content();
                if (toKick != null)
                    toKick.moveToHome();
                trn.content(figure);
                return;
            }
        }
    }

    private void moveFigure(GameFigure figure, int steps) {
        GarageNode gn = garageNode(figure);
        Node currentNode = gn != null
                ? gn
                : root.next().findNode(figure, root);

        if (figure.isInHome()) {
            currentNode.move(figure, steps);
        } else {
            boolean result = currentNode.move(figure, steps);
            if (result)
                if (currentNode instanceof ContentNode cn)
                    cn.content(null);
        }
    }

    private List<GameFigure> figuresOnBoard(Team team) {
        List<GameFigure> result = new ArrayList<>();
        Node n = root.next();
        n.collectAllGameFiguresFromTeam(team, root, result);
        return result;
    }

    private List<GameFigure> moveableFigures(Team team, int roll) {
        TeamRootNode trn = team.teamRootNode();

        // Bei Figuren im Häuschen & gewürfelter 6 & freiem Startfeld -> Figur muss aus Häuschen
        if (roll == 6 && !team.isHomeEmpty() && !trn.isOccupiedByFriendlyFigure())
            return Arrays.stream(team.home()).filter(Objects::nonNull).toList();

        // Auf dem Spielbrett (ohne Garage)
        List<GameFigure> result = figuresOnBoard(team);
        result = new ArrayList<>(result.stream().filter(x -> {
            Node n = root.next().findNode(x, root);
            return n.targetNode(team, roll) != null;
        }).toList());

        // Im Häuschen
        boolean includeHome = roll == 6 && !trn.isOccupiedByFriendlyFigure();
        if (includeHome) {
            result.addAll(Arrays.stream(team.home()).filter(Objects::nonNull).toList());
        }

        // In der Garage
        GarageNode[] garage = garage(team);

        for (GarageNode n : garage) {
            GameFigure garageContent = n.content();
            if (garageContent == null)
                continue;
            if (n.targetNode(team, roll) != null)
                result.add(garageContent);
        }

        return result;
    }

    private GarageNode[] garage(Team team) {
        return root.findGarageRootNode(team).garage();
    }

    private Team checkWinner() {
        for (Team team : teams) {
            GarageNode[] garage = garage(team);
            if (Arrays.stream(garage).allMatch(x -> x.content() != null))
                return team;
        }
        return null;
    }

    public GarageNode garageNode(GameFigure figure) {
        GarageRootNode grn = root.findGarageRootNode(figure.team());
        return Arrays.stream(grn.garage()).filter(x -> x.content() != null && x.content() == figure)
                .findFirst()
                .orElse(null);
    }

    private void endTurn() {
        currentTeamIndex = lastRoll == 6
                ? currentTeamIndex
                : currentTeamIndex == teams.length - 1
                    ? 0
                    : ++currentTeamIndex;
        lastRoll = 0;
        selectableFigures = new ArrayList<>();
        selectionIndex = 0;
    }

    private int rollDice() {
        return new Random().nextInt(1, 7);
    }

    private void setPhase(String phase) {
        currentPhase = phase;
        repaint();
    }

    private void repaint() {
        Runnable cb = repaintCallback;
        if (cb != null) {
            SwingUtilities.invokeLater(cb);
        }
    }

    public Node root() {
        return root;
    }

    public Team[] teams() {
        return teams;
    }

    public String currentPhase() {
        return currentPhase;
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

    private Node createBoard() {
        TeamRootNode n = new TeamRootNode(teams[0]);
        teams[0].teamRootNode(n);
        Node prev = n;

        for (int i = 0; i < teams.length; i++) {
            for (int j = 0; j < 8; j++) {
                ContentNode cn = new ContentNode();
                prev.next(cn);
                prev = cn;
            }

            Team nextTeam = i == teams.length - 1 ? teams[0] : teams[i + 1];

            GarageRootNode grn = new GarageRootNode(nextTeam);
            prev.next(grn);
            prev = grn;

            if (i < teams.length - 1) {
                TeamRootNode trn = new TeamRootNode(nextTeam);
                prev.next(trn);
                prev = trn;
                nextTeam.teamRootNode(trn);
            } else {
                prev.next(n);
            }
        }

        return n;
    }
}
