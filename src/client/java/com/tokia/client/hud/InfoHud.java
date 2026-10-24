package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.util.ClickCounter;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** FPS, Ping, Koordinaten, CPS und Uhrzeit in einer Ecke des Bildschirms. */
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
        if (!cfg.infoEnabled || mc.player == null || mc.options.hideGui) {
            return;
        }

        List<String> lines = new ArrayList<>();
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
        if (cfg.showCps) {
            lines.add("CPS: " + ClickCounter.left() + " | " + ClickCounter.right());
        }
        if (cfg.showClock) {
            lines.add(LocalTime.now().format(CLOCK));
        }
        if (lines.isEmpty()) {
            return;
        }

        int lineH = mc.font.lineHeight + 2;
        int w = 0;
        for (String l : lines) {
            w = Math.max(w, mc.font.width(l));
        }
        w += 6;
        int h = lines.size() * lineH + 2;

        int x = switch (Math.floorMod(cfg.infoCorner, TokiaConfig.CORNER_COUNT)) {
            case 1, 3 -> g.guiWidth() - w - 4;
            default -> 4;
        };
        int y = switch (Math.floorMod(cfg.infoCorner, TokiaConfig.CORNER_COUNT)) {
            case 2, 3 -> g.guiHeight() - h - 4;
            default -> 4;
        };

        g.fill(x, y, x + w, y + h, 0x80000000);
        for (int i = 0; i < lines.size(); i++) {
            g.text(mc.font, lines.get(i), x + 3, y + 2 + i * lineH, 0xFFFFFFFF, true);
        }
    }
}
