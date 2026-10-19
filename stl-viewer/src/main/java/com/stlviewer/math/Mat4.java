package com.stlviewer.math;

/**
 * 4x4 Matrix for 3D coordinate transformations.
 * Stored in column-major order: m[col * 4 + row]
 */
public final class Mat4 {
    public final float[] m = new float[16];

    public Mat4() {
        identity();
    }

    public Mat4 identity() {
        for (int i = 0; i < 16; i++) {
            m[i] = (i % 5 == 0) ? 1.0f : 0.0f;
        }
        return this;
    }

    public static Mat4 createIdentity() {
        return new Mat4().identity();
    }

    public static Mat4 multiply(Mat4 a, Mat4 b) {
        Mat4 res = new Mat4();
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += a.m[k * 4 + row] * b.m[col * 4 + k];
                }
                res.m[col * 4 + row] = sum;
            }
        }
        return res;
    }

    public static Mat4 translate(float tx, float ty, float tz) {
        Mat4 res = new Mat4();
        res.m[12] = tx;
        res.m[13] = ty;
        res.m[14] = tz;
        return res;
    }

    public static Mat4 scale(float sx, float sy, float sz) {
        Mat4 res = new Mat4();
        res.m[0] = sx;
        res.m[5] = sy;
        res.m[10] = sz;
        return res;
    }

    public static Mat4 rotateX(float angleRad) {
        Mat4 res = new Mat4();
        float c = (float) Math.cos(angleRad);
        float s = (float) Math.sin(angleRad);
        res.m[5] = c;
        res.m[6] = s;
        res.m[9] = -s;
        res.m[10] = c;
        return res;
    }

    public static Mat4 rotateY(float angleRad) {
        Mat4 res = new Mat4();
        float c = (float) Math.cos(angleRad);
        float s = (float) Math.sin(angleRad);
        res.m[0] = c;
        res.m[2] = -s;
        res.m[8] = s;
        res.m[10] = c;
        return res;
    }

    public static Mat4 rotateZ(float angleRad) {
        Mat4 res = new Mat4();
        float c = (float) Math.cos(angleRad);
        float s = (float) Math.sin(angleRad);
        res.m[0] = c;
        res.m[1] = s;
        res.m[4] = -s;
        res.m[5] = c;
        return res;
    }

    public static Mat4 lookAt(Vec3 eye, Vec3 target, Vec3 up) {
        Vec3 f = target.sub(eye).normalize();
        Vec3 s = f.cross(up).normalize();
        Vec3 u = s.cross(f);

        Mat4 res = new Mat4();
        res.m[0] = s.x;
        res.m[4] = s.y;
        res.m[8] = s.z;

        res.m[1] = u.x;
        res.m[5] = u.y;
        res.m[9] = u.z;

        res.m[2] = -f.x;
        res.m[6] = -f.y;
        res.m[10] = -f.z;

        res.m[12] = -s.dot(eye);
        res.m[13] = -u.dot(eye);
        res.m[14] = f.dot(eye);
        res.m[15] = 1.0f;
        return res;
    }

    public static Mat4 perspective(float fovYRad, float aspect, float zNear, float zFar) {
        Mat4 res = new Mat4();
        float tanHalfFov = (float) Math.tan(fovYRad * 0.5f);
        for (int i = 0; i < 16; i++) res.m[i] = 0;

        res.m[0] = 1.0f / (aspect * tanHalfFov);
        res.m[5] = 1.0f / tanHalfFov;
        res.m[10] = -(zFar + zNear) / (zFar - zNear);
        res.m[11] = -1.0f;
        res.m[14] = -(2.0f * zFar * zNear) / (zFar - zNear);
        return res;
    }

    public static Mat4 orthographic(float left, float right, float bottom, float top, float zNear, float zFar) {
        Mat4 res = new Mat4();
        for (int i = 0; i < 16; i++) res.m[i] = 0;

        res.m[0] = 2.0f / (right - left);
        res.m[5] = 2.0f / (top - bottom);
        res.m[10] = -2.0f / (zFar - zNear);
        res.m[12] = -(right + left) / (right - left);
        res.m[13] = -(top + bottom) / (top - bottom);
        res.m[14] = -(zFar + zNear) / (zFar - zNear);
        res.m[15] = 1.0f;
        return res;
    }

    /**
     * Transforms a 3D point (w = 1.0) and performs homogeneous coordinate division.
     * Result coordinates are NDC (Normalized Device Coordinates) [-1, 1] plus w in out[3].
     */
    public void transformPoint(float x, float y, float z, float[] out4) {
        float rx = m[0] * x + m[4] * y + m[8] * z + m[12];
        float ry = m[1] * x + m[5] * y + m[9] * z + m[13];
        float rz = m[2] * x + m[6] * y + m[10] * z + m[14];
        float rw = m[3] * x + m[7] * y + m[11] * z + m[15];

        out4[0] = rx;
        out4[1] = ry;
        out4[2] = rz;
        out4[3] = rw;
    }

    /**
     * Transforms a 3D normal vector (w = 0.0) without translation.
     */
    public Vec3 transformNormal(Vec3 v) {
        float rx = m[0] * v.x + m[4] * v.y + m[8] * v.z;
        float ry = m[1] * v.x + m[5] * v.y + m[9] * v.z;
        float rz = m[2] * v.x + m[6] * v.y + m[10] * v.z;
        return new Vec3(rx, ry, rz).normalize();
    }
}
