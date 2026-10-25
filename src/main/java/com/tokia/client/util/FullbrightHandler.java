package com.tokia.client.util;

import com.tokia.client.config.TokiaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

/**
 * Maximale Helligkeit: Solange aktiv, wird die Helligkeit auf Maximum gestellt.
 * Beim Ausschalten oder Beenden wird der urspruengliche Wert wiederhergestellt.
 * (Mehr als das Spiel-Maximum ist ohne Mixin nicht moeglich.)
 */
public final class FullbrightHandler {
    private static boolean active;
    private static double saved;

    private FullbrightHandler() {
    }

    public static void tick(Minecraft mc) {
        OptionInstance<Double> gamma = mc.options.gamma();
        boolean want = TokiaConfig.get().fullbright;
        if (want && !active) {
            saved = gamma.get();
            active = true;
        }
        if (want) {
            if (gamma.get() < 1.0) {
                gamma.set(1.0);
            }
        } else if (active) {
            gamma.set(saved);
            active = false;
        }
    }

    public static void reset(Minecraft mc) {
        if (active) {
            mc.options.gamma().set(saved);
            active = false;
        }
    }
}
