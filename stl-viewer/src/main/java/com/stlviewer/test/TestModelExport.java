package com.stlviewer.test;

import com.stlviewer.model.Mesh;
import com.stlviewer.parser.SampleModels;
import com.stlviewer.parser.StlExporter;
import com.stlviewer.parser.StlParser;

import java.io.File;

public class TestModelExport {
    public static void main(String[] args) throws Exception {
        System.out.println("--- STL Viewer Self-Test ---");

        // 1. Create procedural gear mesh
        Mesh gear = SampleModels.createSpurGear();
        System.out.println("Procedural Gear triangles: " + gear.getTriangleCount());
        System.out.println("Dimensions: " + gear.size);
        System.out.println("Volume (mm³): " + gear.volumeMm3);
        System.out.println("Surface Area (mm²): " + gear.surfaceAreaMm2);

        // 2. Export to binary STL file
        File sampleFile = new File("beispiel_zahnrad.stl");
        StlExporter.exportBinary(gear, sampleFile);
        System.out.println("Exported binary STL: " + sampleFile.getAbsolutePath() + " (" + sampleFile.length() + " bytes)");

        // 3. Parse it back using StlParser
        Mesh parsed = StlParser.parse(sampleFile, (pct, msg) -> {
            System.out.println("Parsing progress: " + (int)(pct * 100) + "% - " + msg);
        });

        System.out.println("Parsed mesh name: " + parsed.getName());
        System.out.println("Parsed format: " + parsed.getFormat());
        System.out.println("Parsed triangles: " + parsed.getTriangleCount());
        System.out.println("Parsed size: " + parsed.size);

        if (parsed.getTriangleCount() == gear.getTriangleCount()) {
            System.out.println(">>> SELF-TEST PASSED: Triangle count matches perfectly! <<<");
        } else {
            System.err.println(">>> SELF-TEST FAILED: Triangle count mismatch! <<<");
            System.exit(1);
        }

        // Also export calibration cube
        Mesh cube = SampleModels.createCalibrationCube();
        File cubeFile = new File("beispiel_wuerfel_20mm.stl");
        StlExporter.exportBinary(cube, cubeFile);
        System.out.println("Exported calibration cube: " + cubeFile.getName() + " (" + cubeFile.length() + " bytes)");
    }
}
