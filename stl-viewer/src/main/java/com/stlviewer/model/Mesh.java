package com.stlviewer.model;

import com.stlviewer.math.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a 3D mesh consisting of triangles, bounding box, and calculated statistics.
 */
public class Mesh {
    private String name = "Unnamed Mesh";
    private String format = "STL";
    private final List<Triangle> triangles = new ArrayList<>();

    public float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
    public float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
    public float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

    public Vec3 center = new Vec3();
    public Vec3 size = new Vec3();
    public float maxSpan = 1.0f;

    public float surfaceAreaMm2 = 0.0f;
    public float volumeMm3 = 0.0f;
    public int distinctVertexCount = 0;

    public Mesh() {}

    public Mesh(String name) {
        this.name = name;
    }

    public void addTriangle(Triangle triangle) {
        triangles.add(triangle);
    }

    public List<Triangle> getTriangles() {
        return triangles;
    }

    public int getTriangleCount() {
        return triangles.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Finalizes the mesh: computes bounding box, centroid, surface area, volume, and smoothed normals.
     */
    public void finalizeMesh() {
        minX = minY = minZ = Float.POSITIVE_INFINITY;
        maxX = maxY = maxZ = Float.NEGATIVE_INFINITY;
        surfaceAreaMm2 = 0.0f;
        volumeMm3 = 0.0f;

        if (triangles.isEmpty()) {
            center.set(0, 0, 0);
            size.set(0, 0, 0);
            maxSpan = 1.0f;
            return;
        }

        Map<Long, Vec3> normalAccumulator = new HashMap<>();
        Map<Long, Integer> vertexSet = new HashMap<>();

        for (Triangle t : triangles) {
            updateBounds(t.v1);
            updateBounds(t.v2);
            updateBounds(t.v3);

            surfaceAreaMm2 += t.computeArea();
            volumeMm3 += t.computeSignedVolume();

            // Accumulate normals for smooth shading
            accumulateVertexNormal(normalAccumulator, vertexSet, t.v1, t.normal);
            accumulateVertexNormal(normalAccumulator, vertexSet, t.v2, t.normal);
            accumulateVertexNormal(normalAccumulator, vertexSet, t.v3, t.normal);
        }

        // Bounding box calculations
        size.x = Math.max(0.0001f, maxX - minX);
        size.y = Math.max(0.0001f, maxY - minY);
        size.z = Math.max(0.0001f, maxZ - minZ);

        center.x = (minX + maxX) * 0.5f;
        center.y = (minY + maxY) * 0.5f;
        center.z = (minZ + maxZ) * 0.5f;

        maxSpan = Math.max(size.x, Math.max(size.y, size.z));
        volumeMm3 = Math.abs(volumeMm3);
        distinctVertexCount = vertexSet.size();

        // Normalize accumulated normals for smooth shading
        Map<Long, Vec3> normalizedVertexNormals = new HashMap<>();
        for (Map.Entry<Long, Vec3> entry : normalAccumulator.entrySet()) {
            normalizedVertexNormals.put(entry.getKey(), entry.getValue().normalize());
        }

        // Assign smoothed normals
        for (Triangle t : triangles) {
            long k1 = quantizeKey(t.v1);
            long k2 = quantizeKey(t.v2);
            long k3 = quantizeKey(t.v3);

            Vec3 sn1 = normalizedVertexNormals.get(k1);
            Vec3 sn2 = normalizedVertexNormals.get(k2);
            Vec3 sn3 = normalizedVertexNormals.get(k3);

            t.n1 = (sn1 != null) ? sn1 : t.normal;
            t.n2 = (sn2 != null) ? sn2 : t.normal;
            t.n3 = (sn3 != null) ? sn3 : t.normal;
        }
    }

    private void updateBounds(Vec3 v) {
        if (v.x < minX) minX = v.x;
        if (v.x > maxX) maxX = v.x;
        if (v.y < minY) minY = v.y;
        if (v.y > maxY) maxY = v.y;
        if (v.z < minZ) minZ = v.z;
        if (v.z > maxZ) maxZ = v.z;
    }

    private void accumulateVertexNormal(Map<Long, Vec3> normalMap, Map<Long, Integer> vSet, Vec3 v, Vec3 normal) {
        long key = quantizeKey(v);
        vSet.put(key, 1);
        Vec3 acc = normalMap.get(key);
        if (acc == null) {
            normalMap.put(key, new Vec3(normal.x, normal.y, normal.z));
        } else {
            acc.x += normal.x;
            acc.y += normal.y;
            acc.z += normal.z;
        }
    }

    /**
     * Translates the mesh so its bottom face rests flat on the Z=0 build plate (Z-Min = 0)
     * and its XY centroid is centered at the origin (X=0, Y=0).
     */
    public void alignToBuildPlate() {
        if (triangles.isEmpty()) return;
        float dx = -center.x;
        float dy = -center.y;
        float dz = -minZ;
        translate(dx, dy, dz);
    }

    /**
     * Translates all vertices by the specified offsets and recalculates bounds.
     */
    public void translate(float dx, float dy, float dz) {
        for (Triangle t : triangles) {
            t.v1.x += dx; t.v1.y += dy; t.v1.z += dz;
            t.v2.x += dx; t.v2.y += dy; t.v2.z += dz;
            t.v3.x += dx; t.v3.y += dy; t.v3.z += dz;
        }
        finalizeMesh();
    }

    private long quantizeKey(Vec3 v) {
        // Quantize coordinates to ~0.001mm to group identical vertices
        long qx = Math.round(v.x * 1000.0f);
        long qy = Math.round(v.y * 1000.0f);
        long qz = Math.round(v.z * 1000.0f);
        return (qx * 73856093L) ^ (qy * 19349663L) ^ (qz * 83492791L);
    }
}
