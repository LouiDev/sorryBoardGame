package ui;

import models.Board;
import models.GameFigure;
import models.GameState;
import models.Team;
import linkedlist.ContentNode;
import linkedlist.EndNode;
import linkedlist.GarageNode;
import linkedlist.GarageRootNode;
import linkedlist.Node;
import linkedlist.TeamRootNode;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Zeichnet das Mensch-ärgere-dich-nicht-Spielfeld.
 *
 * Layout-Algorithmus (Polygon-Pfad):
 *   - Für n Teams wird ein regelmäßiges n-Eck berechnet.
 *   - Jede Seite entspricht einer Team-Strecke (10 Felder).
 *   - Die Lauffelder entlang jeder Seite werden durch lineare Interpolation berechnet.
 *   - Die 4 Heimfelder (GarageNodes) jedes Teams zeigen senkrecht zur Kante nach innen.
 */
public class BoardPanel extends JPanel {

    private static final int FIELD_RADIUS = 18;
    private static final int FIGURE_RADIUS = 8;
    private static final int HOME_FIELD_RADIUS = 16;
    private static final int HIGHLIGHT_EXTRA = 6;

    // Anzahl Felder zwischen zwei TeamRootNodes (exkl. des nächsten TeamRootNode selbst):
    //   8 ContentNodes + 1 GarageRootNode = 9 Zwischenfelder, der nächste TeamRootNode ist Ecke
    // Auf jeder Polygon-Kante liegen daher 10 gleichmäßig verteilte Felder (0..9),
    // wobei Index 0 = TeamRootNode dieser Kante, Index 9 = GarageRootNode.
    // Der TeamRootNode der nächsten Kante liegt auf dem nächsten Eckpunkt des Polygons.
    private static final int FIELDS_PER_SIDE = 10;

    private final Board board;
    private float highlightPhase = 0f;

    public BoardPanel(Board board) {
        this.board = board;
        setBackground(new Color(245, 240, 220));

        // Timer für pulsierende Highlight-Animation (~60 fps)
        Timer animTimer = new Timer(16, e -> {
            if (board.state() == GameState.WAITING_FOR_FIGURE_SELECTION) {
                highlightPhase += 0.05f;
                if (highlightPhase > 2 * Math.PI) highlightPhase -= (float) (2 * Math.PI);
            } else {
                highlightPhase = 0f;
            }
            repaint();
        });
        animTimer.start();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 700);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Renderer.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;

        Team[] teams = board.teams();
        int n = teams.length;

        // Polygon-Radius: so groß wie möglich mit Abstand zum Rand
        int margin = FIELD_RADIUS * 4;
        int polygonRadius = Math.min(cx, cy) - margin;

        // Eckpunkte des regelmäßigen n-Ecks berechnen.
        // Rotation um -π/2 damit Team 0 oben beginnt; bei gerader Teamanzahl
        // wird zusätzlich um eine halbe Segmentbreite gedreht, damit die Seiten
        // horizontal/vertikal ausgerichtet sind.
        double startAngle = -Math.PI / 2;
        if (n % 2 == 0) {
            startAngle += Math.PI / n;
        }

        double[] cornerX = new double[n];
        double[] cornerY = new double[n];
        for (int i = 0; i < n; i++) {
            double angle = startAngle + 2 * Math.PI * i / n;
            cornerX[i] = cx + polygonRadius * Math.cos(angle);
            cornerY[i] = cy + polygonRadius * Math.sin(angle);
        }

        // Node-Liste traversieren und Felder den Polygon-Kanten zuordnen.
        // Die Linked-List hat für jedes Team genau die Reihenfolge:
        //   TeamRootNode(i) → ContentNode×8 → GarageRootNode(i+1) → TeamRootNode(i+1) → ...
        // Wir traversieren und zeichnen jedes Feld an der berechneten Position.

