package com.tokia.client.config;

/** Gemeinsame Einstellungen einer einzelnen HUD-Anzeige (Position, Farbe, Hintergrund ...). */
public final class WidgetCfg {
    public boolean enabled = true;
    public boolean label = true;
    public int color = 0xFFFFFF;
    public boolean background = true;
    public int bgOpacity = 50;
    public boolean shadow = true;
    /** Position: 0 = linker/oberer Rand, 1 = rechter/unterer Rand. */
    public double x = 0.0;
    public double y = 0.0;

    public WidgetCfg() {
    }

    public WidgetCfg(boolean enabled, double x, double y) {
        this.enabled = enabled;
        this.x = x;
        this.y = y;
    }

    /** Textfarbe als ARGB. */
    public int textColor() {
        return 0xFF000000 | (color & 0xFFFFFF);
    }

    /** Hintergrundfarbe (Schwarz) mit der eingestellten Deckkraft als ARGB. */
    public int bgColor() {
        int pct = Math.max(0, Math.min(100, bgOpacity));
        return Math.round(pct * 2.55f) << 24;
    }

    /** Setzt Aussehen auf Standard (Position und An/Aus bleiben). */
    public void resetStyle() {
        WidgetCfg d = new WidgetCfg();
        label = d.label;
        color = d.color;
        background = d.background;
        bgOpacity = d.bgOpacity;
        shadow = d.shadow;
    }
}
