package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.util.ClickCounter;
import com.tokia.client.util.PlayerStats;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** FPS, Ping, Koordinaten, CPS, Uhrzeit und Extras in einer frei verschiebbaren Anzeige. */
public final class InfoHud {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private static int frames;
    private static int fps;
    private static long lastNs = System.nanoTime();

    private InfoHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(TokiaMod.id("info"), InfoHud::render);
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    private static String direction(float yaw) {
        double deg = ((yaw % 360.0) + 360.0) % 360.0;
        int idx = (int) Math.round(deg / 45.0) % 8;
        return tr("tokia.dir." + idx);
    }

    static List<String> lines(Minecraft mc, TokiaConfig cfg) {
        List<String> lines = new ArrayList<>();
        if (mc.player == null) {
            return lines;
        }
        if (cfg.showFps) {
            lines.add("FPS: " + fps);
        }
        if (cfg.showPing) {
            int ping = -1;
            ClientPacketListener conn = mc.getConnection();
            if (conn != null) {
                PlayerInfo info = conn.getPlayerInfo(mc.player.getUUID());
                if (info != null) {
                    ping = info.getLatency();
                }
            }
            lines.add("Ping: " + (ping >= 0 ? ping + " ms" : "-"));
        }
        if (cfg.showCoords) {
            lines.add(String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
        if (cfg.showDirection) {
            lines.add(tr("tokia.hud.direction") + ": " + direction(mc.player.getYRot()));
        }
        if (cfg.showSpeed) {
            lines.add(String.format(Locale.ROOT, "%s: %.1f b/s", tr("tokia.hud.speed"), PlayerStats.speed()));
        }
        if (cfg.showCps) {
            lines.add("CPS: " + ClickCounter.left() + " | " + ClickCounter.right());
        }
        if (cfg.showRam) {
            Runtime rt = Runtime.getRuntime();
            long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
            long max = rt.maxMemory() / (1024 * 1024);
            lines.add("RAM: " + used + "/" + max + " MB");
        }
        if (cfg.showSession) {
            long s = (System.currentTimeMillis() - PlayerStats.SESSION_START) / 1000;
            lines.add(String.format(Locale.ROOT, "%s: %d:%02d:%02d", tr("tokia.hud.session"),
                s / 3600, (s / 60) % 60, s % 60));
        }
        if (cfg.showClock) {
            lines.add(LocalTime.now().format(CLOCK));
        }
        return lines;
    }

    /** Groesse {Breite, Hoehe} der Anzeige; auch fuer den HUD-Editor. */
    public static int[] size(Minecraft mc) {
        List<String> lines = lines(mc, TokiaConfig.get());
        if (lines.isEmpty()) {
            return new int[] {60, 12};
        }
        int w = 0;
        for (String l : lines) {
            w = Math.max(w, mc.font.width(l));
        }
        return new int[] {w + 6, lines.size() * (mc.font.lineHeight + 2) + 2};
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        // FPS selbst zaehlen: Frames pro Sekunde
        frames++;
        long now = System.nanoTime();
        if (now - lastNs >= 1_000_000_000L) {
            fps = frames;
            frames = 0;
            lastNs = now;
        }

        TokiaConfig cfg = TokiaConfig.get();
        Minecraft mc = Minecraft.getInstance();
        if (!cfg.infoEnabled || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        List<String> lines = lines(mc, cfg);
        if (lines.isEmpty()) {
            return;
        }
        int[] s = size(mc);
        int x = HudLayout.x(cfg.infoX, s[0], g.guiWidth());
        int y = HudLayout.y(cfg.infoY, s[1], g.guiHeight());
        int lineH = mc.font.lineHeight + 2;

        if (cfg.infoBackground) {
            g.fill(x, y, x + s[0], y + s[1], 0x80000000);
        }
        for (int i = 0; i < lines.size(); i++) {
            g.text(mc.font, lines.get(i), x + 3, y + 2 + i * lineH, 0xFFFFFFFF, true);
        }
    }
}
