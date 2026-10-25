package com.tokia.client.util;

/** Sammelt kleine Statistiken: Laufgeschwindigkeit und Spielzeit seit dem Start. */
public final class PlayerStats {
    public static final long SESSION_START = System.currentTimeMillis();

    private static boolean hasLast;
    private static double lastX;
    private static double lastZ;
    private static double speed;

    private PlayerStats() {
    }

    /** Einmal pro Tick aufrufen, solange ein Spieler existiert. */
    public static void tick(double x, double z) {
        if (hasLast) {
            double dist = Math.hypot(x - lastX, z - lastZ) * 20.0; // Bloecke pro Sekunde
            speed = speed * 0.8 + dist * 0.2;
        }
        lastX = x;
        lastZ = z;
        hasLast = true;
    }

    public static void reset() {
        hasLast = false;
        speed = 0;
    }

    /** Geglaettete horizontale Geschwindigkeit in Bloecken pro Sekunde. */
    public static double speed() {
        return speed;
    }
}
