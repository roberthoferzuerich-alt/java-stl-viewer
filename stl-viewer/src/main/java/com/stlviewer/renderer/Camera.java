package com.stlviewer.renderer;

import com.stlviewer.math.Mat4;
import com.stlviewer.math.Vec3;
import com.stlviewer.model.Mesh;

/**
 * Orbit camera supporting 360-degree rotation, panning, zoom, and perspective/orthographic projections.
 */
public class Camera {
    public float yaw = 0.785f;    // ~45 degrees in radians
    public float pitch = 0.523f;  // ~30 degrees in radians
    public float distance = 100.0f;
    public float panX = 0.0f;
    public float panY = 0.0f;

    public Vec3 target = new Vec3(0, 0, 0);
    public boolean isOrthographic = false;
    public float fovY = (float) Math.toRadians(45.0);
    public float nearPlane = 0.1f;
    public float farPlane = 10000.0f;

    public void rotate(float deltaYaw, float deltaPitch) {
        yaw += deltaYaw;
        pitch += deltaPitch;

        // Clamp pitch to avoid gimbal lock / flipping upside down
        float limit = (float) Math.toRadians(89.5);
        if (pitch > limit) pitch = limit;
        if (pitch < -limit) pitch = -limit;
    }

    public void pan(float dx, float dy) {
        // Pan sensitivity scaled by distance
        float scale = distance * 0.0018f;
        panX += dx * scale;
        panY -= dy * scale; // Inverted screen Y
    }

    public void zoom(float factor) {
        distance *= factor;
        if (distance < 0.1f) distance = 0.1f;
        if (distance > 100000.0f) distance = 100000.0f;
    }

    public void resetView(Mesh mesh) {
        panX = 0.0f;
        panY = 0.0f;
        yaw = (float) Math.toRadians(45.0);
        pitch = (float) Math.toRadians(30.0);

        if (mesh != null && mesh.getTriangleCount() > 0) {
            target = new Vec3(mesh.center);
            float span = Math.max(1.0f, mesh.maxSpan);
            distance = span * 2.2f;
        } else {
            target = new Vec3(0, 0, 0);
            distance = 100.0f;
        }
    }

    public void setPresetView(String preset) {
        panX = 0.0f;
        panY = 0.0f;
        switch (preset.toLowerCase()) {
            case "top" -> {
                yaw = 0.0f;
                pitch = (float) Math.toRadians(89.4);
            }
            case "bottom" -> {
                yaw = 0.0f;
                pitch = (float) Math.toRadians(-89.4);
            }
            case "front" -> {
                yaw = 0.0f;
                pitch = 0.0f;
            }
            case "back" -> {
                yaw = (float) Math.PI;
                pitch = 0.0f;
            }
            case "left" -> {
                yaw = (float) (-Math.PI * 0.5);
                pitch = 0.0f;
            }
            case "right" -> {
                yaw = (float) (Math.PI * 0.5);
                pitch = 0.0f;
            }
            case "iso", "isometric" -> {
                yaw = (float) Math.toRadians(45.0);
                pitch = (float) Math.toRadians(30.0);
            }
        }
    }

    public Mat4 computeViewMatrix() {
        // Camera position on sphere around target
        float cosPitch = (float) Math.cos(pitch);
        float sinPitch = (float) Math.sin(pitch);
        float cosYaw = (float) Math.cos(yaw);
        float sinYaw = (float) Math.sin(yaw);

        float camX = target.x + distance * cosPitch * sinYaw;
        float camY = target.y + distance * sinPitch;
        float camZ = target.z + distance * cosPitch * cosYaw;

        Vec3 eye = new Vec3(camX, camY, camZ);
        Vec3 up = new Vec3(0, 1, 0);

        Mat4 look = Mat4.lookAt(eye, target, up);
        // Apply pan translation in camera space
        Mat4 panMat = Mat4.translate(panX, panY, 0);
        return Mat4.multiply(panMat, look);
    }

    public Mat4 computeProjectionMatrix(float aspect) {
        if (isOrthographic) {
            float halfH = distance * 0.45f;
            float halfW = halfH * aspect;
            return Mat4.orthographic(-halfW, halfW, -halfH, halfH, nearPlane, farPlane);
        } else {
            return Mat4.perspective(fovY, aspect, nearPlane, farPlane);
        }
    }

    public Vec3 getEyePosition() {
        float cosPitch = (float) Math.cos(pitch);
        float sinPitch = (float) Math.sin(pitch);
        float cosYaw = (float) Math.cos(yaw);
        float sinYaw = (float) Math.sin(yaw);

        return new Vec3(
            target.x + distance * cosPitch * sinYaw,
            target.y + distance * sinPitch,
            target.z + distance * cosPitch * cosYaw
        );
    }
}
