package com.tokia.client.util;

import com.tokia.client.config.TokiaConfig;
import net.minecraft.client.Minecraft;

/** Auto-Sprint: Sprinten, solange die Vorwaerts-Taste gehalten wird. */
public final class SprintHelper {
    private static boolean forced;

    private SprintHelper() {
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null) {
            forced = false;
            return;
        }
        boolean want = TokiaConfig.get().autoSprint && mc.options.keyUp.isDown() && !mc.options.keyDown.isDown();
        if (want) {
            mc.options.keySprint.setDown(true);
            forced = true;
        } else if (forced) {
            mc.options.keySprint.setDown(false);
            forced = false;
        }
    }
}
