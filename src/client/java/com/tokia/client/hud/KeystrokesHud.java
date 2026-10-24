package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.util.ClickCounter;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Zeigt WASD, Leertaste und Maustasten (mit CPS) am linken Bildschirmrand. */
public final class KeystrokesHud {
    private static final int BOX = 18;
    private static final int GAP = 2;

    private KeystrokesHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(TokiaMod.id("keystrokes"), KeystrokesHud::render);
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!TokiaConfig.get().keystrokes || mc.player == null || mc.options.hideGui) {
            return;
        }
        int step = BOX + GAP;
        int x = 6;
        int y = g.guiHeight() / 2 - 30;

        key(g, mc, "W", x + step, y, BOX, BOX, mc.options.keyUp.isDown());
        key(g, mc, "A", x, y + step, BOX, BOX, mc.options.keyLeft.isDown());
        key(g, mc, "S", x + step, y + step, BOX, BOX, mc.options.keyDown.isDown());
        key(g, mc, "D", x + 2 * step, y + step, BOX, BOX, mc.options.keyRight.isDown());
        int mouseW = (3 * BOX + 2 * GAP - GAP) / 2;
        key(g, mc, "L " + ClickCounter.left(), x, y + 2 * step, mouseW, BOX, mc.options.keyAttack.isDown());
        key(g, mc, "R " + ClickCounter.right(), x + mouseW + GAP, y + 2 * step, mouseW, BOX, mc.options.keyUse.isDown());
        key(g, mc, "___", x, y + 3 * step, 3 * BOX + 2 * GAP, 12, mc.options.keyJump.isDown());
    }

    private static void key(GuiGraphicsExtractor g, Minecraft mc, String label, int x, int y, int w, int h, boolean down) {
        g.fill(x, y, x + w, y + h, down ? 0xB0FFFFFF : 0x80000000);
        int color = down ? 0xFF000000 : 0xFFFFFFFF;
        int tx = x + (w - mc.font.width(label)) / 2;
        int ty = y + (h - mc.font.lineHeight) / 2 + 1;
        g.text(mc.font, label, tx, ty, color, false);
    }
}
