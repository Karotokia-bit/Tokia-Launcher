package com.tokia.client.util;

import com.tokia.client.config.TokiaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

/**
 * Zoom: Solange die Zoom-Taste gehalten wird, wird das Sichtfeld (FOV) sanft verkleinert.
 * Danach wird das urspruengliche FOV wiederhergestellt.
 */
public final class ZoomHandler {
    private static boolean zooming;
    private static int saved;

    private ZoomHandler() {
    }

    public static void tick(Minecraft mc, boolean keyDown) {
        if (mc.player == null) {
            reset(mc);
            return;
        }
        TokiaConfig cfg = TokiaConfig.get();
        OptionInstance<Integer> fov = mc.options.fov();
        boolean active = keyDown && cfg.zoomEnabled;
        if (active && !zooming) {
            saved = fov.get();
            zooming = true;
        }
        if (!zooming) {
            return;
        }
        int goal = active ? Math.max(30, Math.min(110, cfg.zoomFov)) : saved;
        int cur = fov.get();
        if (cur != goal) {
            int step = Math.max(1, Math.abs(cur - goal) / 3);
            int next = cur > goal ? Math.max(goal, cur - step) : Math.min(goal, cur + step);
            fov.set(next);
        }
        if (!active && fov.get() == saved) {
            zooming = false;
        }
    }

    /** Stellt das urspruengliche FOV sofort wieder her (z. B. beim Beenden des Spiels). */
    public static void reset(Minecraft mc) {
        if (zooming) {
            mc.options.fov().set(saved);
            zooming = false;
        }
    }
}
