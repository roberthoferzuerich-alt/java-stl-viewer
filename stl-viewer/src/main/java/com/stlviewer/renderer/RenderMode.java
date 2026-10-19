package com.stlviewer.renderer;

/**
 * Visual rendering modes for the 3D viewer.
 */
public enum RenderMode {
    FLAT_SHADED("Flächen-Schattiert (Flat)"),
    SMOOTH_SHADED("Geglättet (Gouraud)"),
    WIREFRAME("Drahtgitter (Wireframe)"),
    SOLID_WIREFRAME("Schattiert + Gitter"),
    HEIGHT_MAP("Höhenprofil (Z-Gradient)"),
    NORMAL_MAP("Normalen-Farben (RGB)");

    private final String displayName;

    RenderMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
