package com.stlviewer.parser;

import com.stlviewer.math.Vec3;
import com.stlviewer.model.Mesh;
import com.stlviewer.model.Triangle;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Locale;

/**
 * High-performance STL parser supporting both ASCII and Binary STL formats.
 */
public class StlParser {

    public interface ProgressListener {
        void onProgress(float percentage, String message);
    }

    public static Mesh parse(File file, ProgressListener listener) throws IOException {
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Datei existiert nicht oder kann nicht gelesen werden: " + file.getAbsolutePath());
        }

        boolean isBinary = isBinaryStl(file);
        Mesh mesh = new Mesh(file.getName());
        mesh.setFormat(isBinary ? "Binary STL" : "ASCII STL");

        if (listener != null) {
            listener.onProgress(0.1f, "Lese " + (isBinary ? "Binär" : "ASCII") + " STL Datei...");
        }

        if (isBinary) {
            parseBinary(file, mesh, listener);
        } else {
            parseAscii(file, mesh, listener);
        }

        if (listener != null) {
            listener.onProgress(0.9f, "Berechne Begrenzungsrahmen & Normalen...");
        }

        mesh.finalizeMesh();

        if (listener != null) {
            listener.onProgress(1.0f, "Fertig geladen!");
        }

        return mesh;
    }

    /**
     * Determines whether an STL file is Binary or ASCII.
     */
    private static boolean isBinaryStl(File file) {
        long fileSize = file.length();
        if (fileSize < 84) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[84];
            int read = fis.read(header);
            if (read < 84) return false;

            ByteBuffer bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
            bb.position(80);
            int triangleCount = bb.getInt();

            if (triangleCount > 0) {
                long expectedSize = 84L + ((long) triangleCount * 50L);
                if (fileSize == expectedSize) {
                    return true;
                }
            }

            // Check if file starts with "solid"
            String headerStr = new String(header, 0, Math.min(read, 80)).trim().toLowerCase(Locale.ENGLISH);
            if (!headerStr.startsWith("solid")) {
                return true;
            }

            // Check if non-ASCII bytes exist in the first 512 bytes
            byte[] sample = new byte[512];
            int sampleRead = fis.read(sample);
            for (int i = 0; i < sampleRead; i++) {
                byte b = sample[i];
                if (b < 0 || (b < 32 && b != '\n' && b != '\r' && b != '\t')) {
                    return true;
                }
            }

            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static void parseBinary(File file, Mesh mesh, ProgressListener listener) throws IOException {
        long fileSize = file.length();
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {

            // Buffer up to 32MB chunks
            int bufferSize = (int) Math.min(fileSize, 32 * 1024 * 1024);
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.LITTLE_ENDIAN);

            // Read header (80 bytes) + triangle count (4 bytes)
            ByteBuffer headBuf = ByteBuffer.allocate(84).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(headBuf);
            headBuf.flip();
            headBuf.position(80);
            int totalTriangles = headBuf.getInt();

            int trianglesRead = 0;
            buffer.clear();

            while (channel.read(buffer) > 0 || buffer.position() > 0) {
                buffer.flip();

                while (buffer.remaining() >= 50 && (totalTriangles <= 0 || trianglesRead < totalTriangles)) {
                    float nx = buffer.getFloat();
                    float ny = buffer.getFloat();
                    float nz = buffer.getFloat();

                    float v1x = buffer.getFloat();
                    float v1y = buffer.getFloat();
                    float v1z = buffer.getFloat();

                    float v2x = buffer.getFloat();
                    float v2y = buffer.getFloat();
                    float v2z = buffer.getFloat();

                    float v3x = buffer.getFloat();
                    float v3y = buffer.getFloat();
                    float v3z = buffer.getFloat();

                    buffer.getShort(); // 2 attribute bytes (unused)

                    Vec3 normal = new Vec3(nx, ny, nz);
                    Vec3 v1 = new Vec3(v1x, v1y, v1z);
                    Vec3 v2 = new Vec3(v2x, v2y, v2z);
                    Vec3 v3 = new Vec3(v3x, v3y, v3z);

                    mesh.addTriangle(new Triangle(v1, v2, v3, normal));
                    trianglesRead++;
                }

                buffer.compact();

                if (listener != null && totalTriangles > 0 && trianglesRead % 25000 == 0) {
                    float progress = 0.1f + 0.8f * ((float) trianglesRead / totalTriangles);
                    listener.onProgress(progress, String.format(Locale.GERMAN, "Lese Dreiecke: %,d / %,d", trianglesRead, totalTriangles));
                }
            }
        }
    }

    private static void parseAscii(File file, Mesh mesh, ProgressListener listener) throws IOException {
        long totalBytes = file.length();
        long bytesRead = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file), 65536)) {
            String line;
            Vec3 normal = null;
            Vec3 v1 = null;
            Vec3 v2 = null;
            Vec3 v3 = null;
            int vertexIndex = 0;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                bytesRead += line.length() + 1;
                String trimmed = line.trim().toLowerCase(Locale.ENGLISH);

                if (trimmed.startsWith("facet normal")) {
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length >= 4) {
                        try {
                            float nx = Float.parseFloat(parts[2]);
                            float ny = Float.parseFloat(parts[3]);
                            float nz = Float.parseFloat(parts[4]);
                            normal = new Vec3(nx, ny, nz);
                        } catch (NumberFormatException ignored) {
                            normal = null;
                        }
                    }
                    vertexIndex = 0;
                } else if (trimmed.startsWith("vertex")) {
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length >= 4) {
                        try {
                            float vx = Float.parseFloat(parts[1]);
                            float vy = Float.parseFloat(parts[2]);
                            float vz = Float.parseFloat(parts[3]);
                            Vec3 v = new Vec3(vx, vy, vz);

                            if (vertexIndex == 0) v1 = v;
                            else if (vertexIndex == 1) v2 = v;
                            else if (vertexIndex == 2) v3 = v;

                            vertexIndex++;
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (trimmed.startsWith("endfacet")) {
                    if (v1 != null && v2 != null && v3 != null) {
                        mesh.addTriangle(new Triangle(v1, v2, v3, normal));
                        count++;
                    }
                    v1 = v2 = v3 = null;
                    normal = null;
                    vertexIndex = 0;

                    if (listener != null && count % 10000 == 0 && totalBytes > 0) {
                        float progress = 0.1f + 0.8f * ((float) bytesRead / totalBytes);
                        listener.onProgress(progress, String.format(Locale.GERMAN, "Lese ASCII Dreiecke: %,d", count));
                    }
                }
            }
        }
    }
}
