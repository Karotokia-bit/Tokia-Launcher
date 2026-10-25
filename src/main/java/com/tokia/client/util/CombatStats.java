package com.tokia.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;

/**
 * Merkt sich Reichweite und Combo.
 * Reach: Abstand zum anvisierten Gegner im Moment des Klicks.
 * Combo: Treffer in Folge, ohne selbst Schaden zu nehmen (zaehlt Klicks auf ein anvisiertes Ziel).
 */
public final class CombatStats {
    private static final long REACH_SHOW_MS = 5000;
    private static final long COMBO_TIMEOUT_MS = 3000;

    private static boolean attackWasDown;
    private static int lastHurtTime;
    private static double reach = -1;
    private static long reachAt;
    private static int combo;
    private static long comboAt;

    private CombatStats() {
    }

    /** Einmal pro Tick aufrufen. */
    public static void tick(Minecraft mc, boolean attackDown) {
        if (mc.player == null) {
            reset();
            return;
        }
        long now = System.currentTimeMillis();

        // Selbst getroffen worden: Combo ist vorbei
        int hurt = mc.player.hurtTime;
        if (hurt > lastHurtTime) {
            combo = 0;
        }
        lastHurtTime = hurt;

        if (attackDown && !attackWasDown) {
            HitResult hit = mc.hitResult;
            if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
                reach = hit.getLocation().distanceTo(mc.player.getEyePosition());
                reachAt = now;
                combo++;
                comboAt = now;
            }
        }
        attackWasDown = attackDown;

        if (combo > 0 && now - comboAt > COMBO_TIMEOUT_MS) {
            combo = 0;
        }
    }

    public static void reset() {
        attackWasDown = false;
        lastHurtTime = 0;
        reach = -1;
        combo = 0;
    }

    /** Letzte Reichweite in Bloecken oder -1, wenn laenger her. */
    public static double reach() {
        return reach >= 0 && System.currentTimeMillis() - reachAt <= REACH_SHOW_MS ? reach : -1;
    }

    public static int combo() {
        return combo;
    }
}
