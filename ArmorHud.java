package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.config.WidgetCfg;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Zeigt die Haltbarkeit der getragenen Ruestung. */
public final class ArmorHud {
    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private record Line(String name, String pct, int pctColor) {
    }

    private ArmorHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(TokiaMod.id("armor"), ArmorHud::render);
    }

    private static List<Line> lines(Minecraft mc) {
        List<Line> out = new ArrayList<>();
        if (mc.player == null) {
            return out;
        }
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            String name = stack.getHoverName().getString();
            String pct = "";
            int pctColor = 0xFF55FF55;
            if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
                int max = stack.getMaxDamage();
                int percent = (int) Math.round(100.0 * (max - stack.getDamageValue()) / max);
                pct = " " + percent + "%";
                pctColor = percent > 50 ? 0xFF55FF55 : percent > 25 ? 0xFFFFFF55 : 0xFFFF5555;
            }
            out.add(new Line(name, pct, pctColor));
        }
        return out;
    }

    /** Groesse {Breite, Hoehe}; auch fuer den HUD-Editor. */
    public static int[] size(Minecraft mc) {
        List<Line> lines = lines(mc);
        if (lines.isEmpty()) {
            return new int[] {90, 12};
        }
        int w = 0;
        for (Line l : lines) {
            w = Math.max(w, mc.font.width(l.name()) + mc.font.width(l.pct()));
        }
        return new int[] {w + 8, lines.size() * (mc.font.lineHeight + 3)};
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        WidgetCfg c = TokiaConfig.get().armor;
        if (!c.enabled || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        List<Line> lines = lines(mc);
        if (lines.isEmpty()) {
            return;
        }
        int[] s = size(mc);
        int x = HudLayout.x(c.x, s[0], g.guiWidth());
        int y = HudLayout.y(c.y, s[1], g.guiHeight());
        int lineH = mc.font.lineHeight + 3;
        for (int i = 0; i < lines.size(); i++) {
            Line l = lines.get(i);
            int ly = y + i * lineH;
            if (c.background) {
                g.fill(x, ly, x + s[0], ly + lineH - 1, c.bgColor());
            }
            g.text(mc.font, l.name(), x + 4, ly + 2, c.textColor(), c.shadow);
            if (!l.pct().isEmpty()) {
                g.text(mc.font, l.pct(), x + 4 + mc.font.width(l.name()), ly + 2, l.pctColor(), c.shadow);
            }
        }
    }
}
