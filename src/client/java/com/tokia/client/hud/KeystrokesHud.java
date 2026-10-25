package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.util.ClickCounter;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Zeigt WASD, Maustasten (mit CPS) und Leertaste. */
public final class KeystrokesHud {
    private static final int BOX = 18;
    private static final int GAP = 2;

    private KeystrokesHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(TokiaMod.id("keystrokes"), KeystrokesHud::render);
    }

    /** Groesse {Breite, Hoehe}; auch fuer den HUD-Editor. */
    public static int[] size() {
        TokiaConfig cfg = TokiaConfig.get();
        int step = BOX + GAP;
        int next = 2 + (cfg.keystrokesMouse ? 1 : 0);
        int h = cfg.keystrokesSpace ? next * step + 12 : next * step - GAP;
        return new int[] {3 * BOX + 2 * GAP, h};
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        TokiaConfig cfg = TokiaConfig.get();
        if (!cfg.keystrokes || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        int[] s = size();
        int step = BOX + GAP;
        int x = HudLayout.x(cfg.keysX, s[0], g.guiWidth());
        int y = HudLayout.y(cfg.keysY, s[1], g.guiHeight());

        key(g, mc, "W", x + step, y, BOX, BOX, mc.options.keyUp.isDown());
        key(g, mc, "A", x, y + step, BOX, BOX, mc.options.keyLeft.isDown());
        key(g, mc, "S", x + step, y + step, BOX, BOX, mc.options.keyDown.isDown());
        key(g, mc, "D", x + 2 * step, y + step, BOX, BOX, mc.options.keyRight.isDown());
        int next = 2;
        if (cfg.keystrokesMouse) {
            int mouseW = (3 * BOX + 2 * GAP - GAP) / 2;
            key(g, mc, "L " + ClickCounter.left(), x, y + next * step, mouseW, BOX, mc.options.keyAttack.isDown());
            key(g, mc, "R " + ClickCounter.right(), x + mouseW + GAP, y + next * step, mouseW, BOX, mc.options.keyUse.isDown());
            next++;
        }
        if (cfg.keystrokesSpace) {
            key(g, mc, "___", x, y + next * step, 3 * BOX + 2 * GAP, 12, mc.options.keyJump.isDown());
        }
    }

    private static void key(GuiGraphicsExtractor g, Minecraft mc, String label, int x, int y, int w, int h, boolean down) {
        g.fill(x, y, x + w, y + h, down ? 0xB0FFFFFF : 0x80000000);
        int color = down ? 0xFF000000 : 0xFFFFFFFF;
        int tx = x + (w - mc.font.width(label)) / 2;
        int ty = y + (h - mc.font.lineHeight) / 2 + 1;
        g.text(mc.font, label, tx, ty, color, false);
    }
}
