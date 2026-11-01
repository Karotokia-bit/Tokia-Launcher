package com.tokia.client.util;

/** Misst FPS und Frametime. frame() wird einmal pro gezeichnetem Bild aufgerufen. */
public final class FrameStats {
    private static int frames;
    private static int fps;
    private static long lastSecond = System.nanoTime();
    private static long lastFrame = System.nanoTime();
    private static double frameMs;

    private FrameStats() {
    }

    public static void frame() {
        long now = System.nanoTime();
        double ms = (now - lastFrame) / 1_000_000.0;
        lastFrame = now;
        frameMs = frameMs * 0.9 + ms * 0.1;
        frames++;
        if (now - lastSecond >= 1_000_000_000L) {
            fps = frames;
            frames = 0;
            lastSecond = now;
        }
    }

    public static int fps() {
        return fps;
    }

    public static double frameMs() {
        return frameMs;
    }
}
