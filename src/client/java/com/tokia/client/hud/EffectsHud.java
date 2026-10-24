package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Zeigt aktive Traenke/Effekte mit Restdauer unten links. */
public final class EffectsHud {
    private EffectsHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(TokiaMod.id("effects"), EffectsHud::render);
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!TokiaConfig.get().effectsHud || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        List<String> lines = new ArrayList<>();
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
        if (lines.isEmpty()) {
            return;
        }
        int lineH = mc.font.lineHeight + 3;
        int y = g.guiHeight() - 6 - lines.size() * lineH;
        for (String l : lines) {
            int w = mc.font.width(l);
            g.fill(4, y - 2, 4 + w + 6, y + mc.font.lineHeight + 1, 0x80000000);
            g.text(mc.font, l, 7, y, 0xFFFFFFFF, true);
            y += lineH;
        }
    }
}