        Node current = board.root();
        int teamIndex = 0;
        int fieldIndex = 0; // 0 = TeamRootNode, 1-8 = Content, 9 = GarageRootNode

        // Maximale Iterationen zum Schutz vor Endlosschleife (Sentinel-Guard)
        int maxNodes = n * (FIELDS_PER_SIDE + 1) + 10;
        int iterations = 0;

        GameFigure selected = board.selectedFigure();

        while (!(current instanceof EndNode) && iterations < maxNodes) {
            int nextTeamIndex = (teamIndex + 1) % n;

            double t = (double) fieldIndex / FIELDS_PER_SIDE;
            int px = (int) Math.round(cornerX[teamIndex] + t * (cornerX[nextTeamIndex] - cornerX[teamIndex]));
            int py = (int) Math.round(cornerY[teamIndex] + t * (cornerY[nextTeamIndex] - cornerY[teamIndex]));

            if (current instanceof GarageRootNode grn) {
                drawGarageRoot(g2, px, py, grn, cx, cy, cornerX, cornerY, teamIndex, n, selected);
                teamIndex = nextTeamIndex;
                fieldIndex = 0;
                current = current.next();
            } else if (current instanceof TeamRootNode trn) {
                drawTeamRoot(g2, px, py, trn, selected);
                fieldIndex = 1;
                current = current.next();
            } else if (current instanceof ContentNode cn) {
                drawContent(g2, px, py, cn, selected);
                fieldIndex++;
                current = current.next();
            } else {
                current = current.next();
            }

            iterations++;

            // Abbruch, wenn wir wieder am Startknoten angelangt sind (zirkuläre Liste)
            if (current == board.root()) break;
        }

        // Heimfelder (home-Figuren) in den Ecken zeichnen
        drawAllHomes(g2, teams, n, cornerX, cornerY, cx, cy, polygonRadius, startAngle, selected);

