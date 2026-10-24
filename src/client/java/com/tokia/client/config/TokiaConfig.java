package com.tokia.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tokia.TokiaMod;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Alle Einstellungen von Tokia. Wird als config/tokia.json gespeichert. */
public final class TokiaConfig {
    public static final int[] COLORS = {
        0xFFFFFFFF, 0xFFFF5555, 0xFF55FF55, 0xFF5555FF,
        0xFFFFFF55, 0xFF55FFFF, 0xFFFF55FF, 0xFFFFAA00
    };
    public static final int STYLE_COUNT = 4;   // Kreuz, Punkt, Kreis, Quadrat
    public static final int CORNER_COUNT = 4;  // oben links, oben rechts, unten links, unten rechts

    // Crosshair
    public boolean crosshairEnabled = true;
    public int crosshairStyle = 0;
    public int crosshairColor = 0;
    public int crosshairSize = 5;
    public int crosshairThickness = 1;
    public int crosshairGap = 3;
    public boolean crosshairOutline = true;

    // Info-HUD
    public boolean infoEnabled = true;
    public boolean showFps = true;
    public boolean showPing = true;
    public boolean showCoords = true;
    public boolean showCps = true;
    public boolean showClock = false;
    public int infoCorner = 0;

    // Weitere Module
    public boolean keystrokes = true;
    public boolean armorHud = true;
    public boolean effectsHud = true;

    public int color() {
        return COLORS[Math.floorMod(crosshairColor, COLORS.length)];
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
