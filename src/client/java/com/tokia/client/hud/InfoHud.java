package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.config.WidgetCfg;
import com.tokia.client.util.ClickCounter;
import com.tokia.client.util.FrameStats;
import com.tokia.client.util.PlayerStats;
import com.tokia.client.util.Stopwatch;
import com.tokia.client.util.Waypoints;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/** Alle Info-Anzeigen. Jede ist ein eigenes, einzeln verschiebbares und gestaltbares Element. */
public final class InfoHud {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    /** Beschreibung einer einzelnen Anzeige. */
    public static final class Def {
        public final String nameKey;
        public final String labelKey;
        public final Function<TokiaConfig, WidgetCfg> cfg;
        final Function<Minecraft, String> value;
        /** Bewertung 0 = gut, 1 = mittel, 2 = schlecht, -1 = keine; null = Anzeige kennt keine Bewertung. */
        final Function<Minecraft, Integer> quality;

        Def(String id, Function<TokiaConfig, WidgetCfg> cfg, Function<Minecraft, String> value) {
            this(id, cfg, value, null);
        }

        Def(String id, Function<TokiaConfig, WidgetCfg> cfg, Function<Minecraft, String> value,
            Function<Minecraft, Integer> quality) {
            this.nameKey = "tokia.widget." + id;
            this.labelKey = "tokia.label." + id;
            this.cfg = cfg;
            this.value = value;
            this.quality = quality;
        }

        public boolean supportsDynamicColor() {
            return quality != null;
        }
    }

    public static final List<Def> DEFS = List.of(
        new Def("fps", c -> c.fps, mc -> String.valueOf(FrameStats.fps()), mc -> {
            int f = FrameStats.fps();
            return f >= 60 ? 0 : f >= 30 ? 1 : 2;
        }),
        new Def("ping", c -> c.ping, InfoHud::ping, mc -> {
            int p = pingMs(mc);
            return p < 0 ? -1 : p <= 80 ? 0 : p <= 150 ? 1 : 2;
        }),
        new Def("coords", c -> c.coords, mc -> String.format(Locale.ROOT, "%.1f / %.1f / %.1f",
            mc.player.getX(), mc.player.getY(), mc.player.getZ())),
        new Def("cps", c -> c.cps, mc -> ClickCounter.left() + " | " + ClickCounter.right()),
        new Def("frametime", c -> c.frametime, mc -> String.format(Locale.ROOT, "%.1f ms", FrameStats.frameMs()), mc -> {
            double ms = FrameStats.frameMs();
            return ms <= 16.7 ? 0 : ms <= 33.4 ? 1 : 2;
        }),
        new Def("chunk", c -> c.chunk, mc -> Math.floorDiv((int) Math.floor(mc.player.getX()), 16) + ", "
            + Math.floorDiv((int) Math.floor(mc.player.getZ()), 16)),
        new Def("direction", c -> c.direction, mc -> direction(mc.player.getYRot())),
        new Def("angles", c -> c.angles, mc -> String.format(Locale.ROOT, "%.1f / %.1f",
            wrap(mc.player.getYRot()), mc.player.getXRot())),
        new Def("speed", c -> c.speed, mc -> String.format(Locale.ROOT, "%.1f b/s", PlayerStats.speed())),
        new Def("ram", c -> c.ram, InfoHud::ram),
        new Def("handitem", c -> c.handItem, InfoHud::handItem),
        new Def("clock", c -> c.clock, mc -> LocalTime.now().format(CLOCK)),
        new Def("session", c -> c.session, InfoHud::session),
        new Def("stopwatch", c -> c.stopwatch, mc -> Stopwatch.format()),
        new Def("waypoint", c -> c.waypoint, Waypoints::text)
    );

    private InfoHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(TokiaMod.id("info"), InfoHud::render);
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    private static double wrap(float yaw) {
        double d = yaw % 360.0;
        if (d > 180.0) {
            d -= 360.0;
        }
        if (d < -180.0) {
            d += 360.0;
        }
        return d;
    }

    private static String direction(float yaw) {
        double deg = ((yaw % 360.0) + 360.0) % 360.0;
        int idx = (int) Math.round(deg / 45.0) % 8;
        return tr("tokia.dir." + idx);
    }

    private static int pingMs(Minecraft mc) {
        int ping = -1;
        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            PlayerInfo info = conn.getPlayerInfo(mc.player.getUUID());
            if (info != null) {
                ping = info.getLatency();
            }
        }
        return ping;
    }

    private static String ping(Minecraft mc) {
        int ping = pingMs(mc);
        return ping >= 0 ? ping + " ms" : "-";
    }

    private static String ram(Minecraft mc) {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long max = rt.maxMemory() / (1024 * 1024);
        return used + "/" + max + " MB";
    }

    private static String handItem(Minecraft mc) {
        ItemStack stack = mc.player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (stack.isEmpty()) {
            return "-";
        }
        String name = stack.getHoverName().getString();
        return stack.getCount() > 1 ? name + " x" + stack.getCount() : name;
    }

    private static String session(Minecraft mc) {
        long s = (System.currentTimeMillis() - PlayerStats.SESSION_START) / 1000;
        return String.format(Locale.ROOT, "%d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
    }

    /** Anzeigetext einer Anzeige, mit oder ohne Beschriftung. */
    public static String text(Def d, Minecraft mc, WidgetCfg c) {
        String v = mc.player == null ? "-" : d.value.apply(mc);
        return c.label ? tr(d.labelKey) + ": " + v : v;
    }

    /** Groesse {Breite, Hoehe} einer Anzeige; auch fuer den HUD-Editor. */
    public static int[] size(Def d, Minecraft mc) {
        WidgetCfg c = d.cfg.apply(TokiaConfig.get());
        return new int[] {mc.font.width(text(d, mc, c)) + 6, mc.font.lineHeight + 4};
    }

    /** Textfarbe einer Anzeige: bei "Farbe nach Wert" gruen/gelb/rot, sonst die gewaehlte Farbe. */
    public static int colorFor(Def d, Minecraft mc, WidgetCfg c) {
        if (c.dynamicColor && d.quality != null && mc.player != null) {
            int q = d.quality.apply(mc);
            if (q == 0) {
                return 0xFF55FF55;
            }
            if (q == 1) {
                return 0xFFFFFF55;
            }
            if (q == 2) {
                return 0xFFFF5555;
            }
        }
        return c.textColor();
    }

    /** Zeichnet eine Anzeige mit ihrem Stil. Wird auch fuer die Vorschau im Menue genutzt. */
    public static void drawBox(GuiGraphicsExtractor g, Minecraft mc, WidgetCfg c, String text, int color, int x, int y) {
        int w = mc.font.width(text) + 6;
        int h = mc.font.lineHeight + 4;
        if (c.background) {
            g.fill(x, y, x + w, y + h, c.bgColor());
        }
        g.text(mc.font, text, x + 3, y + 2, color, c.shadow);
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        FrameStats.frame();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        TokiaConfig cfg = TokiaConfig.get();
        for (Def d : DEFS) {
            WidgetCfg c = d.cfg.apply(cfg);
            if (!c.enabled) {
                continue;
            }
            String text = text(d, mc, c);
            int w = mc.font.width(text) + 6;
            int h = mc.font.lineHeight + 4;
            int x = HudLayout.x(c.x, w, g.guiWidth());
            int y = HudLayout.y(c.y, h, g.guiHeight());
            drawBox(g, mc, c, text, colorFor(d, mc, c), x, y);
        }
    }
}
