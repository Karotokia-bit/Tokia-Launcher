package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Blinkende Warnung in der Bildschirmmitte, wenn Ruestung oder Werkzeug fast kaputt sind. */
public final class AlertHud {
    private static final EquipmentSlot[] ARMOR = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private AlertHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(TokiaMod.id("alerts"), AlertHud::render);
    }

    private static int percent(ItemStack s) {
        if (s.isEmpty() || !s.isDamageableItem() || s.getMaxDamage() <= 0) {
            return 100;
        }
        int max = s.getMaxDamage();
        return (int) Math.round(100.0 * (max - s.getDamageValue()) / max);
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        TokiaConfig cfg = TokiaConfig.get();
        if (mc.player == null || mc.gui.hud.isHidden() || (!cfg.warnArmor && !cfg.warnTool)) {
            return;
        }
        List<String> msgs = new ArrayList<>();
        if (cfg.warnArmor) {
            for (EquipmentSlot slot : ARMOR) {
                ItemStack s = mc.player.getItemBySlot(slot);
                int p = percent(s);
                if (p <= cfg.warnPercent) {
                    msgs.add(Component.translatable("tokia.alert.armor").getString() + ": "
                        + s.getHoverName().getString() + " " + p + "%");
                }
            }
        }
        if (cfg.warnTool) {
            ItemStack s = mc.player.getItemBySlot(EquipmentSlot.MAINHAND);
            int p = percent(s);
            if (p <= cfg.warnPercent) {
                msgs.add(Component.translatable("tokia.alert.tool").getString() + ": "
                    + s.getHoverName().getString() + " " + p + "%");
            }
        }
        if (msgs.isEmpty()) {
            return;
        }
        int color = (System.currentTimeMillis() / 500) % 2 == 0 ? 0xFFFF5555 : 0xFFFFAA00;
        int y = g.guiHeight() / 4;
        for (String m : msgs) {
            g.text(mc.font, m, (g.guiWidth() - mc.font.width(m)) / 2, y, color, true);
            y += mc.font.lineHeight + 3;
        }
    }
}
