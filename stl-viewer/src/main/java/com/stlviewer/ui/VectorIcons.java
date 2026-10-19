package com.stlviewer.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * High-DPI antialiased vector icons for the STL Viewer UI.
 * Eliminates Unicode missing glyph issues (tofu rectangles) on all operating systems.
 */
public final class VectorIcons {

    private VectorIcons() {}

    public static Icon createFolderIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Folder tab
            Path2D tab = new Path2D.Float();
            tab.moveTo(2, 4);
            tab.lineTo(6, 4);
            tab.lineTo(8, 6);
            tab.lineTo(w - 3, 6);
            tab.lineTo(w - 3, h - 3);
            tab.lineTo(2, h - 3);
            tab.closePath();
            g2.draw(tab);

            // Folder front flap
            Path2D front = new Path2D.Float();
            front.moveTo(2, 7);
            front.lineTo(w - 2, 7);
            front.lineTo(w - 3, h - 3);
            front.lineTo(3, h - 3);
            front.closePath();
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
            g2.fill(front);
            g2.setColor(color);
            g2.draw(front);
        });
    }

    public static Icon createGearIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            float cx = w / 2.0f;
            float cy = h / 2.0f;
            float rOuter = w * 0.42f;
            float rInner = w * 0.28f;
            float rHole = w * 0.15f;

            // Gear teeth
            int numTeeth = 8;
            Path2D path = new Path2D.Float();
            for (int i = 0; i < numTeeth * 2; i++) {
                double angle = i * Math.PI / numTeeth;
                float r = (i % 2 == 0) ? rOuter : rInner;
                float x = cx + (float) (Math.cos(angle) * r);
                float y = cy + (float) (Math.sin(angle) * r);
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            path.closePath();

            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(path);

            // Center hole
            g2.drawOval((int) (cx - rHole), (int) (cy - rHole), (int) (rHole * 2), (int) (rHole * 2));
        });
    }

    public static Icon createCubeIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            float cx = w / 2.0f;
            float cy = h / 2.0f;
            float r = w * 0.40f;
            float dy = r * 0.55f;

            // Top vertex
            float p0x = cx, p0y = cy - r;
            // Top right
            float p1x = cx + r * 0.86f, p1y = cy - r * 0.4f;
            // Bottom right
            float p2x = cx + r * 0.86f, p2y = cy + dy;
            // Bottom vertex
            float p3x = cx, p3y = cy + r;
            // Bottom left
            float p4x = cx - r * 0.86f, p4y = cy + dy;
            // Top left
            float p5x = cx - r * 0.86f, p5y = cy - r * 0.4f;
            // Center
            float pcx = cx, pcy = cy + dy - r * 0.4f;

            // Outer hexagon
            Path2D hex = new Path2D.Float();
            hex.moveTo(p0x, p0y);
            hex.lineTo(p1x, p1y);
            hex.lineTo(p2x, p2y);
            hex.lineTo(p3x, p3y);
            hex.lineTo(p4x, p4y);
            hex.lineTo(p5x, p5y);
            hex.closePath();
            g2.draw(hex);

            // Internal edges
            g2.drawLine((int) p0x, (int) p0y, (int) pcx, (int) pcy);
            g2.drawLine((int) p2x, (int) p2y, (int) pcx, (int) pcy);
            g2.drawLine((int) p4x, (int) p4y, (int) pcx, (int) pcy);
        });
    }

    public static Icon createTorusIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Outer ellipse
            g2.drawOval(2, 4, w - 4, h - 8);
            // Inner ellipse
            int innerW = (int) (w * 0.45f);
            int innerH = (int) (h * 0.35f);
            g2.drawOval((w - innerW) / 2, (h - innerH) / 2, innerW, innerH);
        });
    }

    public static Icon createHexNutIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            float cx = w / 2.0f;
            float cy = h / 2.0f;
            float r = w * 0.42f;

            Path2D hex = new Path2D.Float();
            for (int i = 0; i < 6; i++) {
                double angle = i * Math.PI / 3.0 - Math.PI / 6.0;
                float x = cx + (float) (Math.cos(angle) * r);
                float y = cy + (float) (Math.sin(angle) * r);
                if (i == 0) hex.moveTo(x, y);
                else hex.lineTo(x, y);
            }
            hex.closePath();
            g2.draw(hex);

            // Center hole
            float rHole = w * 0.20f;
            g2.drawOval((int) (cx - rHole), (int) (cy - rHole), (int) (rHole * 2), (int) (rHole * 2));
        });
    }

    public static Icon createResetIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = w / 2;
            int cy = h / 2;
            int r = (int) (w * 0.36f);

            // 270 degree circular arc
            g2.drawArc(cx - r, cy - r, r * 2, r * 2, 45, 270);

            // Arrow head at start of arc
            int ax = cx + (int) (Math.cos(Math.toRadians(45)) * r);
            int ay = cy - (int) (Math.sin(Math.toRadians(45)) * r);
            g2.drawLine(ax, ay, ax + 4, ay - 1);
            g2.drawLine(ax, ay, ax + 1, ay + 4);
        });
    }

    public static Icon createCameraIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Camera body
            g2.drawRoundRect(2, 5, w - 4, h - 8, 3, 3);
            // Flash/top bump
            g2.drawRect(w / 2 - 3, 3, 6, 2);
            // Lens circle
            int r = 3;
            g2.drawOval(w / 2 - r, h / 2 - r + 1, r * 2, r * 2);
        });
    }

    public static Icon createHelpIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Outer circle
            g2.drawOval(2, 2, w - 4, h - 4);

            // Question mark
            Path2D q = new Path2D.Float();
            float cx = w / 2.0f;
            q.moveTo(cx - 2.5f, 5.5f);
            q.curveTo(cx - 2.5f, 4.0f, cx + 2.5f, 4.0f, cx + 2.5f, 6.0f);
            q.curveTo(cx + 2.5f, 7.5f, cx, 8.0f, cx, 9.5f);
            g2.draw(q);
            g2.fillOval((int) cx - 1, h - 5, 2, 2);
        });
    }

    public static Icon createBuildPlateIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Flat build plate surface at the bottom
            g2.drawRoundRect(2, h - 4, w - 4, 3, 2, 2);
            g2.drawLine(4, h - 2, w - 4, h - 2);

            // Downward alignment arrow landing on the bed
            int cx = w / 2;
            g2.drawLine(cx, 2, cx, h - 6);
            g2.drawLine(cx, h - 6, cx - 4, h - 10);
            g2.drawLine(cx, h - 6, cx + 4, h - 10);
        });
    }

    public static Icon createWireframeIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Wireframe 3D pyramid / mesh triangle with vertices
            int x1 = w / 2, y1 = 3;
            int x2 = 3, y2 = h - 3;
            int x3 = w - 3, y3 = h - 3;
            int x4 = w / 2, y4 = h / 2 + 1;

            g2.drawLine(x1, y1, x2, y2);
            g2.drawLine(x2, y2, x3, y3);
            g2.drawLine(x3, y3, x1, y1);
            g2.drawLine(x1, y1, x4, y4);
            g2.drawLine(x2, y2, x4, y4);
            g2.drawLine(x3, y3, x4, y4);

            // Small vertex dots
            g2.fillOval(x1 - 1, y1 - 1, 3, 3);
            g2.fillOval(x2 - 1, y2 - 1, 3, 3);
            g2.fillOval(x3 - 1, y3 - 1, 3, 3);
            g2.fillOval(x4 - 1, y4 - 1, 3, 3);
        });
    }

    public static Icon createTurntableIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Elliptical 3D rotation ring
            int cx = w / 2;
            int cy = h / 2;
            g2.drawArc(2, cy - 4, w - 4, 8, 20, 310);

            // Arrow on orbit
            g2.drawLine(w - 3, cy, w - 1, cy - 3);
            g2.drawLine(w - 3, cy, w - 6, cy - 1);

            // Center play/pivot dot
            g2.fillOval(cx - 2, cy - 2, 4, 4);
        });
    }
    public static Icon createInfoIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(1, 1, w - 3, h - 3);

            int cx = w / 2;
            int cy = h / 2;
            // Dot of 'i'
            g2.fillOval(cx - 1, cy - 4, 2, 2);
            // Stem of 'i'
            g2.drawLine(cx, cy - 1, cx, cy + 3);
        });
    }

    public static Icon createUserIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = w / 2;
            int rHead = Math.max(3, (int) (w * 0.22f));
            // Head
            g2.drawOval(cx - rHead, 2, rHead * 2, rHead * 2);

            // Torso / Shoulders
            Path2D shoulders = new Path2D.Float();
            shoulders.moveTo(2, h - 2);
            shoulders.curveTo(3, h * 0.65f, w - 3, h * 0.65f, w - 2, h - 2);
            g2.draw(shoulders);
        });
    }

    public static Icon createAIIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            float cx = w / 2.0f;
            float cy = h / 2.0f;
            float r = w * 0.42f;
            float inner = r * 0.28f;

            // 4-point star / sparkle
            Path2D star = new Path2D.Float();
            star.moveTo(cx, cy - r);
            star.quadTo(cx, cy, cx + r, cy);
            star.quadTo(cx, cy, cx, cy + r);
            star.quadTo(cx, cy, cx - r, cy);
            star.quadTo(cx, cy, cx, cy - r);
            star.closePath();

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
            g2.fill(star);
            g2.setColor(color);
            g2.draw(star);
        });
    }

    @FunctionalInterface
    public interface IconPainter {
        void paint(Graphics2D g2, int width, int height);
    }

    private static class VectorIcon implements Icon {
        private final int width;
        private final int height;
        private final IconPainter painter;

        public VectorIcon(int width, int height, IconPainter painter) {
            this.width = width;
            this.height = height;
            this.painter = painter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            painter.paint(g2, width, height);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
}
