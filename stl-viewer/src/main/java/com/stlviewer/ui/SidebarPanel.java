package com.stlviewer.ui;

import com.stlviewer.model.Mesh;
import com.stlviewer.renderer.Camera;
import com.stlviewer.renderer.ColorTheme;
import com.stlviewer.renderer.RenderMode;
import com.stlviewer.renderer.Software3DRenderer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;

/**
 * Sidebar panel displaying model statistics, render settings, and camera controls
 * with large, high-contrast, crystal-clear typography.
 */
public class SidebarPanel extends JPanel {

    private final ViewportPanel viewport;

    // Info Labels
    private final JLabel lblName = createValLabel("-");
    private final JLabel lblFormat = createValLabel("-");
    private final JLabel lblTriangles = createValLabel("-");
    private final JLabel lblVertices = createValLabel("-");
    private final JLabel lblDimensions = createValLabel("-");
    private final JLabel lblVolume = createValLabel("-");
    private final JLabel lblSurface = createValLabel("-");

    // Controls
    private ModernComboBox<RenderMode> cbRenderMode;
    private ModernComboBox<ColorTheme> cbColorTheme;
    private JCheckBox chkGrid;
    private JCheckBox chkGizmo;
    private JCheckBox chkCulling;
    private JCheckBox chkBoundingBox;
    private JCheckBox chkHeadlight;
    private JCheckBox chkOrtho;
    private boolean isUpdatingRenderMode = false;

    public void setRenderModeSelection(RenderMode mode) {
        if (cbRenderMode != null && mode != null) {
            isUpdatingRenderMode = true;
            try {
                cbRenderMode.setSelectedItem(mode);
            } finally {
                isUpdatingRenderMode = false;
            }
        }
    }

