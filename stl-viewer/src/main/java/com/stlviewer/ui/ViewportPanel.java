package com.stlviewer.ui;

import com.stlviewer.model.Mesh;
import com.stlviewer.renderer.Camera;
import com.stlviewer.renderer.RenderMode;
import com.stlviewer.renderer.Software3DRenderer;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interactive 3D Viewport Panel handling multi-threaded rendering, mouse orbit, panning, zoom,
 * auto-rotation (turntable), build plate alignment, and drag-and-drop file imports.
 */
public class ViewportPanel extends JPanel {

    private final Camera camera = new Camera();
    private final Software3DRenderer renderer = new Software3DRenderer(800, 600);
    private Mesh currentMesh;

    private Point lastMousePos;
    private int mouseButton = 0;

    // Performance & FPS Tracking
    private int frameCount = 0;
    private long lastFpsTime = System.currentTimeMillis();
    private float currentFps = 60.0f;
    private float lastRenderTimeMs = 1.5f;
    private long lastInteractionTime = 0;

    // Auto-Rotate (Turntable)
    private boolean autoRotating = false;
    private Timer autoRotateTimer;

    private Consumer<File> fileDropListener;
    private Consumer<String> statusListener;
    private Consumer<RenderMode> renderModeChangeListener;

    public ViewportPanel() {
        setBackground(new Color(18, 20, 26));
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        setupEventHandlers();
        setupDragAndDrop();
        setupAutoRotateTimer();
    }

    private void setupAutoRotateTimer() {
        autoRotateTimer = new Timer(16, e -> {
            if (autoRotating && currentMesh != null) {
                camera.rotate(0.008f, 0.0f);
                lastInteractionTime = System.currentTimeMillis();
                repaint();
            }
        });
    }

    public boolean toggleAutoRotate() {
        autoRotating = !autoRotating;
        if (autoRotating) {
            autoRotateTimer.start();
            notifyStatus("Auto-Drehung aktiv (60 FPS)");
        } else {
            autoRotateTimer.stop();
            notifyStatus("Auto-Drehung pausiert");
            repaint();
        }
        return autoRotating;
    }

    public boolean isAutoRotating() {
        return autoRotating;
    }

    public void alignMeshToBuildPlate() {
        if (currentMesh != null) {
            currentMesh.alignToBuildPlate();
            camera.resetView(currentMesh);
            lastInteractionTime = System.currentTimeMillis();
            repaint();
            notifyStatus("Modell auf Druckbett ausgerichtet (Z-Min = 0.0 mm)");
        }
    }

    public RenderMode cycleRenderMode() {
        RenderMode current = renderer.getRenderMode();
        RenderMode next = switch (current) {
            case FLAT_SHADED -> RenderMode.SOLID_WIREFRAME;
            case SOLID_WIREFRAME -> RenderMode.WIREFRAME;
            case WIREFRAME -> RenderMode.SMOOTH_SHADED;
            case SMOOTH_SHADED -> RenderMode.HEIGHT_MAP;
            default -> RenderMode.FLAT_SHADED;
        };
        setRenderMode(next);
        return next;
    }

    public void setRenderMode(RenderMode mode) {
        renderer.setRenderMode(mode);
        lastInteractionTime = System.currentTimeMillis();
        repaint();
        if (renderModeChangeListener != null) {
            renderModeChangeListener.accept(mode);
        }
        notifyStatus("Render-Modus: " + mode.getDisplayName());
    }

    public void setOnRenderModeChange(Consumer<RenderMode> listener) {
        this.renderModeChangeListener = listener;
    }

