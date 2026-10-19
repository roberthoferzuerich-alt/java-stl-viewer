package com.stlviewer.renderer;

import java.awt.Color;

/**
 * Predefined materials and color themes for model rendering and viewport background.
 */
public enum ColorTheme {
    TITANIUM("Titanium Silber",
            new float[]{0.78f, 0.82f, 0.88f},
            new float[]{1.0f, 1.0f, 1.0f},
            0x252830,
            new Color(0x1a, 0x1d, 0x24),
            new Color(0x10, 0x12, 0x16)),

    CYAN_TECH("Cyan High-Tech",
            new float[]{0.12f, 0.72f, 0.88f},
            new float[]{0.6f, 0.95f, 1.0f},
            0x093040,
            new Color(0x13, 0x1b, 0x24),
            new Color(0x0a, 0x0e, 0x14)),

    GOLD_SUNSET("Gold & Bernstein",
            new float[]{0.92f, 0.65f, 0.18f},
            new float[]{1.0f, 0.9f, 0.6f},
            0x402508,
            new Color(0x22, 0x1a, 0x14),
            new Color(0x12, 0x0e, 0x0a)),

    EMERALD("Smaragd Grün",
            new float[]{0.22f, 0.80f, 0.48f},
            new float[]{0.7f, 1.0f, 0.8f},
            0x0a3820,
            new Color(0x12, 0x22, 0x18),
            new Color(0x0a, 0x14, 0x0e)),

    CRIMSON("Rubinrot",
            new float[]{0.90f, 0.25f, 0.32f},
            new float[]{1.0f, 0.7f, 0.75f},
            0x400e14,
            new Color(0x24, 0x14, 0x16),
            new Color(0x14, 0x0a, 0x0c)),

    WHITE_RESIN("Weißes Harz / Clay",
            new float[]{0.92f, 0.92f, 0.94f},
            new float[]{1.0f, 1.0f, 1.0f},
            0x404450,
            new Color(0x28, 0x2b, 0x34),
            new Color(0x18, 0x1a, 0x20));

    private final String name;
    public final float[] diffuse;
    public final float[] specular;
    public final int wireColor;
    public final Color bgTop;
    public final Color bgBottom;

    ColorTheme(String name, float[] diffuse, float[] specular, int wireColor, Color bgTop, Color bgBottom) {
        this.name = name;
        this.diffuse = diffuse;
        this.specular = specular;
        this.wireColor = wireColor;
        this.bgTop = bgTop;
        this.bgBottom = bgBottom;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
