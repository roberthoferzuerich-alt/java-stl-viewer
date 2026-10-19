package com.stlviewer.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Top toolbar panel offering high-visibility buttons for file operations,
 * sample models, wireframe modes, build plate alignment, auto-rotation, and screenshots.
 */
public class ToolbarPanel extends JPanel {

    public static final String[] SAMPLES = {
        "Zahnrad (16 Zähne)",
        "Kalibrier-Würfel (20mm)",
        "Torus (Ring)",
        "Sechskantmutter M12"
    };

    public interface ToolbarListener {
        void onOpenFile();
        void onSampleSelected(String sampleName);
        void onToggleWireframe();
        void onAlignBuildPlate();
        void onToggleAutoRotate();
        void onResetView();
        void onSaveScreenshot();
        void onShowHelp();
    }

    private final ModernComboBox<String> cbSamples;
    private boolean isUpdatingSelection = false;

    public ToolbarPanel(ToolbarListener listener) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(new Color(24, 28, 38));
        setPreferredSize(new Dimension(950, 54));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(48, 56, 74)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Open Button (Vibrant Blue with vector folder icon)
        ModernButton btnOpen = new ModernButton("STL öffnen", new Color(37, 99, 235), 13);
        btnOpen.setIcon(VectorIcons.createFolderIcon(16, Color.WHITE));
        btnOpen.setPreferredSize(new Dimension(130, 34));
        btnOpen.setMaximumSize(new Dimension(130, 34));
        btnOpen.setToolTipText("STL-Datei von Festplatte öffnen (Strg+O)");
        btnOpen.addActionListener(e -> listener.onOpenFile());
        add(btnOpen);

        add(Box.createHorizontalStrut(12));

        // Sample Model Selector Label
        JLabel lblSample = new JLabel("Beispiel:");
        lblSample.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSample.setForeground(new Color(225, 235, 250));
        lblSample.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblSample.setToolTipText("Klicken, um Beispielmodelle anzuzeigen");
        add(lblSample);
        add(Box.createHorizontalStrut(8));

        // Modern Sample Dropdown Box
        cbSamples = new ModernComboBox<>(SAMPLES, new Color(36, 44, 60), 12);
        cbSamples.setPreferredSize(new Dimension(210, 34));
        cbSamples.setMaximumSize(new Dimension(210, 34));
        cbSamples.setToolTipText("Wähle ein vorgefertigtes 3D-Testmodell");

        lblSample.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cbSamples.showPopup();
            }
        });

        cbSamples.addActionListener(e -> {
            if (!isUpdatingSelection) {
                String selected = (String) cbSamples.getSelectedItem();
                if (selected != null) {
                    listener.onSampleSelected(selected);
                }
            }
        });
        add(cbSamples);

        add(Box.createHorizontalStrut(12));

        // Wireframe / Shaded Topology Toggle Button
        ModernButton btnWireframe = new ModernButton("Drahtgitter", new Color(46, 56, 76), 12);
        btnWireframe.setIcon(VectorIcons.createWireframeIcon(15, Color.WHITE));
        btnWireframe.setPreferredSize(new Dimension(115, 34));
        btnWireframe.setMaximumSize(new Dimension(115, 34));
        btnWireframe.setToolTipText("Topologie-Modus umschalten: Schattiert ↔ Gitter ↔ Drahtgitter (Taste W)");
        btnWireframe.addActionListener(e -> listener.onToggleWireframe());
        add(btnWireframe);

        add(Box.createHorizontalStrut(8));

        // Align to Build Plate Button (Z-Min = 0)
        ModernButton btnBed = new ModernButton("Druckbett", new Color(46, 56, 76), 12);
        btnBed.setIcon(VectorIcons.createBuildPlateIcon(15, Color.WHITE));
        btnBed.setPreferredSize(new Dimension(115, 34));
        btnBed.setMaximumSize(new Dimension(115, 34));
        btnBed.setToolTipText("Auf Druckbett ausrichten: Flach auf Z=0 legen & zentrieren (Taste D)");
        btnBed.addActionListener(e -> listener.onAlignBuildPlate());
        add(btnBed);

        add(Box.createHorizontalStrut(8));

        // 60 FPS Auto-Rotation (Turntable)
        ModernButton btnAutoRotate = new ModernButton("Auto-Drehen", new Color(46, 56, 76), 12);
        btnAutoRotate.setIcon(VectorIcons.createTurntableIcon(15, Color.WHITE));
        btnAutoRotate.setPreferredSize(new Dimension(120, 34));
        btnAutoRotate.setMaximumSize(new Dimension(120, 34));
        btnAutoRotate.setToolTipText("Kontinuierliche 360° Drehung ein/aus (Flüssige 60 FPS)");
        btnAutoRotate.addActionListener(e -> listener.onToggleAutoRotate());
        add(btnAutoRotate);

        add(Box.createHorizontalStrut(8));

        // Reset View Button
        ModernButton btnReset = new ModernButton("Zentrieren", new Color(55, 68, 92), 12);
        btnReset.setIcon(VectorIcons.createResetIcon(15, Color.WHITE));
        btnReset.setPreferredSize(new Dimension(110, 34));
        btnReset.setMaximumSize(new Dimension(110, 34));
        btnReset.setToolTipText("Kamera auf Modellmitte zurücksetzen (Taste R)");
        btnReset.addActionListener(e -> listener.onResetView());
        add(btnReset);

        add(Box.createHorizontalGlue());

        // Screenshot Button (Emerald Green)
        ModernButton btnScreenshot = new ModernButton("Screenshot", new Color(16, 130, 80), 12);
        btnScreenshot.setIcon(VectorIcons.createCameraIcon(15, Color.WHITE));
        btnScreenshot.setPreferredSize(new Dimension(120, 34));
        btnScreenshot.setMaximumSize(new Dimension(120, 34));
        btnScreenshot.setToolTipText("Screenshot als PNG Bilddatei speichern (Strg+S)");
        btnScreenshot.addActionListener(e -> listener.onSaveScreenshot());
        add(btnScreenshot);

        add(Box.createHorizontalStrut(8));

        // Help Button
        ModernButton btnHelp = new ModernButton("Hilfe", new Color(55, 68, 92), 12);
        btnHelp.setIcon(VectorIcons.createHelpIcon(14, Color.WHITE));
        btnHelp.setPreferredSize(new Dimension(80, 34));
        btnHelp.setMaximumSize(new Dimension(80, 34));
        btnHelp.setToolTipText("Steuerung & Tastenkürzel anzeigen (F1)");
        btnHelp.addActionListener(e -> listener.onShowHelp());
        add(btnHelp);
    }

    public void setSelectedSampleIndex(int index) {
        if (index >= 0 && index < cbSamples.getItemCount()) {
            isUpdatingSelection = true;
            try {
                cbSamples.setSelectedIndex(index);
            } finally {
                isUpdatingSelection = false;
            }
        }
    }

    public void setSelectedSampleByName(String name) {
        if (name == null) return;
        for (int i = 0; i < cbSamples.getItemCount(); i++) {
            if (cbSamples.getItemAt(i).contains(name)) {
                setSelectedSampleIndex(i);
                break;
            }
        }
    }
}
