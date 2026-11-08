package com.tokia.client.util;

import com.tokia.client.config.TokiaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Einfache Waypoints: setzen, wechseln, loeschen. Die Anzeige zeigt Entfernung und Richtung zum aktiven Waypoint. */
public final class Waypoints {
    // vorne, vorne-rechts, rechts, hinten-rechts, hinten, hinten-links, links, vorne-links
    private static final String[] ARROWS = {"^", "^>", ">", "v>", "v", "<v", "<", "<^"};

    private Waypoints() {
    }

    public static void add(Minecraft mc) {
        if (mc.player == null) {
            return;
        }
        TokiaConfig c = TokiaConfig.get();
        c.waypoints.add(new TokiaConfig.Waypoint("WP" + (c.waypoints.size() + 1),
            mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        c.activeWaypoint = c.waypoints.size() - 1;
        c.waypoint.enabled = true;
        TokiaConfig.save();
    }

    public static void next() {
        TokiaConfig c = TokiaConfig.get();
        if (c.waypoints.isEmpty()) {
            return;
        }
        c.activeWaypoint = (c.activeWaypoint + 1) % c.waypoints.size();
        TokiaConfig.save();
    }

    public static void deleteActive() {
        TokiaConfig c = TokiaConfig.get();
        if (c.waypoints.isEmpty()) {
            return;
        }
        int idx = Math.max(0, Math.min(c.waypoints.size() - 1, c.activeWaypoint));
        c.waypoints.remove(idx);
        c.activeWaypoint = Math.max(0, Math.min(c.waypoints.size() - 1, idx));
        TokiaConfig.save();
    }

    public static String text(Minecraft mc) {
        TokiaConfig c = TokiaConfig.get();
        if (c.waypoints.isEmpty()) {
            return Component.translatable("tokia.waypoint.none").getString();
        }
        int idx = Math.max(0, Math.min(c.waypoints.size() - 1, c.activeWaypoint));
        TokiaConfig.Waypoint wp = c.waypoints.get(idx);
        double dx = wp.x - mc.player.getX();
        double dy = wp.y - mc.player.getY();
        double dz = wp.z - mc.player.getZ();
        long dist = Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        double diff = ((targetYaw - mc.player.getYRot()) % 360.0 + 540.0) % 360.0 - 180.0;
        int sector = Math.floorMod((int) Math.round(diff / 45.0), 8);
        return wp.name + ": " + dist + " m " + ARROWS[sector];
    }
}
