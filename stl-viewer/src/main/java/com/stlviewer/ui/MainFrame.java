package com.stlviewer.ui;

import com.stlviewer.model.Mesh;
import com.stlviewer.parser.SampleModels;
import com.stlviewer.parser.StlExporter;
import com.stlviewer.parser.StlParser;
import com.stlviewer.renderer.RenderMode;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Main application window for the 3D STL Viewer.
 */
public class MainFrame extends JFrame {

    private final ViewportPanel viewport;
    private final SidebarPanel sidebar;
    private final ToolbarPanel toolbar;
    private final JLabel lblStatus;
    private final JLabel lblStats;

    public MainFrame() {
        super("STL Viewer - 3D Modellbetrachter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(1180, 800);
        setLocationRelativeTo(null);

        // Core UI components
        viewport = new ViewportPanel();
        sidebar = new SidebarPanel(viewport);
        viewport.setOnRenderModeChange(sidebar::setRenderModeSelection);

        toolbar = new ToolbarPanel(new ToolbarPanel.ToolbarListener() {
            @Override
            public void onOpenFile() {
                openFileDialog();
            }

            @Override
            public void onSampleSelected(String sampleName) {
                loadSampleModel(sampleName);
            }

            @Override
            public void onToggleWireframe() {
                viewport.cycleRenderMode();
            }

            @Override
            public void onAlignBuildPlate() {
                viewport.alignMeshToBuildPlate();
                sidebar.updateMeshInfo(viewport.getMesh());
            }

            @Override
            public void onToggleAutoRotate() {
                viewport.toggleAutoRotate();
            }

            @Override
            public void onResetView() {
                viewport.getCamera().resetView(viewport.getMesh());
                viewport.repaint();
                setStatus("Ansicht zentriert");
            }

            @Override
            public void onSaveScreenshot() {
                saveScreenshotDialog();
            }

            @Override
            public void onShowHelp() {
                showHelpDialog();
            }
        });

        // Status Bar
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
        statusBar.setBackground(new Color(20, 23, 30));
        statusBar.setPreferredSize(new Dimension(800, 26));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(42, 47, 58)),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        lblStatus = new JLabel("Bereit");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(new Color(225, 235, 250));

        lblStats = new JLabel("Java 17 3D Software Engine");
        lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStats.setForeground(new Color(175, 190, 215));

        statusBar.add(lblStatus);
        statusBar.add(Box.createHorizontalGlue());
        statusBar.add(lblStats);

        // Layout Assembly
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(toolbar, BorderLayout.NORTH);
        centerContainer.add(viewport, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(centerContainer, BorderLayout.CENTER);
        getContentPane().add(sidebar, BorderLayout.EAST);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // Menu Bar (Dark Theme)
        setJMenuBar(createMenuBar());

        // Event Listeners
        viewport.setOnFileDrop(this::loadFileAsync);
        viewport.setOnStatus(this::setStatus);

        // Keyboard Shortcuts
        setupKeyboardShortcuts();

        // Load default model on startup
        loadSampleModel("Zahnrad (16 Zähne)");
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(20, 24, 34));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mb.setOpaque(true);
        mb.setBackground(new Color(20, 24, 34));
        mb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(48, 56, 74)));

        // File Menu
        JMenu menuFile = createStyledMenu("Datei");
        JMenuItem miOpen = createStyledMenuItem("STL öffnen...", "ctrl O");
        miOpen.setIcon(VectorIcons.createFolderIcon(14, new Color(175, 200, 240)));
        miOpen.addActionListener(e -> openFileDialog());

        JMenuItem miExportStl = createStyledMenuItem("Als Binär-STL speichern...", null);
        miExportStl.addActionListener(e -> exportStlDialog());

        JMenuItem miScreenshot = createStyledMenuItem("Screenshot aufnehmen...", "ctrl S");
        miScreenshot.setIcon(VectorIcons.createCameraIcon(14, new Color(175, 200, 240)));
        miScreenshot.addActionListener(e -> saveScreenshotDialog());

        JMenuItem miExit = createStyledMenuItem("Beenden", "alt F4");
        miExit.addActionListener(e -> System.exit(0));

        menuFile.add(miOpen);
        menuFile.add(miExportStl);
        menuFile.addSeparator();
        menuFile.add(miScreenshot);
        menuFile.addSeparator();
        menuFile.add(miExit);

        // View Menu
        JMenu menuView = createStyledMenu("Ansicht");
        JMenuItem miCenter = createStyledMenuItem("Ansicht zentrieren", "R");
        miCenter.setIcon(VectorIcons.createResetIcon(14, new Color(175, 200, 240)));
        miCenter.addActionListener(e -> {
            viewport.getCamera().resetView(viewport.getMesh());
            viewport.repaint();
        });

        JMenuItem miAlignBed = createStyledMenuItem("Auf Druckbett ausrichten (Z=0)", "D");
        miAlignBed.setIcon(VectorIcons.createBuildPlateIcon(14, new Color(175, 200, 240)));
        miAlignBed.addActionListener(e -> {
            viewport.alignMeshToBuildPlate();
            sidebar.updateMeshInfo(viewport.getMesh());
        });

        JMenuItem miToggleWire = createStyledMenuItem("Darstellungsmodus umschalten", "W");
        miToggleWire.setIcon(VectorIcons.createWireframeIcon(14, new Color(175, 200, 240)));
        miToggleWire.addActionListener(e -> viewport.cycleRenderMode());

        JMenuItem miAutoRotate = createStyledMenuItem("Auto-Drehung umschalten", "SPACE");
        miAutoRotate.setIcon(VectorIcons.createTurntableIcon(14, new Color(175, 200, 240)));
        miAutoRotate.addActionListener(e -> viewport.toggleAutoRotate());

        JMenuItem miIso = createStyledMenuItem("Isometrisch", "1");
        miIso.addActionListener(e -> { viewport.getCamera().setPresetView("iso"); viewport.repaint(); });

        JMenuItem miTop = createStyledMenuItem("Ansicht von Oben", "2");
        miTop.addActionListener(e -> { viewport.getCamera().setPresetView("top"); viewport.repaint(); });

        JMenuItem miFront = createStyledMenuItem("Ansicht von Vorne", "3");
        miFront.addActionListener(e -> { viewport.getCamera().setPresetView("front"); viewport.repaint(); });

        JMenuItem miRight = createStyledMenuItem("Ansicht von Rechts", "4");
        miRight.addActionListener(e -> { viewport.getCamera().setPresetView("right"); viewport.repaint(); });

        JMenuItem miToggleGrid = createStyledMenuItem("Bodenraster umschalten", "G");
        miToggleGrid.addActionListener(e -> {
            viewport.getRenderer().setShowGrid(!viewport.getRenderer().isShowGrid());
            viewport.repaint();
        });

        menuView.add(miCenter);
        menuView.add(miAlignBed);
        menuView.add(miToggleWire);
        menuView.add(miAutoRotate);
        menuView.addSeparator();
        menuView.add(miIso);
        menuView.add(miTop);
        menuView.add(miFront);
        menuView.add(miRight);
        menuView.addSeparator();
        menuView.add(miToggleGrid);

        // Samples Menu
        JMenu menuSamples = createStyledMenu("Beispiele");
        JMenuItem s1 = createStyledMenuItem("Zahnrad (16 Zähne)", null);
        s1.setIcon(VectorIcons.createGearIcon(14, new Color(175, 200, 240)));
        s1.addActionListener(e -> loadSampleModel("Zahnrad"));
        JMenuItem s2 = createStyledMenuItem("Kalibrier-Würfel (20mm)", null);
        s2.setIcon(VectorIcons.createCubeIcon(14, new Color(175, 200, 240)));
        s2.addActionListener(e -> loadSampleModel("Würfel"));
        JMenuItem s3 = createStyledMenuItem("Torus (Ring)", null);
        s3.setIcon(VectorIcons.createTorusIcon(14, new Color(175, 200, 240)));
        s3.addActionListener(e -> loadSampleModel("Torus"));
        JMenuItem s4 = createStyledMenuItem("Sechskantmutter M12", null);
        s4.setIcon(VectorIcons.createHexNutIcon(14, new Color(175, 200, 240)));
        s4.addActionListener(e -> loadSampleModel("Mutter"));

        menuSamples.add(s1);
        menuSamples.add(s2);
        menuSamples.add(s3);
        menuSamples.add(s4);

        // Help Menu
        JMenu menuHelp = createStyledMenu("Hilfe");
        JMenuItem miControls = createStyledMenuItem("Steuerung & Bedienung", "F1");
        miControls.setIcon(VectorIcons.createHelpIcon(14, new Color(175, 200, 240)));
        miControls.addActionListener(e -> showHelpDialog());
        JMenuItem miAbout = createStyledMenuItem("Über STL Viewer 3D...", null);
        miAbout.setIcon(VectorIcons.createInfoIcon(14, new Color(175, 200, 240)));
        miAbout.addActionListener(e -> showAboutDialog());

        menuHelp.add(miControls);
        menuHelp.add(miAbout);

        // Dedicated About / "Über" Menu
        JMenu menuAbout = createStyledMenu("Über");
        JMenuItem miAboutDialog = createStyledMenuItem("Über STL Viewer 3D...", null);
        miAboutDialog.setIcon(VectorIcons.createInfoIcon(14, new Color(175, 200, 240)));
        miAboutDialog.addActionListener(e -> showAboutDialog());

        JMenuItem miAuthor = createStyledMenuItem("Entwickler: Robert Hofer, Zürich", null);
        miAuthor.setIcon(VectorIcons.createUserIcon(14, new Color(175, 200, 240)));
        miAuthor.addActionListener(e -> showAboutDialog());

        JMenuItem miAI = createStyledMenuItem("KI-Co-Entwickler: Antigravity", null);
        miAI.setIcon(VectorIcons.createAIIcon(14, new Color(175, 200, 240)));
        miAI.addActionListener(e -> showAboutDialog());

        menuAbout.add(miAboutDialog);
        menuAbout.addSeparator();
        menuAbout.add(miAuthor);
        menuAbout.add(miAI);

        mb.add(menuFile);
        mb.add(menuView);
        mb.add(menuSamples);
        mb.add(menuHelp);
        mb.add(menuAbout);
        return mb;
    }

    private void setupKeyboardShortcuts() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() != KeyEvent.KEY_PRESSED) return false;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_R -> {
                        viewport.getCamera().resetView(viewport.getMesh());
                        viewport.repaint();
                        setStatus("Ansicht zentriert [R]");
                        return true;
                    }
                    case KeyEvent.VK_D -> {
                        viewport.alignMeshToBuildPlate();
                        sidebar.updateMeshInfo(viewport.getMesh());
                        return true;
                    }
                    case KeyEvent.VK_SPACE -> {
                        viewport.toggleAutoRotate();
                        return true;
                    }
                    case KeyEvent.VK_W -> {
                        viewport.cycleRenderMode();
                        return true;
                    }
                    case KeyEvent.VK_G -> {
                        boolean grid = !viewport.getRenderer().isShowGrid();
                        viewport.getRenderer().setShowGrid(grid);
                        viewport.repaint();
                        setStatus("Raster " + (grid ? "aktiviert" : "deaktiviert") + " [G]");
                        return true;
                    }
                    case KeyEvent.VK_A -> {
                        boolean gizmo = !viewport.getRenderer().isShowAxesGizmo();
                        viewport.getRenderer().setShowAxesGizmo(gizmo);
                        viewport.repaint();
                        setStatus("3D-Kompass " + (gizmo ? "aktiviert" : "deaktiviert") + " [A]");
                        return true;
                    }
                    case KeyEvent.VK_B -> {
                        boolean box = !viewport.getRenderer().isShowBoundingBox();
                        viewport.getRenderer().setShowBoundingBox(box);
                        viewport.repaint();
                        setStatus("Bounding Box " + (box ? "aktiviert" : "deaktiviert") + " [B]");
                        return true;
                    }
                    case KeyEvent.VK_1 -> {
                        viewport.getCamera().setPresetView("iso");
                        viewport.repaint();
                        setStatus("Isometrische Ansicht [1]");
                        return true;
                    }
                    case KeyEvent.VK_2 -> {
                        viewport.getCamera().setPresetView("top");
                        viewport.repaint();
                        setStatus("Ansicht von Oben [2]");
                        return true;
                    }
                    case KeyEvent.VK_3 -> {
                        viewport.getCamera().setPresetView("front");
                        viewport.repaint();
                        setStatus("Ansicht von Vorne [3]");
                        return true;
                    }
                    case KeyEvent.VK_4 -> {
                        viewport.getCamera().setPresetView("right");
                        viewport.repaint();
                        setStatus("Ansicht von Rechts [4]");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void openFileDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("STL 3D-Modell öffnen");
        chooser.setFileFilter(new FileNameExtensionFilter("STL 3D-Modelle (*.stl)", "stl"));
        chooser.setCurrentDirectory(new File("."));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadFileAsync(chooser.getSelectedFile());
        }
    }

    public void loadFileAsync(File file) {
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(this, "Datei existiert nicht: " + file, "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setStatus("Lade Datei: " + file.getName() + "...");

        new SwingWorker<Mesh, Void>() {
            @Override
            protected Mesh doInBackground() throws Exception {
                return StlParser.parse(file, (pct, msg) -> {
                    SwingUtilities.invokeLater(() -> setStatus(String.format("Lade: %s (%d%%)", msg, (int) (pct * 100))));
                });
            }

            @Override
            protected void done() {
                try {
                    Mesh mesh = get();
                    setMesh(mesh);
                    setStatus("Erfolgreich geladen: " + file.getName());
                    setTitle("STL Viewer - " + file.getName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    setStatus("Fehler beim Laden");
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Fehler beim Parsen der STL Datei:\n" + ex.getMessage(),
                        "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public void loadSampleModel(String sampleName) {
        Mesh mesh;
        int sampleIdx = 0;
        if (sampleName.contains("Würfel") || sampleName.contains("Kalibrier")) {
            mesh = SampleModels.createCalibrationCube();
            sampleIdx = 1;
        } else if (sampleName.contains("Torus") || sampleName.contains("Ring")) {
            mesh = SampleModels.createTorus();
            sampleIdx = 2;
        } else if (sampleName.contains("Mutter") || sampleName.contains("Sechskant")) {
            mesh = SampleModels.createHexNut();
            sampleIdx = 3;
        } else {
            mesh = SampleModels.createSpurGear();
            sampleIdx = 0;
        }

        setMesh(mesh);
        if (toolbar != null) {
            toolbar.setSelectedSampleIndex(sampleIdx);
        }
        setStatus("Beispielmodell geladen: " + mesh.getName());
    }

    private void setMesh(Mesh mesh) {
        viewport.setMesh(mesh);
        sidebar.updateMeshInfo(mesh);
        if (mesh != null) {
            lblStats.setText(String.format(Locale.GERMAN, "%s | %,d Dreiecke | %,d Vertices",
                mesh.getFormat(), mesh.getTriangleCount(), mesh.distinctVertexCount));
        }
    }

    private void saveScreenshotDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Screenshot speichern");
        chooser.setSelectedFile(new File("stl_screenshot.png"));
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Bild (*.png)", "png"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".png")) {
                target = new File(target.getParentFile(), target.getName() + ".png");
            }
            try {
                viewport.exportScreenshot(target);
                setStatus("Screenshot erfolgreich gespeichert: " + target.getName());
                JOptionPane.showMessageDialog(this, "Screenshot gespeichert unter:\n" + target.getAbsolutePath(),
                    "Erfolg", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Fehler beim Speichern:\n" + e.getMessage(),
                    "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportStlDialog() {
        Mesh mesh = viewport.getMesh();
        if (mesh == null || mesh.getTriangleCount() == 0) {
            JOptionPane.showMessageDialog(this, "Kein Modell geladen zum Exportieren.",
                "Hinweis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Als Binär-STL Datei speichern");
        chooser.setSelectedFile(new File(mesh.getName().replaceAll("[^a-zA-Z0-9_-]", "_") + "_export.stl"));
        chooser.setFileFilter(new FileNameExtensionFilter("Binäre STL-Datei (*.stl)", "stl"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".stl")) {
                target = new File(target.getParentFile(), target.getName() + ".stl");
            }
            try {
                StlExporter.exportBinary(mesh, target);
                setStatus("STL erfolgreich exportiert: " + target.getName());
                JOptionPane.showMessageDialog(this, "Modell erfolgreich exportiert:\n" + target.getAbsolutePath(),
                    "Export erfolgreich", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Fehler beim Exportieren:\n" + e.getMessage(),
                    "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void setStatus(String text) {
        lblStatus.setText(text);
    }

    private void showHelpDialog() {
        String msg = """
            Bedienung des 3D STL Viewers:

            Maus-Steuerung:
            • Linke Maustaste gedrückt halten + Ziehen: 3D Drehung um Modellzentrum
            • Rechte Maustaste / Mittlere Maustaste + Ziehen: Modell verschieben (Pan)
            • Mausrad: Hinein- / Herauszoomen
            • Doppelklick: Ansicht an Modellgröße anpassen

            Tastatur-Kürzel & Schnellfunktionen:
            • [R]: Ansicht zentrieren
            • [D]: Auf Druckbett ausrichten (Z-Min = 0.0 mm & XY zentrieren)
            • [W]: Darstellungsmodus umschalten (Schattiert ↔ Gitter ↔ Drahtgitter)
            • [Leertaste]: Kontinuierliche 360° Auto-Drehung ein/aus (flüssige 60 FPS)
            • [G]: Bodenraster ein-/ausblenden
            • [A]: 3D-Achsen-Kompass ein-/ausblenden
            • [B]: Bounding-Box Rahmen umschalten
            • [1] - [4]: Isometrisch, Oben, Vorne, Rechts
            • [Strg + O]: STL Datei öffnen
            • [Strg + S]: Screenshot speichern

            Drag & Drop:
            • Ziehe beliebige .stl Dateien direkt per Drag & Drop in das Fenster!
            """;
        JOptionPane.showMessageDialog(this, msg, "Hilfe & Steuerung", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        AboutDialog.showAbout(this);
    }

    private JMenu createStyledMenu(String title) {
        JMenu menu = new JMenu(title) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                if (isSelected() || isArmed() || getPopupMenu().isVisible()) {
                    g2.setColor(new Color(42, 52, 72));
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 6, 6);
                    g2.setColor(new Color(59, 130, 246));
                    g2.drawRoundRect(2, 2, w - 4, h - 4, 6, 6);
                }

                FontMetrics fm = g2.getFontMetrics(getFont());
                int textX = (w - fm.stringWidth(getText())) / 2;
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();

                g2.setFont(getFont());
                g2.setColor(new Color(235, 242, 255));
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        };

        menu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        menu.setForeground(new Color(235, 242, 255));
        menu.setOpaque(false);
        menu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menu.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JPopupMenu popup = menu.getPopupMenu();
        popup.setBackground(new Color(26, 31, 42));
        popup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 66, 88), 1),
            BorderFactory.createEmptyBorder(4, 2, 4, 2)
        ));

        return menu;
    }

    private JMenuItem createStyledMenuItem(String title, String accelerator) {
        JMenuItem mi = new JMenuItem(title) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width + 24, Math.max(d.height, 28));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                boolean armed = isArmed() || isSelected();
                if (armed) {
                    g2.setColor(new Color(37, 99, 235));
                    g2.fillRoundRect(2, 1, w - 4, h - 2, 4, 4);
                } else {
                    g2.setColor(new Color(26, 31, 42));
                    g2.fillRect(0, 0, w, h);
                }

                // Paint Icon
                int curX = 12;
                Icon icon = getIcon();
                if (icon != null) {
                    int iconY = (h - icon.getIconHeight()) / 2;
                    icon.paintIcon(this, g2, curX, iconY);
                    curX += icon.getIconWidth() + 10;
                }

                // Paint Text
                FontMetrics fm = g2.getFontMetrics(getFont());
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), curX, textY);

                // Paint Accelerator if present
                KeyStroke ks = getAccelerator();
                if (ks != null) {
                    String accText = KeyEvent.getKeyModifiersText(ks.getModifiers());
                    if (!accText.isEmpty()) accText += "+";
                    accText += KeyEvent.getKeyText(ks.getKeyCode());
                    int accW = fm.stringWidth(accText);
                    g2.setColor(armed ? new Color(225, 238, 255) : new Color(155, 170, 195));
                    g2.drawString(accText, w - accW - 14, textY);
                }

                g2.dispose();
            }
        };

        mi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mi.setPreferredSize(new Dimension(250, 30));
        mi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mi.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        if (accelerator != null) {
            mi.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }
        return mi;
    }
}
