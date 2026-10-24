package com.tokia.client.hud;

import com.tokia.client.config.TokiaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/** Ersetzt das Vanilla-Crosshair durch ein anpassbares. Ist es ausgeschaltet, wird das Vanilla-Crosshair gezeichnet. */
public final class CrosshairHud {
    private CrosshairHud() {
    }

    public static void register() {
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, deltaTracker) -> {
            TokiaConfig cfg = TokiaConfig.get();
            if (!cfg.crosshairEnabled) {
                original.extractRenderState(graphics, deltaTracker);
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui.hud.isHidden() || !mc.options.getCameraType().isFirstPerson()) {
                return;
            }
            draw(graphics, cfg);
        });
    }

    private static void draw(GuiGraphicsExtractor g, TokiaConfig cfg) {
        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        int size = Math.max(1, cfg.crosshairSize);
        int t = Math.max(1, cfg.crosshairThickness);
        int gap = Math.max(0, cfg.crosshairGap);
        int half = t / 2;

        List<int[]> rects = new ArrayList<>(); // {x1, y1, x2, y2}
        switch (Math.floorMod(cfg.crosshairStyle, TokiaConfig.STYLE_COUNT)) {
            case 0 -> { // Kreuz
                int a = cy - half;
                int c = cx - half;
                rects.add(new int[] {cx - gap - size, a, cx - gap, a + t});
                rects.add(new int[] {cx + gap, a, cx + gap + size, a + t});
                rects.add(new int[] {c, cy - gap - size, c + t, cy - gap});
                rects.add(new int[] {c, cy + gap, c + t, cy + gap + size});
            }
            case 1 -> { // Punkt
                int d = Math.max(1, size / 2);
                rects.add(new int[] {cx - d / 2, cy - d / 2, cx - d / 2 + d, cy - d / 2 + d});
            }
            case 2 -> { // Kreis
                int steps = Math.max(16, size * 8);
                for (int i = 0; i < steps; i++) {
                    double ang = 2 * Math.PI * i / steps;
                    int px = cx + (int) Math.round(size * Math.cos(ang)) - half;
                    int py = cy + (int) Math.round(size * Math.sin(ang)) - half;
                    rects.add(new int[] {px, py, px + t, py + t});
                }
            }
            default -> { // Quadrat
                int x1 = cx - size;
                int x2 = cx + size + 1;
                int y1 = cy - size;
                int y2 = cy + size + 1;
                rects.add(new int[] {x1, y1, x2, y1 + t});
                rects.add(new int[] {x1, y2 - t, x2, y2});
                rects.add(new int[] {x1, y1, x1 + t, y2});
                rects.add(new int[] {x2 - t, y1, x2, y2});
            }
        }

        if (cfg.crosshairOutline) {
            for (int[] r : rects) {
                g.fill(r[0] - 1, r[1] - 1, r[2] + 1, r[3] + 1, 0xFF000000);
            }
        }
        int color = cfg.color();
        for (int[] r : rects) {
            g.fill(r[0], r[1], r[2], r[3], color);
        }
    }
}
