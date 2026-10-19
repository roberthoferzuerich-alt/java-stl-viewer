package com.stlviewer.ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern high-contrast button with smooth hover animation, rounded corners,
 * and razor-sharp text readability unaffected by OS theme overrides.
 */
public class ModernButton extends JButton {

    private final Color normalBg;
    private final Color hoverBg;
    private final Color pressedBg;
    private final Color borderColor;

    private boolean isHover = false;
    private boolean isPressed = false;

    public ModernButton(String text, Color baseColor) {
        this(text, baseColor, 13);
    }

    public ModernButton(String text, Color baseColor, int fontSize) {
        super(text);

        this.normalBg = baseColor;
        this.hoverBg = brighten(baseColor, 1.25f);
        this.pressedBg = brighten(baseColor, 0.85f);
        this.borderColor = brighten(baseColor, 1.45f);

        setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHover = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHover = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 8; // Rounded corner radius

        // Background color based on interaction state
        Color bg = normalBg;
        if (!isEnabled()) {
            bg = new Color(50, 55, 65);
        } else if (isPressed) {
            bg = pressedBg;
        } else if (isHover) {
            bg = hoverBg;
        }

        // Fill background with subtle depth gradient
        GradientPaint gp = new GradientPaint(0, 0, brighten(bg, 1.10f), 0, h, bg);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // Subtle top rim highlight for glass/depth feel
        if (isEnabled() && !isPressed) {
            g2.setColor(new Color(255, 255, 255, 28));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc - 1, arc - 1);
        }

        // Border outline
        Color border = isEnabled() ? (isHover ? Color.WHITE : borderColor) : new Color(70, 75, 85);
        g2.setColor(border);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        // Render Crisp Icon and Text
        FontMetrics fm = g2.getFontMetrics(getFont());
        String text = getText();
        javax.swing.Icon icon = getIcon();

        int iconW = (icon != null) ? icon.getIconWidth() : 0;
        int gap = (icon != null && text != null && !text.isEmpty()) ? 8 : 0;
        int textW = (text != null && !text.isEmpty()) ? fm.stringWidth(text) : 0;
        int totalW = iconW + gap + textW;

        int curX = (w - totalW) / 2;
        int pressDy = (isPressed ? 1 : 0);

        if (icon != null) {
            int iconY = (h - icon.getIconHeight()) / 2 + pressDy;
            icon.paintIcon(this, g2, curX, iconY);
            curX += iconW + gap;
        }

        if (text != null && !text.isEmpty()) {
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent() + pressDy;
            if (!isEnabled()) {
                g2.setColor(new Color(130, 140, 155));
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.setFont(getFont());
            g2.drawString(text, curX, textY);
        }

        g2.dispose();
    }

    private static Color brighten(Color c, float factor) {
        int r = Math.min(255, Math.max(0, Math.round(c.getRed() * factor)));
        int g = Math.min(255, Math.max(0, Math.round(c.getGreen() * factor)));
        int b = Math.min(255, Math.max(0, Math.round(c.getBlue() * factor)));
        return new Color(r, g, b, c.getAlpha());
    }
}
