package com.stlviewer.model;

import com.stlviewer.math.Vec3;

/**
 * Represents a single triangle (facet) of an STL mesh.
 */
public final class Triangle {
    public final Vec3 v1;
    public final Vec3 v2;
    public final Vec3 v3;

    public Vec3 normal;
    public Vec3 n1;
    public Vec3 n2;
    public Vec3 n3;

    public Triangle(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 normal) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;

        if (normal != null && normal.lengthSquared() > 1e-7f) {
            this.normal = normal.normalize();
        } else {
            this.normal = computeNormal();
        }

        // Default vertex normals equal face normal
        this.n1 = this.normal;
        this.n2 = this.normal;
        this.n3 = this.normal;
    }

    public Vec3 computeNormal() {
        Vec3 edge1 = v2.sub(v1);
        Vec3 edge2 = v3.sub(v1);
        Vec3 cross = edge1.cross(edge2);
        return cross.normalize();
    }

    public float computeArea() {
        Vec3 edge1 = v2.sub(v1);
        Vec3 edge2 = v3.sub(v1);
        return edge1.cross(edge2).length() * 0.5f;
    }

    /**
     * Calculates signed volume contribution for a triangle to the origin.
     */
    public float computeSignedVolume() {
        return v1.dot(v2.cross(v3)) / 6.0f;
    }
}
