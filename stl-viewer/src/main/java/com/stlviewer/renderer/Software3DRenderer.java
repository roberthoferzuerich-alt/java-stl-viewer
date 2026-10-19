package com.stlviewer.renderer;

import com.stlviewer.math.Mat4;
import com.stlviewer.math.Vec3;
import com.stlviewer.model.Mesh;
import com.stlviewer.model.Triangle;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Multi-threaded software 3D rasterizer with Z-buffering, Blinn-Phong lighting,
 * smooth shading, height maps, wireframe overlays, ground grid, and 3D orientation gizmo.
 */
public class Software3DRenderer {

    private int width = 800;
    private int height = 600;

    private BufferedImage bufferImage;
    private int[] pixels;
    private float[] zBuffer;

    private RenderMode renderMode = RenderMode.FLAT_SHADED;
    private ColorTheme colorTheme = ColorTheme.TITANIUM;
    private boolean backfaceCulling = true;
    private boolean showGrid = true;
    private boolean showAxesGizmo = true;
    private boolean showBoundingBox = false;
    private boolean headlightMode = true; // Light follows camera vs fixed world light

    private static final int NUM_SLICES = Math.max(2, Runtime.getRuntime().availableProcessors());

    public Software3DRenderer(int width, int height) {
        resize(width, height);
    }

