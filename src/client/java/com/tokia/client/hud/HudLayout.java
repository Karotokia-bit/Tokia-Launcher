package com.tokia.client.hud;

/** Rechnet gespeicherte Positionen (0..1) in Bildschirmkoordinaten um und zurueck. */
public final class HudLayout {
    public static final int MARGIN = 4;

    private HudLayout() {
    }

    public static int x(double frac, int w, int guiW) {
        return MARGIN + (int) Math.round(clamp(frac) * Math.max(0, guiW - w - 2 * MARGIN));
    }

    public static int y(double frac, int h, int guiH) {
        return MARGIN + (int) Math.round(clamp(frac) * Math.max(0, guiH - h - 2 * MARGIN));
    }

    public static double fracX(int x, int w, int guiW) {
        int range = guiW - w - 2 * MARGIN;
        return range <= 0 ? 0 : clamp((x - MARGIN) / (double) range);
    }

    public static double fracY(int y, int h, int guiH) {
        int range = guiH - h - 2 * MARGIN;
        return range <= 0 ? 0 : clamp((y - MARGIN) / (double) range);
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
