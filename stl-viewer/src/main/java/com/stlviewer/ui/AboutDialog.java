package com.stlviewer.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.net.URI;

/**
 * Modern dark-themed About dialog displaying project information,
 * the author Robert Hofer (Zürich), and Antigravity (Google DeepMind).
 */
public class AboutDialog extends JDialog {

    private static final Color BG_DARK = new Color(20, 24, 34);
    private static final Color CARD_BG = new Color(28, 34, 48);
    private static final Color CARD_BORDER = new Color(42, 52, 72);
    private static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);

    public AboutDialog(JFrame parent) {
        super(parent, "Über STL Viewer 3D", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        initUI();
        pack();
        setLocationRelativeTo(parent);
    }

    public static void showAbout(JFrame parent) {
        AboutDialog dialog = new AboutDialog(parent);
        dialog.setVisible(true);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(22, 28, 20, 28));
        root.setPreferredSize(new Dimension(680, 710));

        // --- Header Section ---
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(CENTER_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        JLabel lblLogo = new JLabel(VectorIcons.createCubeIcon(32, new Color(96, 165, 250)));
        JLabel lblTitle = new JLabel("STL Viewer 3D");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(TEXT_PRIMARY);

        titleRow.add(lblLogo);
        titleRow.add(lblTitle);

        JLabel lblSub = new JLabel("High-Performance 3D CAD & Mesh Inspector");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(175, 200, 240));
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblVer = new JLabel("Version 1.1.0 • Reines Java 17 (Software 3D-Rasterizer)");
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVer.setForeground(TEXT_SECONDARY);
        lblVer.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(titleRow);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSub);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblVer);
        headerPanel.add(Box.createVerticalStrut(14));

        root.add(headerPanel, BorderLayout.NORTH);

        // --- Center Content Section ---
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);

        // Card 1: Author & Contact
        contentPanel.add(createAuthorCard(), gbc);

        // Card 2: AI Pair Programmer (Antigravity)
        gbc.gridy = 1;
        contentPanel.add(createAICard(), gbc);

        // Card 3: Project Highlights
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(createFeaturesCard(), gbc);

        root.add(contentPanel, BorderLayout.CENTER);

        // --- Footer Section ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 12));
        footerPanel.setOpaque(false);

        ModernButton btnClose = new ModernButton("Schließen", new Color(37, 99, 235), 13);
        btnClose.setPreferredSize(new Dimension(120, 36));
        btnClose.addActionListener(e -> dispose());

        footerPanel.add(btnClose);
        root.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(root);

        // Escape & Enter key handler to close
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        getRootPane().setDefaultButton(btnClose);
    }

    private JPanel createAuthorCard() {
        JPanel card = createCardPanel();

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));
        titleRow.add(new JLabel(VectorIcons.createUserIcon(18, new Color(96, 165, 250))), BorderLayout.WEST);
        JLabel lblHeader = new JLabel("Entwickler & Autor");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setForeground(new Color(147, 197, 253));
        titleRow.add(lblHeader, BorderLayout.CENTER);

        JLabel lblName = new JLabel("Robert Hofer");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(TEXT_PRIMARY);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLocation = new JLabel("Zürich, Schweiz");
        lblLocation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLocation.setForeground(TEXT_SECONDARY);
        lblLocation.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Email line with Copy & Mail actions
        JPanel emailRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        emailRow.setOpaque(false);
        emailRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));

        JLabel lblEmail = new JLabel("robert.hofer.zuerich@bluewin.ch");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEmail.setForeground(new Color(224, 231, 255));
        lblEmail.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JButton btnCopy = createSmallButton("Kopieren");
        btnCopy.setToolTipText("E-Mail in die Zwischenablage kopieren");
        btnCopy.addActionListener(e -> {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection("robert.hofer.zuerich@bluewin.ch"), null);
                btnCopy.setText("Kopiert!");
                new javax.swing.Timer(1800, evt -> btnCopy.setText("Kopieren")).start();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "robert.hofer.zuerich@bluewin.ch", "E-Mail", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnMail = createSmallButton("E-Mail");
        btnMail.setToolTipText("E-Mail-Programm öffnen");
        btnMail.addActionListener(e -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                    Desktop.getDesktop().mail(new URI("mailto:robert.hofer.zuerich@bluewin.ch?subject=STL%20Viewer%203D"));
                } else {
                    Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection("robert.hofer.zuerich@bluewin.ch"), null);
                    JOptionPane.showMessageDialog(this, "E-Mail-Adresse kopiert:\nrobert.hofer.zuerich@bluewin.ch", "E-Mail", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection("robert.hofer.zuerich@bluewin.ch"), null);
                JOptionPane.showMessageDialog(this, "E-Mail-Adresse kopiert:\nrobert.hofer.zuerich@bluewin.ch", "E-Mail", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        emailRow.add(lblEmail);
        emailRow.add(btnCopy);
        emailRow.add(btnMail);

        card.add(titleRow);
        card.add(Box.createVerticalStrut(6));
        card.add(lblName);
        card.add(lblLocation);
        card.add(Box.createVerticalStrut(4));
        card.add(emailRow);

        return card;
    }

    private JPanel createAICard() {
        JPanel card = createCardPanel();

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));
        titleRow.add(new JLabel(VectorIcons.createAIIcon(18, new Color(192, 132, 252))), BorderLayout.WEST);
        JLabel lblHeader = new JLabel("KI-Pair-Programmer & Co-Entwickler");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setForeground(new Color(216, 180, 254));
        titleRow.add(lblHeader, BorderLayout.CENTER);

        JLabel lblName = new JLabel("Antigravity");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(TEXT_PRIMARY);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTeam = new JLabel("Google DeepMind – Advanced Agentic Coding");
        lblTeam.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTeam.setForeground(TEXT_SECONDARY);
        lblTeam.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRole = new JLabel("<html><body style='color: #94a3b8; font-size: 11px; line-height: 1.45;'>"
            + "Architektur, Multi-Threaded Z-Buffer Software-Rasterizer, Gouraud-Shading,<br>"
            + "Drahtgitter-Topologieprüfung, Druckbett-Ausrichtung und UI/UX Dark Theme."
            + "</body></html>");
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleRow);
        card.add(Box.createVerticalStrut(6));
        card.add(lblName);
        card.add(lblTeam);
        card.add(Box.createVerticalStrut(4));
        card.add(lblRole);

        return card;
    }

    private JPanel createFeaturesCard() {
        JPanel card = createCardPanel();

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));
        titleRow.add(new JLabel(VectorIcons.createInfoIcon(18, new Color(52, 211, 153))), BorderLayout.WEST);
        JLabel lblHeader = new JLabel("Highlights & Technologie");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setForeground(new Color(110, 231, 183));
        titleRow.add(lblHeader, BorderLayout.CENTER);

        JLabel lblFeatures = new JLabel("<html><body style='color: #cbd5e1; font-size: 11px; line-height: 1.45;'>"
            + "• <b>Reines Java 17:</b> 100 % portabel, keine externen Treiber oder OpenGL-DLLs nötig<br>"
            + "• <b>Render-on-Demand & 60 FPS:</b> Ruhezustand im Stillstand, flüssige 60 FPS Drehung<br>"
            + "• <b>CAD & 3D-Druck Tools:</b> Z-Min = 0 Druckbett-Ausrichtung, Bounding-Box, Volumen & Fläche<br>"
            + "• <b>Topologie-Inspektion:</b> Drahtgitter (Wireframe) & Schattiert + Gitter per Taste [W]"
            + "</body></html>");
        lblFeatures.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleRow);
        card.add(Box.createVerticalStrut(6));
        card.add(lblFeatures);

        return card;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE, pref.height);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        return card;
    }

    private ModernButton createSmallButton(String text) {
        ModernButton btn = new ModernButton(text, new Color(42, 54, 76), 11);
        btn.setPreferredSize(new Dimension(text.length() > 6 ? 85 : 75, 26));
        return btn;
    }
}
