package com.stlviewer.parser;

import com.stlviewer.math.Vec3;
import com.stlviewer.model.Mesh;
import com.stlviewer.model.Triangle;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Exports meshes to standard Binary STL format.
 */
public class StlExporter {

    public static void exportBinary(Mesh mesh, File file) throws IOException {
        List<Triangle> triangles = mesh.getTriangles();
        int triangleCount = triangles.size();

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            // 80-byte header
            byte[] header = new byte[80];
            String title = "Exported by Java STL Viewer: " + mesh.getName();
            byte[] titleBytes = title.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(titleBytes, 0, header, 0, Math.min(titleBytes.length, 80));
            bos.write(header);

            // 4-byte triangle count (uint32, little-endian)
            ByteBuffer countBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            countBuf.putInt(triangleCount);
            bos.write(countBuf.array());

            // Facets (50 bytes each)
            ByteBuffer facetBuf = ByteBuffer.allocate(50).order(ByteOrder.LITTLE_ENDIAN);

            for (Triangle t : triangles) {
                facetBuf.clear();

                // Normal
                facetBuf.putFloat(t.normal.x);
                facetBuf.putFloat(t.normal.y);
                facetBuf.putFloat(t.normal.z);

                // Vertices
                facetBuf.putFloat(t.v1.x);
                facetBuf.putFloat(t.v1.y);
                facetBuf.putFloat(t.v1.z);

                facetBuf.putFloat(t.v2.x);
                facetBuf.putFloat(t.v2.y);
                facetBuf.putFloat(t.v2.z);

                facetBuf.putFloat(t.v3.x);
                facetBuf.putFloat(t.v3.y);
                facetBuf.putFloat(t.v3.z);

                // Attribute byte count (2 bytes)
                facetBuf.putShort((short) 0);

                bos.write(facetBuf.array());
            }
            bos.flush();
        }
    }
}
