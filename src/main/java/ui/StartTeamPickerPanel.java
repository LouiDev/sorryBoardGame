package ui;

import models.StartTeamPicker;
import models.Team;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class StartTeamPickerPanel extends JPanel {

    private static final int CARD_W = 160;
    private static final int CARD_H = 200;
    private static final int CARD_ARC = 18;
    private static final int CARD_GAP = 24;

    private final StartTeamPicker startRoll;

    public StartTeamPickerPanel(StartTeamPicker startRoll) {
        this.startRoll = startRoll;
        setBackground(new Color(245, 240, 220));
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

        drawTitle(g2, w);

        Team[] teams = startRoll.teams();
        int selected = startRoll.selectedIndex();

        int totalW = teams.length * CARD_W + (teams.length - 1) * CARD_GAP;
        int startX = (w - totalW) / 2;
        int cardY = h / 2 - CARD_H / 2 - 20;

        for (int i = 0; i < teams.length; i++) {
            int cx = startX + i * (CARD_W + CARD_GAP);
            drawCard(g2, cx, cardY, teams[i], i == selected);
        }

        drawHint(g2, w, h);
    }

    private void drawTitle(Graphics2D g2, int w) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.setColor(new Color(60, 60, 60));
        String title = "Wer fängt an?";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, 80);
    }

    private void drawCard(Graphics2D g2, int x, int y, Team team, boolean selected) {
        Color teamColor = team.color();
        Color bgColor = selected
                ? Renderer.fieldFill(teamColor, 230)
                : new Color(200, 200, 200, 120);
        Color borderColor = selected ? teamColor.darker() : new Color(160, 160, 160);
        float strokeWidth = selected ? 3.5f : 1.5f;

        g2.setColor(bgColor);
        g2.fillRoundRect(x, y, CARD_W, CARD_H, CARD_ARC, CARD_ARC);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(strokeWidth));
        g2.drawRoundRect(x, y, CARD_W, CARD_H, CARD_ARC, CARD_ARC);

        int cx = x + CARD_W / 2;

        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(selected ? new Color(40, 40, 40) : new Color(150, 150, 150));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(team.name(), cx - fm.stringWidth(team.name()) / 2, y + 38);

        if (selected) {
            int ar = 7;
            int ax = cx;
            int ay = y - 18;
            g2.setColor(teamColor.darker());
            g2.fillPolygon(
                    new int[]{ax, ax - ar, ax + ar},
                    new int[]{ay + ar * 2, ay, ay},
                    3
            );
        }
    }

    private void drawHint(Graphics2D g2, int w, int h) {
        String hint = "\u2190 \u2192 Ausw\u00e4hlen   SPACE: Best\u00e4tigen";
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(new Color(80, 80, 80));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, (w - fm.stringWidth(hint)) / 2, h / 2 + CARD_H / 2 + 50);
    }
}
