package com.tokia.client.config;

import com.tokia.client.util.Chroma;

/** Gemeinsame Einstellungen einer einzelnen HUD-Anzeige (Position, Farbe, Hintergrund ...). */
public final class WidgetCfg {
    public boolean enabled = true;
    public boolean label = true;
    public int color = 0xFFFFFF;
    public boolean rainbow = false;
    /** Farbe nach Wert (z. B. FPS gruen/gelb/rot); nur bei Anzeigen, die das unterstuetzen. */
    public boolean dynamicColor = false;
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

    /** Aktuelle Farbe als RGB (Regenbogen oder feste Farbe). */
    public int rgb() {
        return rainbow ? Chroma.rgb() : (color & 0xFFFFFF);
    }

    /** Textfarbe als ARGB. */
    public int textColor() {
        return 0xFF000000 | rgb();
    }

    /** Hintergrundfarbe (Schwarz) mit der eingestellten Deckkraft als ARGB. */
    public int bgColor() {
        int pct = Math.max(0, Math.min(100, bgOpacity));
        return Math.round(pct * 2.55f) << 24;
    }

    /** Setzt Aussehen auf Standard (Position, An/Aus und "Farbe nach Wert" bleiben). */
    public void resetStyle() {
        WidgetCfg d = new WidgetCfg();
        label = d.label;
        color = d.color;
        rainbow = d.rainbow;
        background = d.background;
        bgOpacity = d.bgOpacity;
        shadow = d.shadow;
    }
}
