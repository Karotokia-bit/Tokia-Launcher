package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.config.WidgetCfg;
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
        WidgetCfg c = cfg.keys;
        if (!c.enabled || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        int[] s = size();
        int step = BOX + GAP;
        int x = HudLayout.x(c.x, s[0], g.guiWidth());
        int y = HudLayout.y(c.y, s[1], g.guiHeight());

        key(g, mc, c, "W", x + step, y, BOX, BOX, mc.options.keyUp.isDown());
        key(g, mc, c, "A", x, y + step, BOX, BOX, mc.options.keyLeft.isDown());
        key(g, mc, c, "S", x + step, y + step, BOX, BOX, mc.options.keyDown.isDown());
        key(g, mc, c, "D", x + 2 * step, y + step, BOX, BOX, mc.options.keyRight.isDown());
        int next = 2;
        if (cfg.keystrokesMouse) {
            int mouseW = (3 * BOX + 2 * GAP - GAP) / 2;
            key(g, mc, c, "L " + ClickCounter.left(), x, y + next * step, mouseW, BOX, mc.options.keyAttack.isDown());
            key(g, mc, c, "R " + ClickCounter.right(), x + mouseW + GAP, y + next * step, mouseW, BOX, mc.options.keyUse.isDown());
            next++;
        }
        if (cfg.keystrokesSpace) {
            key(g, mc, c, "___", x, y + next * step, 3 * BOX + 2 * GAP, 12, mc.options.keyJump.isDown());
        }
    }

    private static void key(GuiGraphicsExtractor g, Minecraft mc, WidgetCfg c, String label,
                            int x, int y, int w, int h, boolean down) {
        int rgb = c.rgb();
        if (down) {
            g.fill(x, y, x + w, y + h, 0xB0000000 | rgb);
        } else if (c.background) {
            g.fill(x, y, x + w, y + h, c.bgColor());
        }
        int bright = ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF);
        int color = down ? (bright > 300 ? 0xFF000000 : 0xFFFFFFFF) : c.textColor();
        int tx = x + (w - mc.font.width(label)) / 2;
        int ty = y + (h - mc.font.lineHeight) / 2 + 1;
        g.text(mc.font, label, tx, ty, color, false);
    }
}
