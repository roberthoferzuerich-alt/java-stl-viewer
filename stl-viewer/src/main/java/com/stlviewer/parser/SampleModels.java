package com.stlviewer.parser;

import com.stlviewer.math.Vec3;
import com.stlviewer.model.Mesh;
import com.stlviewer.model.Triangle;

/**
 * Generates procedural 3D models for immediate demonstration and testing.
 */
public class SampleModels {

    /**
     * Standard 20x20x20 mm 3D Calibration Cube.
     */
    public static Mesh createCalibrationCube() {
        Mesh mesh = new Mesh("Kalibrierungs-Würfel (20mm)");
        mesh.setFormat("Generiert");

        float s = 10.0f; // -10 to +10 mm

        Vec3 p0 = new Vec3(-s, -s, -s);
        Vec3 p1 = new Vec3( s, -s, -s);
        Vec3 p2 = new Vec3( s,  s, -s);
        Vec3 p3 = new Vec3(-s,  s, -s);
        Vec3 p4 = new Vec3(-s, -s,  s);
        Vec3 p5 = new Vec3( s, -s,  s);
        Vec3 p6 = new Vec3( s,  s,  s);
        Vec3 p7 = new Vec3(-s,  s,  s);

        // Front (Z+)
        addQuad(mesh, p4, p5, p6, p7);
        // Back (Z-)
        addQuad(mesh, p1, p0, p3, p2);
        // Left (X-)
        addQuad(mesh, p0, p4, p7, p3);
        // Right (X+)
        addQuad(mesh, p5, p1, p2, p6);
        // Top (Y+)
        addQuad(mesh, p7, p6, p2, p3);
        // Bottom (Y-)
        addQuad(mesh, p0, p1, p5, p4);

        mesh.finalizeMesh();
        return mesh;
    }

    /**
     * Mechanical Spur Gear (Zahnrad) with teeth and center hole.
     */
    public static Mesh createSpurGear() {
        Mesh mesh = new Mesh("Präzisions-Zahnrad (16 Zähne)");
        mesh.setFormat("Generiert");

        int numTeeth = 16;
        int numSegments = numTeeth * 4;
        float rInner = 8.0f;    // Center hole radius
        float rRoot = 18.0f;    // Root radius
        float rTip = 24.0f;     // Tip radius
        float height = 10.0f;   // Thickness

        float zTop = height * 0.5f;
        float zBottom = -height * 0.5f;

        Vec3[] topInner = new Vec3[numSegments];
        Vec3[] botInner = new Vec3[numSegments];
        Vec3[] topOuter = new Vec3[numSegments];
        Vec3[] botOuter = new Vec3[numSegments];

        for (int i = 0; i < numSegments; i++) {
            float angle = (float) (2.0 * Math.PI * i / numSegments);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            // Radius oscillates to create teeth profile
            int toothPart = i % 4;
            float rOuter = (toothPart == 1 || toothPart == 2) ? rTip : rRoot;

            topInner[i] = new Vec3(cos * rInner, sin * rInner, zTop);
            botInner[i] = new Vec3(cos * rInner, sin * rInner, zBottom);
            topOuter[i] = new Vec3(cos * rOuter, sin * rOuter, zTop);
            botOuter[i] = new Vec3(cos * rOuter, sin * rOuter, zBottom);
        }

        for (int i = 0; i < numSegments; i++) {
            int next = (i + 1) % numSegments;

            // Top rim
            addQuad(mesh, topOuter[i], topOuter[next], topInner[next], topInner[i]);
            // Bottom rim
            addQuad(mesh, botInner[i], botInner[next], botOuter[next], botOuter[i]);
            // Outer teeth perimeter
            addQuad(mesh, topOuter[i], botOuter[i], botOuter[next], topOuter[next]);
            // Inner bore hole
            addQuad(mesh, topInner[i], topInner[next], botInner[next], botInner[i]);
        }

        mesh.finalizeMesh();
        return mesh;
    }