    public synchronized void resize(int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (this.width == w && this.height == h && bufferImage != null) return;

        this.width = w;
        this.height = h;

        bufferImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) bufferImage.getRaster().getDataBuffer()).getData();
        zBuffer = new float[width * height];
    }

    public synchronized BufferedImage render(Mesh mesh, Camera camera) {
        if (width <= 0 || height <= 0 || bufferImage == null) {
            return null;
        }

        // 1. Clear background and Z-buffer
        fillBackground();
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);

        if (mesh == null || mesh.getTriangleCount() == 0) {
            drawEmptyMessage();
            return bufferImage;
        }

        // 2. Matrices setup
        float aspect = (float) width / (float) height;
        Mat4 viewMat = camera.computeViewMatrix();
        Mat4 projMat = camera.computeProjectionMatrix(aspect);
        Mat4 mvpMat = Mat4.multiply(projMat, viewMat);

        // Light setup
        final Vec3 lightDir = headlightMode
            ? camera.getEyePosition().sub(camera.target).normalize().add(new Vec3(0.25f, 0.45f, 0.15f)).normalize()
            : new Vec3(0.4f, 0.8f, 0.5f).normalize();

        // 3. Render Ground Grid (if enabled)
        if (showGrid) {
            renderGrid(mesh, mvpMat);
        }

        // 4. Render 3D Bounding Box (if enabled)
        if (showBoundingBox) {
            renderBoundingBox(mesh, mvpMat);
        }

        // 5. Pre-transform triangles or render in parallel bands
        final List<Triangle> triangles = mesh.getTriangles();
        final int triCount = triangles.size();

        // Project all triangles into view/clip/screen space
        final ProjectedTriangle[] projected = new ProjectedTriangle[triCount];
        final float meshMinZ = mesh.minZ;
        final float meshSpanZ = Math.max(0.001f, mesh.maxZ - mesh.minZ);
        final float[] matDiffuse = colorTheme.diffuse;
        final float[] matSpecular = colorTheme.specular;

        // Parallel vertex transformation and lighting setup
        IntStream.range(0, triCount).parallel().forEach(i -> {
            Triangle t = triangles.get(i);
            ProjectedTriangle pt = new ProjectedTriangle();

            float[] clip1 = new float[4];
            float[] clip2 = new float[4];
            float[] clip3 = new float[4];

            mvpMat.transformPoint(t.v1.x, t.v1.y, t.v1.z, clip1);
            mvpMat.transformPoint(t.v2.x, t.v2.y, t.v2.z, clip2);
            mvpMat.transformPoint(t.v3.x, t.v3.y, t.v3.z, clip3);

            // Near-plane culling
            if (clip1[3] <= 0.001f && clip2[3] <= 0.001f && clip3[3] <= 0.001f) {
                projected[i] = null;
                return;
            }

            pt.sx1 = (clip1[0] / clip1[3] * 0.5f + 0.5f) * width;
            pt.sy1 = (1.0f - (clip1[1] / clip1[3] * 0.5f + 0.5f)) * height;
            pt.sz1 = clip1[2] / clip1[3];

            pt.sx2 = (clip2[0] / clip2[3] * 0.5f + 0.5f) * width;
            pt.sy2 = (1.0f - (clip2[1] / clip2[3] * 0.5f + 0.5f)) * height;
            pt.sz2 = clip2[2] / clip2[3];

            pt.sx3 = (clip3[0] / clip3[3] * 0.5f + 0.5f) * width;
            pt.sy3 = (1.0f - (clip3[1] / clip3[3] * 0.5f + 0.5f)) * height;
            pt.sz3 = clip3[2] / clip3[3];

            // 2D Signed Area for backface culling
            float area = (pt.sx2 - pt.sx1) * (pt.sy3 - pt.sy1) - (pt.sy2 - pt.sy1) * (pt.sx3 - pt.sx1);
            pt.area = area;

            if (backfaceCulling && area <= 0.0f) {
                projected[i] = null;
                return;
            }

            // Calculate lighting based on render mode
            switch (renderMode) {
                case FLAT_SHADED, SOLID_WIREFRAME -> {
                    float dot = Math.abs(t.normal.dot(lightDir));
                    float diff = Math.max(0.0f, dot);
                    float lightFactor = 0.25f + 0.75f * diff;
                    pt.r1 = pt.r2 = pt.r3 = Math.min(1.0f, matDiffuse[0] * lightFactor);
                    pt.g1 = pt.g2 = pt.g3 = Math.min(1.0f, matDiffuse[1] * lightFactor);
                    pt.b1 = pt.b2 = pt.b3 = Math.min(1.0f, matDiffuse[2] * lightFactor);
                }
                case SMOOTH_SHADED -> {
                    float d1 = Math.max(0.0f, Math.abs(t.n1.dot(lightDir)));
                    float d2 = Math.max(0.0f, Math.abs(t.n2.dot(lightDir)));
                    float d3 = Math.max(0.0f, Math.abs(t.n3.dot(lightDir)));

                    float l1 = 0.22f + 0.78f * d1;
                    float l2 = 0.22f + 0.78f * d2;
                    float l3 = 0.22f + 0.78f * d3;

                    pt.r1 = Math.min(1.0f, matDiffuse[0] * l1);
                    pt.g1 = Math.min(1.0f, matDiffuse[1] * l1);
                    pt.b1 = Math.min(1.0f, matDiffuse[2] * l1);

                    pt.r2 = Math.min(1.0f, matDiffuse[0] * l2);
                    pt.g2 = Math.min(1.0f, matDiffuse[1] * l2);
                    pt.b2 = Math.min(1.0f, matDiffuse[2] * l2);

                    pt.r3 = Math.min(1.0f, matDiffuse[0] * l3);
                    pt.g3 = Math.min(1.0f, matDiffuse[1] * l3);
                    pt.b3 = Math.min(1.0f, matDiffuse[2] * l3);
                }
                case HEIGHT_MAP -> {
                    float t1 = (t.v1.z - meshMinZ) / meshSpanZ;
                    float t2 = (t.v2.z - meshMinZ) / meshSpanZ;
                    float t3 = (t.v3.z - meshMinZ) / meshSpanZ;

                    int c1 = evaluateColorMap(t1);
                    int c2 = evaluateColorMap(t2);
                    int c3 = evaluateColorMap(t3);

                    pt.r1 = ((c1 >> 16) & 0xFF) / 255.0f;
                    pt.g1 = ((c1 >> 8) & 0xFF) / 255.0f;
                    pt.b1 = (c1 & 0xFF) / 255.0f;

                    pt.r2 = ((c2 >> 16) & 0xFF) / 255.0f;
                    pt.g2 = ((c2 >> 8) & 0xFF) / 255.0f;
                    pt.b2 = (c2 & 0xFF) / 255.0f;

                    pt.r3 = ((c3 >> 16) & 0xFF) / 255.0f;
                    pt.g3 = ((c3 >> 8) & 0xFF) / 255.0f;
                    pt.b3 = (c3 & 0xFF) / 255.0f;
                }
                case NORMAL_MAP -> {
                    pt.r1 = pt.r2 = pt.r3 = t.normal.x * 0.5f + 0.5f;
                    pt.g1 = pt.g2 = pt.g3 = t.normal.y * 0.5f + 0.5f;
                    pt.b1 = pt.b2 = pt.b3 = t.normal.z * 0.5f + 0.5f;
                }
                case WIREFRAME -> {
                    // Handled in wireframe stage
                }
            }

            projected[i] = pt;
        });

        // 6. Multi-threaded Horizontal Band Rasterization (Solid Faces)
        if (renderMode != RenderMode.WIREFRAME) {
            final int sliceHeight = (height + NUM_SLICES - 1) / NUM_SLICES;

            IntStream.range(0, NUM_SLICES).parallel().forEach(slice -> {
                int yStart = slice * sliceHeight;
                int yEnd = Math.min(height, yStart + sliceHeight);

                for (ProjectedTriangle pt : projected) {
                    if (pt == null) continue;

                    int minScanY = Math.max(yStart, (int) Math.floor(Math.min(pt.sy1, Math.min(pt.sy2, pt.sy3))));
                    int maxScanY = Math.min(yEnd - 1, (int) Math.ceil(Math.max(pt.sy1, Math.max(pt.sy2, pt.sy3))));

                    if (minScanY > maxScanY) continue;

                    int minScanX = Math.max(0, (int) Math.floor(Math.min(pt.sx1, Math.min(pt.sx2, pt.sx3))));
                    int maxScanX = Math.min(width - 1, (int) Math.ceil(Math.max(pt.sx1, Math.max(pt.sx2, pt.sx3))));

                    if (minScanX > maxScanX) continue;

                    float invArea = 1.0f / pt.area;

                    for (int y = minScanY; y <= maxScanY; y++) {
                        int rowOffset = y * width;
                        float fy = y + 0.5f;

                        for (int x = minScanX; x <= maxScanX; x++) {
                            float fx = x + 0.5f;

                            // Barycentric coordinates
                            float w1 = ((pt.sx3 - pt.sx2) * (fy - pt.sy2) - (pt.sy3 - pt.sy2) * (fx - pt.sx2)) * invArea;
                            float w2 = ((pt.sx1 - pt.sx3) * (fy - pt.sy3) - (pt.sy1 - pt.sy3) * (fx - pt.sx3)) * invArea;
                            float w3 = 1.0f - w1 - w2;

                            if (w1 >= 0.0f && w2 >= 0.0f && w3 >= 0.0f) {
                                float z = w1 * pt.sz1 + w2 * pt.sz2 + w3 * pt.sz3;
                                int idx = rowOffset + x;

                                if (z < zBuffer[idx]) {
                                    zBuffer[idx] = z;

                                    float r = w1 * pt.r1 + w2 * pt.r2 + w3 * pt.r3;
                                    float g = w1 * pt.g1 + w2 * pt.g2 + w3 * pt.g3;
                                    float b = w1 * pt.b1 + w2 * pt.b2 + w3 * pt.b3;

                                    int ir = (int) (r * 255.0f);
                                    int ig = (int) (g * 255.0f);
                                    int ib = (int) (b * 255.0f);

                                    pixels[idx] = (ir << 16) | (ig << 8) | ib;
                                }
                            }
                        }
                    }
                }
            });
        }

        // 7. Wireframe Overlay (for WIREFRAME or SOLID_WIREFRAME modes)
        if (renderMode == RenderMode.WIREFRAME || renderMode == RenderMode.SOLID_WIREFRAME) {
            int wireCol = colorTheme.wireColor;
            float zBias = (renderMode == RenderMode.SOLID_WIREFRAME) ? -0.0006f : 0.0f;

            for (ProjectedTriangle pt : projected) {
                if (pt == null) continue;
                drawLine3D(pt.sx1, pt.sy1, pt.sz1 + zBias, pt.sx2, pt.sy2, pt.sz2 + zBias, wireCol);
                drawLine3D(pt.sx2, pt.sy2, pt.sz2 + zBias, pt.sx3, pt.sy3, pt.sz3 + zBias, wireCol);
                drawLine3D(pt.sx3, pt.sy3, pt.sz3 + zBias, pt.sx1, pt.sy1, pt.sz1 + zBias, wireCol);
            }
        }

        // 8. 3D Orientation Gizmo (Corner Compass)
        if (showAxesGizmo) {
            renderAxesGizmo(camera);
        }

        return bufferImage;
    }

    private void fillBackground() {
        Color top = colorTheme.bgTop;
        Color bot = colorTheme.bgBottom;

        int r1 = top.getRed(), g1 = top.getGreen(), b1 = top.getBlue();
        int r2 = bot.getRed(), g2 = bot.getGreen(), b2 = bot.getBlue();

        for (int y = 0; y < height; y++) {
            float t = (float) y / (float) (height - 1);
            int r = (int) (r1 + t * (r2 - r1));
            int g = (int) (g1 + t * (g2 - g1));
            int b = (int) (b1 + t * (b2 - b1));
            int rgb = (r << 16) | (g << 8) | b;

            int rowOffset = y * width;
            Arrays.fill(pixels, rowOffset, rowOffset + width, rgb);
        }
    }

    private void drawEmptyMessage() {
        Graphics2D g2 = bufferImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(160, 170, 190, 180));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        String msg1 = "Kein STL-Modell geladen";
        String msg2 = "Ziehe eine .stl Datei hierher oder wähle oben '📂 STL laden'";

        int w1 = g2.getFontMetrics().stringWidth(msg1);
        int w2 = g2.getFontMetrics().stringWidth(msg2);

        g2.drawString(msg1, (width - w1) / 2, height / 2 - 15);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2.drawString(msg2, (width - w2) / 2, height / 2 + 15);
        g2.dispose();
    }

    /**
     * Renders a 3D ground grid beneath the model.
     */
    private void renderGrid(Mesh mesh, Mat4 mvpMat) {
        float floorZ = mesh.minZ;
        float span = Math.max(20.0f, mesh.maxSpan);
        float step = span / 10.0f;
        int gridColor = 0x223040;
        int centerColor = 0x3d5a7d;

        float startX = mesh.center.x - span;
        float endX = mesh.center.x + span;
        float startY = mesh.center.y - span;
        float endY = mesh.center.y + span;

        float[] c1 = new float[4];
        float[] c2 = new float[4];

        for (float x = startX; x <= endX + 0.1f; x += step) {
            int col = Math.abs(x - mesh.center.x) < step * 0.4f ? centerColor : gridColor;
            mvpMat.transformPoint(x, startY, floorZ, c1);
            mvpMat.transformPoint(x, endY, floorZ, c2);
            projectAndDrawLine(c1, c2, col);
        }

        for (float y = startY; y <= endY + 0.1f; y += step) {
            int col = Math.abs(y - mesh.center.y) < step * 0.4f ? centerColor : gridColor;
            mvpMat.transformPoint(startX, y, floorZ, c1);
            mvpMat.transformPoint(endX, y, floorZ, c2);
            projectAndDrawLine(c1, c2, col);
        }
    }

    private void renderBoundingBox(Mesh mesh, Mat4 mvpMat) {
        float x1 = mesh.minX, x2 = mesh.maxX;
        float y1 = mesh.minY, y2 = mesh.maxY;
        float z1 = mesh.minZ, z2 = mesh.maxZ;
        int col = 0x00d0ff;

        Vec3[] corners = {
            new Vec3(x1, y1, z1), new Vec3(x2, y1, z1),
            new Vec3(x2, y2, z1), new Vec3(x1, y2, z1),
            new Vec3(x1, y1, z2), new Vec3(x2, y1, z2),
            new Vec3(x2, y2, z2), new Vec3(x1, y2, z2)
        };

        int[][] edges = {
            {0,1}, {1,2}, {2,3}, {3,0},
            {4,5}, {5,6}, {6,7}, {7,4},
            {0,4}, {1,5}, {2,6}, {3,7}
        };

        float[] c1 = new float[4];
        float[] c2 = new float[4];

        for (int[] e : edges) {
            Vec3 pA = corners[e[0]];
            Vec3 pB = corners[e[1]];
            mvpMat.transformPoint(pA.x, pA.y, pA.z, c1);
            mvpMat.transformPoint(pB.x, pB.y, pB.z, c2);
            projectAndDrawLine(c1, c2, col);
        }
    }

    private void projectAndDrawLine(float[] c1, float[] c2, int color) {
        if (c1[3] <= 0.001f || c2[3] <= 0.001f) return;

        float sx1 = (c1[0] / c1[3] * 0.5f + 0.5f) * width;
        float sy1 = (1.0f - (c1[1] / c1[3] * 0.5f + 0.5f)) * height;
        float sz1 = c1[2] / c1[3];

        float sx2 = (c2[0] / c2[3] * 0.5f + 0.5f) * width;
        float sy2 = (1.0f - (c2[1] / c2[3] * 0.5f + 0.5f)) * height;
        float sz2 = c2[2] / c2[3];

        drawLine3D(sx1, sy1, sz1, sx2, sy2, sz2, color);
    }

    /**
     * Bresenham 3D line drawing with depth-test.
     */
    private void drawLine3D(float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        int ix1 = Math.round(x1);
        int iy1 = Math.round(y1);
        int ix2 = Math.round(x2);
        int iy2 = Math.round(y2);

        int dx = Math.abs(ix2 - ix1);
        int dy = Math.abs(iy2 - iy1);
        int sx = ix1 < ix2 ? 1 : -1;
        int sy = iy1 < iy2 ? 1 : -1;
        int err = dx - dy;

        int steps = Math.max(dx, dy);
        if (steps == 0) return;
        float dz = (z2 - z1) / steps;
        float curZ = z1;

        int curX = ix1;
        int curY = iy1;

        for (int i = 0; i <= steps; i++) {
            if (curX >= 0 && curX < width && curY >= 0 && curY < height) {
                int idx = curY * width + curX;
                if (curZ <= zBuffer[idx] + 0.001f) {
                    pixels[idx] = color;
                }
            }

            if (curX == ix2 && curY == iy2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                curX += sx;
            }
            if (e2 < dx) {
                err += dx;
                curY += sy;
            }
            curZ += dz;
        }
    }

    /**
     * Renders 3D coordinate axes compass in the bottom-left corner of the viewport.
     */
    private void renderAxesGizmo(Camera camera) {
        int originX = 65;
        int originY = height - 65;
        float gizmoSize = 42.0f;

        Mat4 rotMat = camera.computeViewMatrix();
        // Remove translation part from view matrix for gizmo
        rotMat.m[12] = 0;
        rotMat.m[13] = 0;
        rotMat.m[14] = 0;

        Vec3 axX = rotMat.transformNormal(new Vec3(1, 0, 0));
        Vec3 axY = rotMat.transformNormal(new Vec3(0, 1, 0));
        Vec3 axZ = rotMat.transformNormal(new Vec3(0, 0, 1));

        Graphics2D g = bufferImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Circular semi-transparent background
        g.setColor(new Color(20, 24, 32, 160));
        g.fillOval(originX - 52, originY - 52, 104, 104);
        g.setColor(new Color(60, 70, 90, 140));
        g.drawOval(originX - 52, originY - 52, 104, 104);

        g.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Draw axes in order of depth (Z sort for gizmo)
        Axis[] axes = {
            new Axis("X", axX, new Color(240, 60, 60)),
            new Axis("Y", axY, new Color(60, 210, 60)),
            new Axis("Z", axZ, new Color(60, 140, 255))
        };
        Arrays.sort(axes, (a, b) -> Float.compare(a.dir.z, b.dir.z));

        for (Axis axis : axes) {
            int destX = originX + Math.round(axis.dir.x * gizmoSize);
            int destY = originY - Math.round(axis.dir.y * gizmoSize);

            g.setColor(axis.color);
            g.drawLine(originX, originY, destX, destY);
            g.fillOval(destX - 3, destY - 3, 6, 6);
            g.drawString(axis.name, destX + 5, destY + 4);
        }

        g.dispose();
    }

    private int evaluateColorMap(float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        // Cyan -> Green -> Yellow -> Red gradient
        int r, g, b;
        if (t < 0.25f) {
            float f = t / 0.25f;
            r = 0;
            g = (int) (f * 255);
            b = 255;
        } else if (t < 0.5f) {
            float f = (t - 0.25f) / 0.25f;
            r = 0;
            g = 255;
            b = (int) ((1.0f - f) * 255);
        } else if (t < 0.75f) {
            float f = (t - 0.5f) / 0.25f;
            r = (int) (f * 255);
            g = 255;
            b = 0;
        } else {
            float f = (t - 0.75f) / 0.25f;
            r = 255;
            g = (int) ((1.0f - f) * 255);
            b = 0;
        }
        return (r << 16) | (g << 8) | b;
    }

    // Getters and Setters
    public RenderMode getRenderMode() { return renderMode; }
    public void setRenderMode(RenderMode renderMode) { this.renderMode = renderMode; }

    public ColorTheme getColorTheme() { return colorTheme; }
    public void setColorTheme(ColorTheme colorTheme) { this.colorTheme = colorTheme; }

    public boolean isBackfaceCulling() { return backfaceCulling; }
    public void setBackfaceCulling(boolean backfaceCulling) { this.backfaceCulling = backfaceCulling; }

    public boolean isShowGrid() { return showGrid; }
    public void setShowGrid(boolean showGrid) { this.showGrid = showGrid; }

    public boolean isShowAxesGizmo() { return showAxesGizmo; }
    public void setShowAxesGizmo(boolean showAxesGizmo) { this.showAxesGizmo = showAxesGizmo; }

    public boolean isShowBoundingBox() { return showBoundingBox; }
    public void setShowBoundingBox(boolean showBoundingBox) { this.showBoundingBox = showBoundingBox; }

    public boolean isHeadlightMode() { return headlightMode; }
    public void setHeadlightMode(boolean headlightMode) { this.headlightMode = headlightMode; }

    private static class ProjectedTriangle {
        float sx1, sy1, sz1;
        float sx2, sy2, sz2;
        float sx3, sy3, sz3;
        float area;
        float r1, g1, b1;
        float r2, g2, b2;
        float r3, g3, b3;
    }

    private static class Axis {
        String name;
        Vec3 dir;
        Color color;
        Axis(String name, Vec3 dir, Color color) {
            this.name = name;
            this.dir = dir;
            this.color = color;
        }
    }
}
