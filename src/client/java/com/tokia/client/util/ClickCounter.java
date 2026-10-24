package com.tokia.client.util;

import java.util.ArrayDeque;

/**
 * Zaehlt Klicks pro Sekunde (CPS). Die Tasten werden einmal pro Tick (20x/s) abgefragt,
 * daher ist die Aufloesung auf maximal 20 Klicks pro Sekunde und Taste begrenzt.
 */
public final class ClickCounter {
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();
    private static boolean leftWasDown;
    private static boolean rightWasDown;

    private ClickCounter() {
    }

    public static void tick(boolean leftDown, boolean rightDown) {
        long now = System.currentTimeMillis();
        if (leftDown && !leftWasDown) {
            LEFT.addLast(now);
        }
        if (rightDown && !rightWasDown) {
            RIGHT.addLast(now);
        }
        leftWasDown = leftDown;
        rightWasDown = rightDown;
    }

    public static int left() {
        return count(LEFT);
    }

    public static int right() {
        return count(RIGHT);
    }

    private static int count(ArrayDeque<Long> q) {
        long limit = System.currentTimeMillis() - 1000;
        while (!q.isEmpty() && q.peekFirst() < limit) {
            q.removeFirst();
        }
        return q.size();
    }
}