    /**
     * Smooth 3D Torus (Donut).
     */
    public static Mesh createTorus() {
        Mesh mesh = new Mesh("Torus (Geometrischer Ring)");
        mesh.setFormat("Generiert");

        int majorSegs = 36;
        int minorSegs = 18;
        float R = 22.0f; // Major radius
        float r = 7.0f;  // Tube radius

        Vec3[][] grid = new Vec3[majorSegs][minorSegs];

        for (int i = 0; i < majorSegs; i++) {
            float u = (float) (2.0 * Math.PI * i / majorSegs);
            float cosU = (float) Math.cos(u);
            float sinU = (float) Math.sin(u);

            for (int j = 0; j < minorSegs; j++) {
                float v = (float) (2.0 * Math.PI * j / minorSegs);
                float cosV = (float) Math.cos(v);
                float sinV = (float) Math.sin(v);

                float x = (R + r * cosV) * cosU;
                float y = (R + r * cosV) * sinU;
                float z = r * sinV;

                grid[i][j] = new Vec3(x, y, z);
            }
        }

        for (int i = 0; i < majorSegs; i++) {
            int nextI = (i + 1) % majorSegs;
            for (int j = 0; j < minorSegs; j++) {
                int nextJ = (j + 1) % minorSegs;

                Vec3 p0 = grid[i][j];
                Vec3 p1 = grid[nextI][j];
                Vec3 p2 = grid[nextI][nextJ];
                Vec3 p3 = grid[i][nextJ];

                addQuad(mesh, p0, p1, p2, p3);
            }
        }

        mesh.finalizeMesh();
        return mesh;
    }

    /**
     * Hexagonal M12 Nut (Sechskant-Mutter).
     */
    public static Mesh createHexNut() {
        Mesh mesh = new Mesh("Sechskantmutter M12");
        mesh.setFormat("Generiert");

        int sides = 6;
        int boreSegments = 24;
        float outerR = 14.0f;
        float innerR = 6.0f;
        float height = 10.0f;

        float zTop = height * 0.5f;
        float zBottom = -height * 0.5f;

        Vec3[] hexTop = new Vec3[sides];
        Vec3[] hexBot = new Vec3[sides];
        for (int i = 0; i < sides; i++) {
            float angle = (float) (2.0 * Math.PI * i / sides + Math.PI / 6.0);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            hexTop[i] = new Vec3(cos * outerR, sin * outerR, zTop);
            hexBot[i] = new Vec3(cos * outerR, sin * outerR, zBottom);
        }

        // Outer 6 faces
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            addQuad(mesh, hexTop[i], hexBot[i], hexBot[next], hexTop[next]);
        }

        // Bore vertices
        Vec3[] boreTop = new Vec3[boreSegments];
        Vec3[] boreBot = new Vec3[boreSegments];
        for (int i = 0; i < boreSegments; i++) {
            float angle = (float) (2.0 * Math.PI * i / boreSegments);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            boreTop[i] = new Vec3(cos * innerR, sin * innerR, zTop);
            boreBot[i] = new Vec3(cos * innerR, sin * innerR, zBottom);
        }

        // Bore inner cylinder
        for (int i = 0; i < boreSegments; i++) {
            int next = (i + 1) % boreSegments;
            addQuad(mesh, boreTop[i], boreTop[next], boreBot[next], boreBot[i]);
        }

        // Top and bottom caps (triangulation between outer hexagon and inner circle)
        int segmentsPerSide = boreSegments / sides;
        for (int i = 0; i < sides; i++) {
            int nextSide = (i + 1) % sides;
            Vec3 ht1 = hexTop[i];
            Vec3 ht2 = hexTop[nextSide];
            Vec3 hb1 = hexBot[i];
            Vec3 hb2 = hexBot[nextSide];

            for (int k = 0; k < segmentsPerSide; k++) {
                int bIdx1 = (i * segmentsPerSide + k) % boreSegments;
                int bIdx2 = (bIdx1 + 1) % boreSegments;

                // Top cap
                mesh.addTriangle(new Triangle(ht1, ht2, boreTop[bIdx1], null));
                mesh.addTriangle(new Triangle(ht2, boreTop[bIdx2], boreTop[bIdx1], null));

                // Bottom cap
                mesh.addTriangle(new Triangle(hb2, hb1, boreBot[bIdx1], null));
                mesh.addTriangle(new Triangle(hb2, boreBot[bIdx1], boreBot[bIdx2], null));
            }
        }

        mesh.finalizeMesh();
        return mesh;
    }

    private static void addQuad(Mesh mesh, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3) {
        mesh.addTriangle(new Triangle(p0, p1, p2, null));
        mesh.addTriangle(new Triangle(p0, p2, p3, null));
    }
}
