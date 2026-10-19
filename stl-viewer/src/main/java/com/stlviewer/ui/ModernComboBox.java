package com.stlviewer.ui;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern dark-themed ComboBox with rounded corners, custom antialiased vector chevron,
 * high-contrast crystal-clear typography, and custom styled dark popup menu.
 * Completely immune to Windows Look & Feel native overrides that cause invisible text.
 */
public class ModernComboBox<E> extends JComboBox<E> {

    private final Color baseBg;
    private final int fontSize;

    public ModernComboBox(E[] items) {
        this(items, new Color(36, 43, 58), 12);
    }

    public ModernComboBox(E[] items, int fontSize) {
        this(items, new Color(36, 43, 58), fontSize);
    }

    public ModernComboBox(E[] items, Color baseBg, int fontSize) {
        super(items);
        this.baseBg = baseBg;
        this.fontSize = fontSize;
        initStyle();
    }

    public ModernComboBox(ComboBoxModel<E> model) {
        this(model, new Color(36, 43, 58), 12);
    }

    public ModernComboBox(ComboBoxModel<E> model, Color baseBg, int fontSize) {
        super(model);
        this.baseBg = baseBg;
        this.fontSize = fontSize;
        initStyle();
    }

    public ModernComboBox() {
        super();
        this.baseBg = new Color(36, 43, 58);
        this.fontSize = 12;
        initStyle();
    }

    private void initStyle() {
        setOpaque(false);
        setFocusable(false);
        setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        setForeground(Color.WHITE);
        setBackground(baseBg);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setRenderer(new ModernComboRenderer<>());
        setUI(new ModernComboBoxUI(baseBg));
    }

    @Override
    public void updateUI() {
        if (baseBg != null) {
            setUI(new ModernComboBoxUI(baseBg));
            setRenderer(new ModernComboRenderer<>());
        } else {
            super.updateUI();
        }
    }

    public static class ModernComboBoxUI extends BasicComboBoxUI {

        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final Color borderColor;
        private final Color focusBorderColor;
        private final Color arrowColor;
        private final int arc;

        private boolean isHovering = false;
        private MouseAdapter mouseHoverListener;

        public ModernComboBoxUI(Color baseBg) {
            this.normalBg = baseBg;
            this.hoverBg = brighten(baseBg, 1.25f);
            this.pressedBg = brighten(baseBg, 0.85f);
            this.borderColor = brighten(baseBg, 1.55f);
            this.focusBorderColor = new Color(59, 130, 246);
            this.arrowColor = new Color(175, 195, 225);
            this.arc = 8;
        }

