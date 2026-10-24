package com.tokia.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tokia.TokiaMod;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Alle Einstellungen von Tokia. Wird als config/tokia.json gespeichert. */
public final class TokiaConfig {
    /** Farbpalette fuer das Crosshair (RGB). */
    public static final int[] PALETTE = {
        0xFFFFFF, 0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x00AA00, 0x55FFFF, 0x00AAAA,
        0x5555FF, 0xAA55FF, 0xFF55FF, 0xFF8FB1, 0xCCCCCC, 0x888888, 0x444444, 0x000000
    };
    public static final int STYLE_COUNT = 4;   // Kreuz, Punkt, Kreis, Quadrat
    public static final int CORNER_COUNT = 4;  // oben links, oben rechts, unten links, unten rechts

    // Crosshair
    public boolean crosshairEnabled = true;
    public int crosshairStyle = 0;
    public int crosshairRgb = 0xFFFFFF;
    public int crosshairOpacity = 100;   // Prozent
    public int crosshairSize = 5;
    public int crosshairThickness = 1;
    public int crosshairGap = 3;
    public boolean crosshairOutline = true;
    public boolean crosshairDot = false;

    // Info-HUD
    public boolean infoEnabled = true;
    public boolean showFps = true;
    public boolean showPing = true;
    public boolean showCoords = true;
    public boolean showCps = true;
    public boolean showClock = false;
    public boolean infoBackground = true;
    public int infoCorner = 0;

    // Keystrokes
    public boolean keystrokes = true;
    public boolean keystrokesMouse = true;
    public boolean keystrokesSpace = true;

    // Weitere Module
    public boolean armorHud = true;
    public boolean effectsHud = true;

    /** Crosshair-Farbe als ARGB inklusive Deckkraft. */
    public int color() {
        int pct = Math.max(0, Math.min(100, crosshairOpacity));
        int alpha = Math.round(pct * 2.55f);
        return (alpha << 24) | (crosshairRgb & 0xFFFFFF);
    }

    // ---- Zuruecksetzen pro Modul ----
    public void resetCrosshair() {
        TokiaConfig d = new TokiaConfig();
        crosshairEnabled = d.crosshairEnabled;
        crosshairStyle = d.crosshairStyle;
        crosshairRgb = d.crosshairRgb;
        crosshairOpacity = d.crosshairOpacity;
        crosshairSize = d.crosshairSize;
        crosshairThickness = d.crosshairThickness;
        crosshairGap = d.crosshairGap;
        crosshairOutline = d.crosshairOutline;
        crosshairDot = d.crosshairDot;
    }

    public void resetInfo() {
        TokiaConfig d = new TokiaConfig();
        infoEnabled = d.infoEnabled;
        showFps = d.showFps;
        showPing = d.showPing;
        showCoords = d.showCoords;
        showCps = d.showCps;
        showClock = d.showClock;
        infoBackground = d.infoBackground;
        infoCorner = d.infoCorner;
    }

    public void resetKeystrokes() {
        TokiaConfig d = new TokiaConfig();
        keystrokes = d.keystrokes;
        keystrokesMouse = d.keystrokesMouse;
        keystrokesSpace = d.keystrokesSpace;
    }

    public void resetArmor() {
        armorHud = new TokiaConfig().armorHud;
    }

    public void resetEffects() {
        effectsHud = new TokiaConfig().effectsHud;
    }

    // ---- Laden / Speichern ----
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TokiaConfig instance = new TokiaConfig();

    public static TokiaConfig get() {
        return instance;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("tokia.json");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) {
            save();
            return;
        }
        try {
            TokiaConfig loaded = GSON.fromJson(Files.readString(p), TokiaConfig.class);
            if (loaded != null) {
                instance = loaded;
            }
        } catch (Exception e) {
            TokiaMod.LOGGER.warn("Konnte tokia.json nicht lesen, nutze Standardwerte", e);
        }
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(instance));
        } catch (Exception e) {
            TokiaMod.LOGGER.warn("Konnte tokia.json nicht speichern", e);
        }
    }
}
