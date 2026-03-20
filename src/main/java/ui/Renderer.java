package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Renderer {

    private Renderer() {}

    /** Aktiviert Antialiasing und sauberes Rendering auf dem Graphics2D-Kontext. */
    public static void enableAntialiasing(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Zeichnet ein Spielfeld-Kreis.
     *
     * @param g         Graphics2D-Kontext
     * @param cx        Mittelpunkt X
     * @param cy        Mittelpunkt Y
     * @param radius    Radius des Kreises
     * @param fill      Füllfarbe
     * @param border    Rahmenfarbe
     * @param strokeWidth Rahmendicke
     */
    public static void drawField(Graphics2D g, int cx, int cy, int radius,
                                 Color fill, Color border, float strokeWidth) {
        int x = cx - radius;
        int y = cy - radius;
        int d = radius * 2;

        g.setColor(fill);
        g.fillOval(x, y, d, d);

        g.setColor(border);
        g.setStroke(new BasicStroke(strokeWidth));
        g.drawOval(x, y, d, d);
    }

    /**
     * Zeichnet eine Spielfigur als kleineren, ausgefüllten Kreis mit weißem Rand.
     *
     * @param g      Graphics2D-Kontext
     * @param cx     Mittelpunkt X
     * @param cy     Mittelpunkt Y
     * @param radius Radius der Figur
     * @param color  Farbe des Teams
     */
    public static void drawFigure(Graphics2D g, int cx, int cy, int radius, Color color) {
        int x = cx - radius;
        int y = cy - radius;
        int d = radius * 2;

        g.setColor(color.darker());
        g.fillOval(x, y, d, d);

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x, y, d, d);
    }

    /**
     * Berechnet die Farbe für den Hintergrund eines Feldes abhängig vom Typ.
     *
     * @param teamColor  Teamfarbe (für TeamRootNode / GarageRootNode / GarageNode), null für neutrale Felder
     * @param alpha      Alpha-Wert 0–255 für transparente Varianten
     */
    public static Color fieldFill(Color teamColor, int alpha) {
        if (teamColor == null) {
            return new Color(220, 220, 220, alpha);
        }
        return new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), alpha);
    }
}
