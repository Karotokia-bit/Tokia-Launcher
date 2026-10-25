package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Zaehlt wichtige Items im Inventar (Goldaepfel, Enderperlen, Traenke, Pfeile). */
public final class ItemCounterHud {
    private record Line(String text, int color) {
    }

    private ItemCounterHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(TokiaMod.id("items"), ItemCounterHud::render);
    }

    private static int count(Inventory inv, Item item) {
        int n = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(item)) {
                n += s.getCount();
            }
        }
        return n;
    }

    private static void add(List<Line> out, Inventory inv, boolean on, Item item, String key, int color) {
        if (!on) {
            return;
        }
        int n = count(inv, item);
        if (n > 0) {
            out.add(new Line(Component.translatable(key).getString() + ": " + n, color));
        }
    }

    private static List<Line> lines(Minecraft mc, TokiaConfig cfg) {
        List<Line> out = new ArrayList<>();
        if (mc.player == null) {
            return out;
        }
        Inventory inv = mc.player.getInventory();
        add(out, inv, cfg.itemGapples, Items.GOLDEN_APPLE, "tokia.item.gapple", 0xFFFFD955);
        add(out, inv, cfg.itemPearls, Items.ENDER_PEARL, "tokia.item.pearl", 0xFF55FFFF);
        add(out, inv, cfg.itemPots, Items.SPLASH_POTION, "tokia.item.pot", 0xFFFF5555);
        add(out, inv, cfg.itemArrows, Items.ARROW, "tokia.item.arrow", 0xFFCCCCCC);
        return out;
    }

    /** Groesse {Breite, Hoehe}; auch fuer den HUD-Editor. */
    public static int[] size(Minecraft mc) {
        List<Line> lines = lines(mc, TokiaConfig.get());
        if (lines.isEmpty()) {
            return new int[] {90, 12};
        }
        int w = 0;
        for (Line l : lines) {
            w = Math.max(w, mc.font.width(l.text()));
        }
        return new int[] {w + 8, lines.size() * (mc.font.lineHeight + 3)};
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        TokiaConfig cfg = TokiaConfig.get();
        if (!cfg.itemCounter || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        List<Line> lines = lines(mc, cfg);
        if (lines.isEmpty()) {
            return;
        }
        int[] s = size(mc);
        int x = HudLayout.x(cfg.itemsX, s[0], g.guiWidth());
        int y = HudLayout.y(cfg.itemsY, s[1], g.guiHeight());
        int lineH = mc.font.lineHeight + 3;
        for (int i = 0; i < lines.size(); i++) {
            int ly = y + i * lineH;
            g.fill(x, ly, x + s[0], ly + lineH - 1, 0x80000000);
            g.text(mc.font, lines.get(i).text(), x + 4, ly + 2, lines.get(i).color(), true);
        }
    }
}
