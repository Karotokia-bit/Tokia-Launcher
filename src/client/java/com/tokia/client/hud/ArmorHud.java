package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Zeigt Haltbarkeit der getragenen Ruestung am rechten Bildschirmrand. */
public final class ArmorHud {
    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private ArmorHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(TokiaMod.id("armor"), ArmorHud::render);
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!TokiaConfig.get().armorHud || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        int lineH = mc.font.lineHeight + 3;
        int y = g.guiHeight() / 2 - 20;
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
            int total = mc.font.width(name) + mc.font.width(pct);
            int x = g.guiWidth() - total - 6;
            g.fill(x - 3, y - 2, g.guiWidth() - 3, y + mc.font.lineHeight + 1, 0x80000000);
            g.text(mc.font, name, x, y, 0xFFFFFFFF, true);
            if (!pct.isEmpty()) {
                g.text(mc.font, pct, x + mc.font.width(name), y, pctColor, true);
            }
            y += lineH;
        }
    }
}
