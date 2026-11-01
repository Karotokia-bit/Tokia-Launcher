package com.tokia.client.hud;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.config.WidgetCfg;
import com.tokia.client.util.ClickCounter;
import com.tokia.client.util.FrameStats;
import com.tokia.client.util.PlayerStats;
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

        Def(String id, Function<TokiaConfig, WidgetCfg> cfg, Function<Minecraft, String> value) {
            this.nameKey = "tokia.widget." + id;
            this.labelKey = "tokia.label." + id;
            this.cfg = cfg;
            this.value = value;
        }
    }

    public static final List<Def> DEFS = List.of(
        new Def("fps", c -> c.fps, mc -> String.valueOf(FrameStats.fps())),
        new Def("ping", c -> c.ping, InfoHud::ping),
        new Def("coords", c -> c.coords, mc -> String.format(Locale.ROOT, "%.1f / %.1f / %.1f",
            mc.player.getX(), mc.player.getY(), mc.player.getZ())),
        new Def("cps", c -> c.cps, mc -> ClickCounter.left() + " | " + ClickCounter.right()),
        new Def("frametime", c -> c.frametime, mc -> String.format(Locale.ROOT, "%.1f ms", FrameStats.frameMs())),
        new Def("chunk", c -> c.chunk, mc -> Math.floorDiv((int) Math.floor(mc.player.getX()), 16) + ", "
            + Math.floorDiv((int) Math.floor(mc.player.getZ()), 16)),
        new Def("direction", c -> c.direction, mc -> direction(mc.player.getYRot())),
        new Def("angles", c -> c.angles, mc -> String.format(Locale.ROOT, "%.1f / %.1f",
            wrap(mc.player.getYRot()), mc.player.getXRot())),
        new Def("speed", c -> c.speed, mc -> String.format(Locale.ROOT, "%.1f b/s", PlayerStats.speed())),
        new Def("ram", c -> c.ram, InfoHud::ram),
        new Def("handitem", c -> c.handItem, InfoHud::handItem),
        new Def("clock", c -> c.clock, mc -> LocalTime.now().format(CLOCK)),
        new Def("session", c -> c.session, InfoHud::session)
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

    private static String ping(Minecraft mc) {
        int ping = -1;
        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            PlayerInfo info = conn.getPlayerInfo(mc.player.getUUID());
            if (info != null) {
                ping = info.getLatency();
            }
        }
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

    /** Zeichnet eine Anzeige mit ihrem Stil. Wird auch fuer die Vorschau im Menue genutzt. */
    public static void drawBox(GuiGraphicsExtractor g, Minecraft mc, WidgetCfg c, String text, int x, int y) {
        int w = mc.font.width(text) + 6;
        int h = mc.font.lineHeight + 4;
        if (c.background) {
            g.fill(x, y, x + w, y + h, c.bgColor());
        }
        g.text(mc.font, text, x + 3, y + 2, c.textColor(), c.shadow);
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
            drawBox(g, mc, c, text, x, y);
        }
    }
}
