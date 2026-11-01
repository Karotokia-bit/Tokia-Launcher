package com.tokia.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tokia.TokiaMod;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Alle Einstellungen von Tokia. Wird als config/tokia.json gespeichert. */
public final class TokiaConfig {
    /** Farbpalette (RGB). */
    public static final int[] PALETTE = {
        0xFFFFFF, 0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x00AA00, 0x55FFFF, 0x00AAAA,
        0x5555FF, 0xAA55FF, 0xFF55FF, 0xFF8FB1, 0xCCCCCC, 0x888888, 0x444444, 0x000000
    };
    public static final int STYLE_COUNT = 4;   // Kreuz, Punkt, Kreis, Quadrat

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

    // Einzelne Info-Anzeigen (jede mit eigener Position und eigenem Stil)
    public WidgetCfg fps = new WidgetCfg(true, 0.0, 0.00);
    public WidgetCfg ping = new WidgetCfg(true, 0.0, 0.05);
    public WidgetCfg coords = new WidgetCfg(true, 0.0, 0.10);
    public WidgetCfg cps = new WidgetCfg(true, 0.0, 0.15);
    public WidgetCfg frametime = new WidgetCfg(false, 0.0, 0.20);
    public WidgetCfg chunk = new WidgetCfg(false, 0.0, 0.25);
    public WidgetCfg direction = new WidgetCfg(false, 0.0, 0.30);
    public WidgetCfg angles = new WidgetCfg(false, 0.0, 0.35);
    public WidgetCfg speed = new WidgetCfg(false, 0.0, 0.40);
    public WidgetCfg ram = new WidgetCfg(false, 0.0, 0.45);
    public WidgetCfg handItem = new WidgetCfg(false, 0.0, 0.50);
    public WidgetCfg clock = new WidgetCfg(false, 0.0, 0.55);
    public WidgetCfg session = new WidgetCfg(false, 0.0, 0.60);

    // Keystrokes, Ruestung, Effekte
    public WidgetCfg keys = new WidgetCfg(true, 0.0, 0.42);
    public boolean keystrokesMouse = true;
    public boolean keystrokesSpace = true;
    public WidgetCfg armor = new WidgetCfg(true, 1.0, 0.35);
    public WidgetCfg effects = new WidgetCfg(true, 0.0, 1.0);

    // Zoom (Sichtfeld beim Zoomen, 30 = staerkster Zoom)
    public boolean zoomEnabled = true;
    public int zoomFov = 30;

    /** Crosshair-Farbe als ARGB inklusive Deckkraft. */
    public int color() {
        int pct = Math.max(0, Math.min(100, crosshairOpacity));
        int alpha = Math.round(pct * 2.55f);
        return (alpha << 24) | (crosshairRgb & 0xFFFFFF);
    }

    // ---- Listen ----
    public List<WidgetCfg> infoWidgets() {
        return List.of(fps, ping, coords, cps, frametime, chunk, direction, angles, speed, ram, handItem, clock, session);
    }

    public List<WidgetCfg> allWidgets() {
        List<WidgetCfg> all = new ArrayList<>(infoWidgets());
        all.add(keys);
        all.add(armor);
        all.add(effects);
        return all;
    }

    public boolean anyInfoEnabled() {
        for (WidgetCfg w : infoWidgets()) {
            if (w.enabled) {
                return true;
            }
        }
        return false;
    }

    /** Schaltet alle Info-Anzeigen aus oder auf ihre Standardauswahl ein. */
    public void setInfoEnabled(boolean on) {
        List<WidgetCfg> mine = infoWidgets();
        List<WidgetCfg> defaults = new TokiaConfig().infoWidgets();
        for (int i = 0; i < mine.size(); i++) {
            mine.get(i).enabled = on && defaults.get(i).enabled;
        }
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

    public void resetInfoAll() {
        List<WidgetCfg> mine = infoWidgets();
        List<WidgetCfg> defaults = new TokiaConfig().infoWidgets();
        for (int i = 0; i < mine.size(); i++) {
            mine.get(i).resetStyle();
            mine.get(i).enabled = defaults.get(i).enabled;
        }
    }

    public void resetKeystrokes() {
        TokiaConfig d = new TokiaConfig();
        keys.resetStyle();
        keys.enabled = d.keys.enabled;
        keystrokesMouse = d.keystrokesMouse;
        keystrokesSpace = d.keystrokesSpace;
    }

    public void resetArmor() {
        armor.resetStyle();
        armor.enabled = new TokiaConfig().armor.enabled;
    }

    public void resetEffects() {
        effects.resetStyle();
        effects.enabled = new TokiaConfig().effects.enabled;
    }

    public void resetZoom() {
        TokiaConfig d = new TokiaConfig();
        zoomEnabled = d.zoomEnabled;
        zoomFov = d.zoomFov;
    }

    public void resetPositions() {
        List<WidgetCfg> mine = allWidgets();
        List<WidgetCfg> defaults = new TokiaConfig().allWidgets();
        for (int i = 0; i < mine.size(); i++) {
            mine.get(i).x = defaults.get(i).x;
            mine.get(i).y = defaults.get(i).y;
        }
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
