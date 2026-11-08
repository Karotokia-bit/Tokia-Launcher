package com.tokia.client.util;

import java.util.Locale;

/** Einfache Stoppuhr (Start/Stopp und Zuruecksetzen per Taste). */
public final class Stopwatch {
    private static boolean running;
    private static long startedAt;
    private static long accumulated;

    private Stopwatch() {
    }

    public static void toggle() {
        long now = System.currentTimeMillis();
        if (running) {
            accumulated += now - startedAt;
            running = false;
        } else {
            startedAt = now;
            running = true;
        }
    }

    public static void reset() {
        running = false;
        accumulated = 0;
    }

    public static long elapsedMs() {
        return accumulated + (running ? System.currentTimeMillis() - startedAt : 0);
    }

    public static String format() {
        long ms = elapsedMs();
        long tenth = (ms / 100) % 10;
        long sec = (ms / 1000) % 60;
        long min = (ms / 60000) % 60;
        long hour = ms / 3600000;
        if (hour > 0) {
            return String.format(Locale.ROOT, "%d:%02d:%02d", hour, min, sec);
        }
        return String.format(Locale.ROOT, "%02d:%02d.%d", min, sec, tenth);
    }
}
