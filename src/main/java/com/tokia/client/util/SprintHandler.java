package com.tokia.client.util;

import com.tokia.client.config.TokiaConfig;
import net.minecraft.client.Minecraft;

/** Toggle Sprint: Solange vorwaerts gelaufen wird, wird automatisch gesprintet. */
public final class SprintHandler {
    private static boolean forced;

    private SprintHandler() {
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null || !TokiaConfig.get().toggleSprint) {
            release(mc);
            return;
        }
        if (mc.options.keyUp.isDown()) {
            mc.options.keySprint.setDown(true);
            forced = true;
        } else {
            release(mc);
        }
    }

    private static void release(Minecraft mc) {
        if (forced) {
            mc.options.keySprint.setDown(false);
            forced = false;
        }
    }
}
