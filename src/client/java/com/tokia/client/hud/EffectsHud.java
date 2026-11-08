package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.config.WidgetCfg;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Zeigt aktive Traenke/Effekte mit Restdauer. */
public final class EffectsHud {
    private EffectsHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(TokiaMod.id("effects"), EffectsHud::render);
    }

    private static List<String> lines(Minecraft mc) {
        List<String> lines = new ArrayList<>();
        if (mc.player == null) {
            return lines;
        }
        for (MobEffectInstance e : mc.player.getActiveEffects()) {
            String name = e.getEffect().value().getDisplayName().getString();
            if (e.getAmplifier() > 0) {
                name += " " + (e.getAmplifier() + 1);
            }
            String time;
            if (e.isInfiniteDuration()) {
                time = "**:**";
            } else {
                int secs = e.getDuration() / 20;
                time = String.format(Locale.ROOT, "%d:%02d", secs / 60, secs % 60);
            }
            lines.add(name + " (" + time + ")");
        }
        return lines;
    }

    /** Groesse {Breite, Hoehe}; auch fuer den HUD-Editor. */
    public static int[] size(Minecraft mc) {
        List<String> lines = lines(mc);
        if (lines.isEmpty()) {
            return new int[] {90, 12};
        }
        int w = 0;
        for (String l : lines) {
            w = Math.max(w, mc.font.width(l));
        }
        return new int[] {w + 8, lines.size() * (mc.font.lineHeight + 3)};
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        WidgetCfg c = TokiaConfig.get().effects;
        if (!c.enabled || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        List<String> lines = lines(mc);
        if (lines.isEmpty()) {
            return;
        }
        int[] s = size(mc);
        int x = HudLayout.x(c.x, s[0], g.guiWidth());
        int y = HudLayout.y(c.y, s[1], g.guiHeight());
        int lineH = mc.font.lineHeight + 3;
        for (int i = 0; i < lines.size(); i++) {
            int ly = y + i * lineH;
            if (c.background) {
                g.fill(x, ly, x + s[0], ly + lineH - 1, c.bgColor());
            }
            g.text(mc.font, lines.get(i), x + 4, ly + 2, c.textColor(), c.shadow);
        }
    }
}