        // Zentrales Info-Panel im Mittelpunkt des Spielfelds
        drawCenterInfo(g2, cx, cy);
    }

    // -------------------------------------------------------------------------
    // Feld-Zeichenmethoden
    // -------------------------------------------------------------------------

    private void drawTeamRoot(Graphics2D g2, int px, int py, TeamRootNode trn, GameFigure selected) {
        Color c = trn.team().color();
        Renderer.drawField(g2, px, py, FIELD_RADIUS, c, c.darker(), 2.5f);

        GameFigure fig = trn.content();
        if (fig != null) {
            Renderer.drawFigure(g2, px, py, FIGURE_RADIUS, fig.team().color());
            if (fig == selected) {
                drawHighlight(g2, px, py);
            }
        }
    }

    private void drawContent(Graphics2D g2, int px, int py, ContentNode cn, GameFigure selected) {
        Color fill = new Color(235, 235, 235);
        Renderer.drawField(g2, px, py, FIELD_RADIUS, fill, Color.GRAY, 1.5f);

        GameFigure fig = cn.content();
        if (fig != null) {
            Renderer.drawFigure(g2, px, py, FIGURE_RADIUS, fig.team().color());
            if (fig == selected) {
                drawHighlight(g2, px, py);
            }
        }
    }

    private void drawGarageRoot(Graphics2D g2, int px, int py, GarageRootNode grn,
                                int cx, int cy,
                                double[] cornerX, double[] cornerY,
                                int teamIndex, int n, GameFigure selected) {
        Color c = grn.team().color();
        // GarageRootNode selbst: größeres Feld mit Teamfarbe, gestrichelter Rahmen
        Renderer.drawField(g2, px, py, FIELD_RADIUS, Renderer.fieldFill(c, 180), c.darker(), 2f);

        GameFigure fig = grn.content();
        if (fig != null) {
            Renderer.drawFigure(g2, px, py, FIGURE_RADIUS, fig.team().color());
            if (fig == selected) {
                drawHighlight(g2, px, py);
            }
        }

        // Die 4 Zielfelder (GarageNodes) senkrecht zur Kante nach innen zeichnen
        GarageNode[] garageNodes = grn.garage();

        // Senkrechtenvektor zur aktuellen Kante (teamIndex → nextTeamIndex), normiert nach innen
        int nextTeamIndex = (teamIndex + 1) % n;
        double edgeDx = cornerX[nextTeamIndex] - cornerX[teamIndex];
        double edgeDy = cornerY[nextTeamIndex] - cornerY[teamIndex];
        double edgeLen = Math.sqrt(edgeDx * edgeDx + edgeDy * edgeDy);

        // Senkrechte nach innen: rotiere Kantenvektor um +90° und normiere
        double perpX = -edgeDy / edgeLen;
        double perpY = edgeDx / edgeLen;

        // Prüfen ob die Senkrechte wirklich zum Mittelpunkt zeigt; wenn nicht, umdrehen
        double midPx = px + perpX;
        double midPy = py + perpY;
        if (dist(midPx, midPy, cx, cy) > dist(px, py, cx, cy)) {
            perpX = -perpX;
            perpY = -perpY;
        }

        int spacing = FIELD_RADIUS * 2 + 4;
        for (int i = 0; i < garageNodes.length; i++) {
            int gx = (int) Math.round(px + perpX * spacing * (i + 1));
            int gy = (int) Math.round(py + perpY * spacing * (i + 1));
            Renderer.drawField(g2, gx, gy, HOME_FIELD_RADIUS,
                    Renderer.fieldFill(c, 220), c.darker(), 1.5f);

            GameFigure garageFig = garageNodes[i].content();
            if (garageFig != null) {
                Renderer.drawFigure(g2, gx, gy, FIGURE_RADIUS, garageFig.team().color());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Heimfelder-Zeichnen (Figuren, die noch nicht auf dem Brett sind)
    // -------------------------------------------------------------------------

    private void drawAllHomes(Graphics2D g2, Team[] teams, int n,
                              double[] cornerX, double[] cornerY,
                              int cx, int cy, int polygonRadius, double startAngle,
                              GameFigure selected) {
        for (int i = 0; i < n; i++) {
            Team team = teams[i];
            Color c = team.color();

            // Heimbereich liegt außerhalb der Polygon-Ecke von Team i
            // Richtung: von Mittelpunkt zur Ecke (nach außen)
            double angle = startAngle + 2 * Math.PI * i / n;
            double dirX = Math.cos(angle);
            double dirY = Math.sin(angle);

            // Außermittelpunkt des Heimbereichs
            double homeCx = cx + (polygonRadius + FIELD_RADIUS * 5.5) * dirX;
            double homeCy = cy + (polygonRadius + FIELD_RADIUS * 5.5) * dirY;

            // Hintergrundbereich für Heimfeld
            int boxR = FIELD_RADIUS * 3;
            g2.setColor(Renderer.fieldFill(c, 60));
            g2.fillOval((int) homeCx - boxR, (int) homeCy - boxR, boxR * 2, boxR * 2);
            g2.setColor(c.darker());
            g2.drawOval((int) homeCx - boxR, (int) homeCy - boxR, boxR * 2, boxR * 2);

            // 4 Heimfelder in 2×2-Anordnung
            GameFigure[] home = team.home();
            int spacing = FIELD_RADIUS + 3;
            int[][] offsets = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
            for (int j = 0; j < home.length; j++) {
                int hx = (int) Math.round(homeCx + offsets[j][0] * spacing);
                int hy = (int) Math.round(homeCy + offsets[j][1] * spacing);
                Renderer.drawField(g2, hx, hy, HOME_FIELD_RADIUS,
                        Renderer.fieldFill(c, 200), c.darker(), 1.5f);
                // Figur nur zeichnen wenn sie noch im Heim ist
                if (home[j] != null) {
                    Renderer.drawFigure(g2, hx, hy, FIGURE_RADIUS, c);
                    if (home[j] == selected) {
                        drawHighlight(g2, hx, hy);
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Zentrale Info-Anzeige
    // -------------------------------------------------------------------------

    /**
     * Zeichnet im Zentrum des Spielfelds:
     *  - Name/Farbe des aktiven Teams
     *  - Gewürfelte Zahl (falls bereits gewürfelt)
     *  - Anweisung je nach State
     */
    private void drawCenterInfo(Graphics2D g2, int cx, int cy) {
        Team team = board.currentTeam();
        if (team == null) return;

        GameState state = board.state();
        int roll = board.lastRoll();
        Color teamColor = team.color();

        // Zeile 1: "Team " + <farbname> + " ist am Zug!" — zusammen zentriert
        Font prefixFont = new Font("SansSerif", Font.BOLD, 14);
        Font nameFont = new Font("SansSerif", Font.BOLD, 18);

        g2.setFont(prefixFont);
        FontMetrics fmPrefix = g2.getFontMetrics();
        String prefix = "Team ";
        String suffix = " ist am Zug!";
        int prefixW = fmPrefix.stringWidth(prefix);
        int suffixW = fmPrefix.stringWidth(suffix);

        g2.setFont(nameFont);
        FontMetrics fmName = g2.getFontMetrics();
        String colorName = team.name();
        int nameW = fmName.stringWidth(colorName);

        int totalW = prefixW + nameW + suffixW;
        int startX = cx - totalW / 2;
        int line1Y = cy - 30;

        g2.setFont(prefixFont);
        g2.setColor(new Color(60, 60, 60));
        g2.drawString(prefix, startX, line1Y);

        g2.setFont(nameFont);
        g2.setColor(teamColor);
        g2.drawString(colorName, startX + prefixW, line1Y);

        g2.setFont(prefixFont);
        g2.setColor(new Color(60, 60, 60));
        g2.drawString(suffix, startX + prefixW + nameW, line1Y);

        // Zeile 2: Augenzahl — groß, in Teamfarbe, zentriert
        if (roll > 0) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 42));
            g2.setColor(teamColor);
            drawCentered(g2, String.valueOf(roll), cx, cy + 18);
        }

        // Zeile 3: Hinweis je nach State — grau, klein, zentriert
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(80, 80, 80));
        String hint;
        if (state == GameState.WAITING_FOR_ROLL) {
            hint = "SPACE: Würfeln";
        } else if (state == GameState.WAITING_FOR_SKIP_CONFIRM) {
            hint = "Kein Zug möglich — SPACE: Weiter";
        } else {
            hint = "← → Wählen   SPACE: OK";
        }
        drawCentered(g2, hint, cx, cy + 60);
    }

    /** Zeichnet einen String horizontal zentriert um den Punkt (cx, baselineY). */
    private void drawCentered(Graphics2D g2, String text, int cx, int baselineY) {
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        g2.drawString(text, cx - textW / 2, baselineY);
    }

    // -------------------------------------------------------------------------
    // Highlight-Ring für ausgewählte Figur
    // -------------------------------------------------------------------------

    private void drawHighlight(Graphics2D g2, int px, int py) {
        float pulse = (float) Math.sin(highlightPhase);   // -1 .. 1
        int r = FIGURE_RADIUS + HIGHLIGHT_EXTRA + (int) (pulse * 3);
        int alpha = 180 + (int) (pulse * 60);             // 120 .. 240

        // Schatten-Ring (dunkel, außen)
        g2.setColor(new Color(0, 0, 0, Math.max(0, Math.min(255, alpha / 2))));
        g2.setStroke(new BasicStroke(5f));
        g2.drawOval(px - r - 2, py - r - 2, (r + 2) * 2, (r + 2) * 2);

        // Weiß-Ring (innen)
        g2.setColor(new Color(255, 255, 255, Math.max(0, Math.min(255, alpha))));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(px - r, py - r, r * 2, r * 2);
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static double dist(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