    private void setupEventHandlers() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = getWidth();
                int h = getHeight();
                if (w > 0 && h > 0) {
                    renderer.resize(w, h);
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                lastMousePos = e.getPoint();
                mouseButton = e.getButton();
                lastInteractionTime = System.currentTimeMillis();

                if (SwingUtilities.isLeftMouseButton(e)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
                mouseButton = 0;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Double-click resets view to fit mesh
                    camera.resetView(currentMesh);
                    lastInteractionTime = System.currentTimeMillis();
                    repaint();
                    notifyStatus("Ansicht zentriert");
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos == null) {
                    lastMousePos = e.getPoint();
                    return;
                }

                int dx = e.getX() - lastMousePos.x;
                int dy = e.getY() - lastMousePos.y;
                lastMousePos = e.getPoint();
                lastInteractionTime = System.currentTimeMillis();

                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Orbit rotation
                    float yawSpeed = 0.008f;
                    float pitchSpeed = 0.008f;
                    camera.rotate(dx * yawSpeed, -dy * pitchSpeed);
                    repaint();
                } else if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                    // Pan
                    camera.pan(dx, dy);
                    repaint();
                }
            }
        });

        addMouseWheelListener((MouseWheelEvent e) -> {
            int notches = e.getWheelRotation();
            float zoomFactor = (notches < 0) ? 0.90f : 1.11f;
            camera.zoom(zoomFactor);
            lastInteractionTime = System.currentTimeMillis();
            repaint();
        });
    }

    private void setupDragAndDrop() {
        new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        if (files != null && !files.isEmpty()) {
                            File file = files.get(0);
                            if (fileDropListener != null) {
                                fileDropListener.accept(file);
                            }
                        }
                    }
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    dtde.dropComplete(false);
                }
            }
        });
    }

    public void setMesh(Mesh mesh) {
        this.currentMesh = mesh;
        camera.resetView(mesh);
        lastInteractionTime = System.currentTimeMillis();
        repaint();
    }

    public Mesh getMesh() {
        return currentMesh;
    }

    public Camera getCamera() {
        return camera;
    }

    public Software3DRenderer getRenderer() {
        return renderer;
    }

    public void setOnFileDrop(Consumer<File> listener) {
        this.fileDropListener = listener;
    }

    public void setOnStatus(Consumer<String> listener) {
        this.statusListener = listener;
    }

    private void notifyStatus(String msg) {
        if (statusListener != null) {
            statusListener.accept(msg);
        }
    }

    public float getCurrentFps() {
        return currentFps;
    }

    public void exportScreenshot(File targetFile) throws IOException {
        BufferedImage img = renderer.render(currentMesh, camera);
        if (img != null) {
            String format = "PNG";
            if (targetFile.getName().toLowerCase().endsWith(".jpg") || targetFile.getName().toLowerCase().endsWith(".jpeg")) {
                format = "JPEG";
            }
            ImageIO.write(img, format, targetFile);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        long now = System.currentTimeMillis();
        frameCount++;
        if (now - lastFpsTime >= 500) {
            currentFps = (frameCount * 1000.0f) / Math.max(1, (now - lastFpsTime));
            frameCount = 0;
            lastFpsTime = now;
        }

        long t0 = System.nanoTime();
        BufferedImage img = renderer.render(currentMesh, camera);
        long t1 = System.nanoTime();
        lastRenderTimeMs = (t1 - t0) / 1_000_000.0f;

        if (img != null) {
            g.drawImage(img, 0, 0, null);
        }

        // Top-left HUD info (subtle, high-contrast dark capsule)
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));

        if (currentMesh != null) {
            boolean isActive = autoRotating || (now - lastInteractionTime < 600);
            String perfText = isActive
                ? String.format("%.0f FPS (%.1f ms)", Math.max(currentFps, 30.0f), lastRenderTimeMs)
                : String.format("%.1f ms | On-Demand (Pausiert)", lastRenderTimeMs);

            String hudText = String.format("Dreiecke: %,d | %s | Distanz: %.1f mm",
                currentMesh.getTriangleCount(), perfText, camera.distance);

            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(hudText);
            int textH = fm.getHeight();

            // Background pill
            g2.setColor(new Color(15, 18, 26, 195));
            g2.fillRoundRect(12, 10, textW + 18, textH + 8, 8, 8);
            g2.setColor(new Color(55, 66, 88, 180));
            g2.drawRoundRect(12, 10, textW + 18, textH + 8, 8, 8);

            // Text
            g2.setColor(new Color(230, 238, 252));
            g2.drawString(hudText, 21, 10 + fm.getAscent() + 4);
        }

        g2.dispose();
    }
}