    public SidebarPanel(ViewportPanel viewport) {
        this.viewport = viewport;

        setLayout(new BorderLayout());
        setBackground(new Color(24, 28, 38));
        setPreferredSize(new Dimension(320, 600));
        setMinimumSize(new Dimension(300, 400));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(48, 55, 72)));

        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(24, 28, 38));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // 1. Model Statistics Card
        JPanel c1 = createModelStatsCard();
        c1.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(c1);
        content.add(Box.createVerticalStrut(14));

        // 2. Render & Material Options Card
        JPanel c2 = createRenderOptionsCard();
        c2.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(c2);
        content.add(Box.createVerticalStrut(14));

        // 3. Camera & View Angle Card
        JPanel c3 = createCameraCard();
        c3.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(c3);
        content.add(Box.createVerticalStrut(14));

        // 4. Navigation Shortcuts Card
        JPanel c4 = createShortcutsCard();
        c4.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(c4);
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setUI(new ModernComboBox.ModernScrollBarUI());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(new Color(24, 28, 38));
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createModelStatsCard() {
        JPanel card = createCard("Modell-Statistiken");
        JPanel grid = new JPanel(new GridLayout(7, 2, 6, 8));
        grid.setOpaque(false);

        grid.add(createKeyLabel("Name:"));
        grid.add(lblName);
        grid.add(createKeyLabel("Format:"));
        grid.add(lblFormat);
        grid.add(createKeyLabel("Dreiecke:"));
        grid.add(lblTriangles);
        grid.add(createKeyLabel("Eckpunkte:"));
        grid.add(lblVertices);
        grid.add(createKeyLabel("Maße (X×Y×Z):"));
        grid.add(lblDimensions);
        grid.add(createKeyLabel("Volumen:"));
        grid.add(lblVolume);
        grid.add(createKeyLabel("Oberfläche:"));
        grid.add(lblSurface);

        card.add(grid);
        return card;
    }

    private JPanel createRenderOptionsCard() {
        JPanel card = createCard("Darstellung & Material");
        JPanel layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        layout.setOpaque(false);

        Software3DRenderer renderer = viewport.getRenderer();
        Camera camera = viewport.getCamera();

        // Render Mode
        JLabel lblMode = createKeyLabel("Render-Modus:");
        lblMode.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbRenderMode = new ModernComboBox<>(RenderMode.values(), new Color(36, 44, 60), 12);
        cbRenderMode.setPreferredSize(new Dimension(270, 32));
        cbRenderMode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cbRenderMode.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbRenderMode.setSelectedItem(renderer.getRenderMode());
        cbRenderMode.addActionListener(e -> {
            if (!isUpdatingRenderMode) {
                viewport.setRenderMode((RenderMode) cbRenderMode.getSelectedItem());
            }
        });

        // Color Theme
        JLabel lblTheme = createKeyLabel("Farbe / Material:");
        lblTheme.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbColorTheme = new ModernComboBox<>(ColorTheme.values(), new Color(36, 44, 60), 12);
        cbColorTheme.setPreferredSize(new Dimension(270, 32));
        cbColorTheme.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cbColorTheme.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbColorTheme.setSelectedItem(renderer.getColorTheme());
        cbColorTheme.addActionListener(e -> {
            renderer.setColorTheme((ColorTheme) cbColorTheme.getSelectedItem());
            viewport.repaint();
        });

        // Checkboxes
        chkGrid = createCheckBox("Bodenraster anzeigen", renderer.isShowGrid(), e -> {
            renderer.setShowGrid(chkGrid.isSelected());
            viewport.repaint();
        });

        chkGizmo = createCheckBox("3D-Achsen-Kompass", renderer.isShowAxesGizmo(), e -> {
            renderer.setShowAxesGizmo(chkGizmo.isSelected());
            viewport.repaint();
        });

        chkCulling = createCheckBox("Rückseiten verbergen", renderer.isBackfaceCulling(), e -> {
            renderer.setBackfaceCulling(chkCulling.isSelected());
            viewport.repaint();
        });

        chkBoundingBox = createCheckBox("Bounding-Box Rahmen", renderer.isShowBoundingBox(), e -> {
            renderer.setShowBoundingBox(chkBoundingBox.isSelected());
            viewport.repaint();
        });

        chkHeadlight = createCheckBox("Kamera-Licht (Headlight)", renderer.isHeadlightMode(), e -> {
            renderer.setHeadlightMode(chkHeadlight.isSelected());
            viewport.repaint();
        });

        chkOrtho = createCheckBox("Orthografische Ansicht", camera.isOrthographic, e -> {
            camera.isOrthographic = chkOrtho.isSelected();
            viewport.repaint();
        });

        layout.add(lblMode);
        layout.add(Box.createVerticalStrut(4));
        layout.add(cbRenderMode);
        layout.add(Box.createVerticalStrut(10));
        layout.add(lblTheme);
        layout.add(Box.createVerticalStrut(4));
        layout.add(cbColorTheme);
        layout.add(Box.createVerticalStrut(12));
        layout.add(chkGrid);
        layout.add(chkGizmo);
        layout.add(chkCulling);
        layout.add(chkBoundingBox);
        layout.add(chkHeadlight);
        layout.add(chkOrtho);

        card.add(layout);
        return card;
    }

    private JPanel createCameraCard() {
        JPanel card = createCard("Blickwinkel & Kamera");
        JPanel grid = new JPanel(new GridLayout(4, 2, 8, 8));
        grid.setOpaque(false);

        grid.add(createPresetModernButton("Isometrisch", "iso"));
        grid.add(createPresetModernButton("Oben (Z+)", "top"));
        grid.add(createPresetModernButton("Vorne (Y-)", "front"));
        grid.add(createPresetModernButton("Rechts (X+)", "right"));
        grid.add(createPresetModernButton("Unten (Z-)", "bottom"));

        ModernButton btnReset = new ModernButton("Zentrieren", new Color(42, 85, 155), 12);
        btnReset.setIcon(VectorIcons.createResetIcon(14, Color.WHITE));
        btnReset.setPreferredSize(new Dimension(110, 32));
        btnReset.addActionListener(e -> {
            viewport.getCamera().resetView(viewport.getMesh());
            viewport.repaint();
        });
        grid.add(btnReset);

        ModernButton btnBed = new ModernButton("Auf Druckbett", new Color(42, 85, 155), 12);
        btnBed.setIcon(VectorIcons.createBuildPlateIcon(14, Color.WHITE));
        btnBed.setPreferredSize(new Dimension(110, 32));
        btnBed.addActionListener(e -> viewport.alignMeshToBuildPlate());
        grid.add(btnBed);

        ModernButton btnAuto = new ModernButton("Auto-Drehen", new Color(42, 85, 155), 12);
        btnAuto.setIcon(VectorIcons.createTurntableIcon(14, Color.WHITE));
        btnAuto.setPreferredSize(new Dimension(110, 32));
        btnAuto.addActionListener(e -> viewport.toggleAutoRotate());
        grid.add(btnAuto);

        card.add(grid);
        return card;
    }

    private JPanel createShortcutsCard() {
        JPanel card = createCard("Tastatur & Maus");
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        box.add(createHintLabel("• Links-Ziehen: 3D Drehen"));
        box.add(createHintLabel("• Rechts-Ziehen: Verschieben"));
        box.add(createHintLabel("• Mausrad: Zoom"));
        box.add(createHintLabel("• Doppelklick: Ansicht einpassen"));
        box.add(createHintLabel("• [R]: Kamera zurücksetzen"));
        box.add(createHintLabel("• [W]: Drahtgitter ein/aus"));
        box.add(createHintLabel("• [G]: Raster ein/aus"));

        card.add(box);
        return card;
    }

    public void updateMeshInfo(Mesh mesh) {
        if (mesh == null || mesh.getTriangleCount() == 0) {
            lblName.setText("-");
            lblFormat.setText("-");
            lblTriangles.setText("-");
            lblVertices.setText("-");
            lblDimensions.setText("-");
            lblVolume.setText("-");
            lblSurface.setText("-");
            return;
        }

        lblName.setText(mesh.getName());
        lblFormat.setText(mesh.getFormat());
        lblTriangles.setText(String.format(Locale.GERMAN, "%,d", mesh.getTriangleCount()));
        lblVertices.setText(String.format(Locale.GERMAN, "%,d", mesh.distinctVertexCount));

        lblDimensions.setText(String.format(Locale.GERMAN, "%.1f × %.1f × %.1f mm",
            mesh.size.x, mesh.size.y, mesh.size.z));

        float volumeCm3 = mesh.volumeMm3 / 1000.0f;
        lblVolume.setText(String.format(Locale.GERMAN, "%.2f cm³", volumeCm3));

        float areaCm2 = mesh.surfaceAreaMm2 / 100.0f;
        lblSurface.setText(String.format(Locale.GERMAN, "%.2f cm²", areaCm2));
    }

    private JPanel createCard(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(32, 38, 50));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 64, 84), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(new Color(147, 197, 253));
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }

    private JLabel createKeyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(203, 213, 225));
        return l;
    }

    private static JLabel createValLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JLabel createHintLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(203, 213, 225));
        l.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        return l;
    }

    private JCheckBox createCheckBox(String text, boolean initial, java.awt.event.ActionListener al) {
        JCheckBox cb = new JCheckBox(text, initial);
        cb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cb.setForeground(new Color(241, 245, 249));
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        cb.addActionListener(al);
        return cb;
    }

    private ModernButton createPresetModernButton(String text, String presetKey) {
        ModernButton btn = new ModernButton(text, new Color(52, 63, 85), 12);
        btn.setPreferredSize(new Dimension(110, 32));
        btn.addActionListener(e -> {
            viewport.getCamera().setPresetView(presetKey);
            viewport.repaint();
        });
        return btn;
    }

}
