package com.tokia.client.util;

/** Regenbogenfarbe, die sich ueber die Zeit durch alle Farbtoene dreht. */
public final class Chroma {
    private Chroma() {
    }

    /** Aktuelle Regenbogenfarbe als RGB (ohne Alpha). */
    public static int rgb() {
        float hue = (System.currentTimeMillis() % 4000L) / 4000f;
        return hsv(hue, 0.75f, 1.0f);
    }

    private static int hsv(float h, float s, float v) {
        float c = v * s;
        float x = c * (1f - Math.abs((h * 6f) % 2f - 1f));
        float m = v - c;
        float r;
        float g;
        float b;
        switch ((int) (h * 6f) % 6) {
            case 0 -> { r = c; g = x; b = 0; }
            case 1 -> { r = x; g = c; b = 0; }
            case 2 -> { r = 0; g = c; b = x; }
            case 3 -> { r = 0; g = x; b = c; }
            case 4 -> { r = x; g = 0; b = c; }
            default -> { r = c; g = 0; b = x; }
        }
        int ri = Math.round((r + m) * 255f);
        int gi = Math.round((g + m) * 255f);
        int bi = Math.round((b + m) * 255f);
        return (ri << 16) | (gi << 8) | bi;
    }
}