        @Override
        protected void installDefaults() {
            super.installDefaults();
            comboBox.setOpaque(false);
            comboBox.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 4));
        }

        @Override
        protected void installListeners() {
            super.installListeners();
            mouseHoverListener = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (comboBox.isEnabled()) {
                        isHovering = true;
                        comboBox.repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovering = false;
                    comboBox.repaint();
                }
            };
            comboBox.addMouseListener(mouseHoverListener);
            if (arrowButton != null) {
                arrowButton.addMouseListener(mouseHoverListener);
            }
        }

        @Override
        protected void uninstallListeners() {
            if (mouseHoverListener != null) {
                comboBox.removeMouseListener(mouseHoverListener);
                if (arrowButton != null) {
                    arrowButton.removeMouseListener(mouseHoverListener);
                }
            }
            super.uninstallListeners();
        }

        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                    int w = getWidth();
                    int h = getHeight();

                    boolean isOpen = isPopupVisible(comboBox);
                    boolean hover = isHovering || isOpen;

                    Color arrowCol = !comboBox.isEnabled()
                        ? new Color(90, 100, 115)
                        : (hover ? Color.WHITE : arrowColor);

                    g2.setColor(arrowCol);
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    int cx = w / 2 - 2;
                    int cy = h / 2;

                    if (isOpen) {
                        g2.drawLine(cx - 4, cy + 2, cx, cy - 2);
                        g2.drawLine(cx, cy - 2, cx + 4, cy + 2);
                    } else {
                        g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                        g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    }

                    g2.dispose();
                }
            };

            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = c.getWidth();
            int h = c.getHeight();

            boolean isOpen = isPopupVisible(comboBox);
            Color bg;
            Color border;

            if (!comboBox.isEnabled()) {
                bg = new Color(30, 35, 45);
                border = new Color(50, 58, 72);
            } else if (isOpen) {
                bg = pressedBg;
                border = focusBorderColor;
            } else if (isHovering) {
                bg = hoverBg;
                border = focusBorderColor;
            } else {
                bg = normalBg;
                border = borderColor;
            }

            // Fill subtle gradient background
            GradientPaint gp = new GradientPaint(0, 0, brighten(bg, 1.10f), 0, h, bg);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Subtle top inner highlight
            if (comboBox.isEnabled()) {
                g2.setColor(new Color(255, 255, 255, 25));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc - 1, arc - 1);
            }

            // Outer border
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.dispose();

            // Paint the current item text cleanly
            hasFocus = false;
            Rectangle r = rectangleForCurrentValue();
            paintCurrentValue(g, r, false);
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            ListCellRenderer<Object> renderer = (ListCellRenderer<Object>) comboBox.getRenderer();
            Component c = renderer.getListCellRendererComponent(
                listBox,
                comboBox.getSelectedItem(),
                -1,
                false,
                false
            );

            c.setFont(comboBox.getFont());
            if (comboBox.isEnabled()) {
                c.setForeground(Color.WHITE);
            } else {
                c.setForeground(new Color(130, 140, 155));
            }

            if (c instanceof JComponent jc) {
                jc.setOpaque(false);
            }

            int x = bounds.x + 6;
            int y = bounds.y;
            int w = bounds.width - 6;
            int h = bounds.height;

            currentValuePane.paintComponent(g, c, comboBox, x, y, w, h, false);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane scroller = super.createScroller();
                    scroller.setBorder(BorderFactory.createEmptyBorder());
                    scroller.setOpaque(false);
                    scroller.getViewport().setOpaque(true);
                    scroller.getViewport().setBackground(new Color(26, 31, 42));

                    JScrollBar vsb = scroller.getVerticalScrollBar();
                    vsb.setPreferredSize(new Dimension(8, 0));
                    vsb.setUI(new ModernScrollBarUI());
                    vsb.setUnitIncrement(16);
                    return scroller;
                }

                @Override
                protected void configurePopup() {
                    super.configurePopup();
                    setOpaque(true);
                    setBackground(new Color(26, 31, 42));
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(58, 70, 94), 1),
                        BorderFactory.createEmptyBorder(4, 2, 4, 2)
                    ));
                }
            };

            popup.getList().setBackground(new Color(26, 31, 42));
            popup.getList().setSelectionBackground(new Color(37, 99, 235));
            popup.getList().setSelectionForeground(Color.WHITE);
            popup.getList().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return popup;
        }

        private static Color brighten(Color c, float factor) {
            int r = Math.min(255, Math.max(0, Math.round(c.getRed() * factor)));
            int g = Math.min(255, Math.max(0, Math.round(c.getGreen() * factor)));
            int b = Math.min(255, Math.max(0, Math.round(c.getBlue() * factor)));
            return new Color(r, g, b, c.getAlpha());
        }
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(68, 82, 110);
            this.trackColor = new Color(26, 31, 42);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            b.setVisible(false);
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = isThumbRollover();
            g2.setColor(hover ? new Color(95, 115, 155) : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1, thumbBounds.width - 2, thumbBounds.height - 2, 6, 6);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    public static class ModernComboRenderer<T> extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            l.setIconTextGap(8);

            String text = l.getText();
            if (text != null) {
                if (text.contains("Zahnrad") || text.contains("Gear")) {
                    l.setIcon(VectorIcons.createGearIcon(16, isSelected ? Color.WHITE : new Color(175, 200, 240)));
                } else if (text.contains("Würfel") || text.contains("Cube") || text.contains("Kalibrier")) {
                    l.setIcon(VectorIcons.createCubeIcon(16, isSelected ? Color.WHITE : new Color(175, 200, 240)));
                } else if (text.contains("Torus") || text.contains("Ring")) {
                    l.setIcon(VectorIcons.createTorusIcon(16, isSelected ? Color.WHITE : new Color(175, 200, 240)));
                } else if (text.contains("Mutter") || text.contains("Nut") || text.contains("Sechskant")) {
                    l.setIcon(VectorIcons.createHexNutIcon(16, isSelected ? Color.WHITE : new Color(175, 200, 240)));
                }
            }

            if (index == -1) {
                l.setOpaque(false);
                l.setForeground(Color.WHITE);
            } else {
                l.setOpaque(true);
                if (isSelected) {
                    l.setBackground(new Color(37, 99, 235));
                    l.setForeground(Color.WHITE);
                } else {
                    l.setBackground(new Color(26, 31, 42));
                    l.setForeground(new Color(230, 238, 250));
                }
            }
            return l;
        }
    }
}
