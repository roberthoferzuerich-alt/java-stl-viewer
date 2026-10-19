package com.stlviewer;

import com.stlviewer.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.File;

/**
 * Entry point for the STL 3D Viewer application.
 */
public class Main {

    public static void main(String[] args) {
        // High-DPI and font rendering optimization
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.uiScale.enabled", "true");

        try {
            // Use system Look and Feel for native integration
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);

            // If a file path was passed via command line, load it
            if (args != null && args.length > 0) {
                File file = new File(args[0]);
                if (file.exists() && file.isFile()) {
                    frame.loadFileAsync(file);
                }
            }
        });
    }
}
