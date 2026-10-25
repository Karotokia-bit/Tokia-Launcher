package com.tokia.client.gui;

import com.tokia.client.config.TokiaConfig;
import com.tokia.client.hud.CrosshairHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Einstellungsmenue von Tokia (Standard-Taste: Rechte Shift-Taste).
 * Seitenleiste mit Modulen, rechts die Einstellungen des gewaehlten Moduls.
 */
public class TokiaMenuScreen extends Screen {
    private static final int ACCENT = 0xFF8B5CF6;
    private static final int PANEL = 0xF0141420;
    private static final int SIDEBAR = 0xFF0E0E17;
    private static final int BORDER = 0xFF2A2A3C;
    private static final int HOVER = 0x18FFFFFF;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int SUB = 0xFF9A9AB0;
    private static final int OFF = 0xFF3A3A4C;
    private static final int SIDE_W = 104;

    private final Screen parent;
    private final List<Hit> hits = new ArrayList<>();
    private List<Module> modules = new ArrayList<>();
    private int selectedModule;
    private int selectedPage;
    private int mx;
    private int my;
    private SliderRow dragging;

    public TokiaMenuScreen(Screen parent) {
        super(Component.translatable("tokia.menu.title"));
        this.parent = parent;
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    // ------------------------------------------------------------------ Aufbau

    @Override
    protected void init() {
        buildModules();
    }

    private void buildModules() {
        TokiaConfig c = TokiaConfig.get();
        modules = new ArrayList<>();

        // Crosshair: Seite "Form"
        List<Row> shape = new ArrayList<>();
        shape.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.crosshairEnabled, v -> c.crosshairEnabled = v));
        shape.add(new ChoiceRow(tr("tokia.opt.style"),
            new String[] {tr("tokia.style.0"), tr("tokia.style.1"), tr("tokia.style.2"), tr("tokia.style.3")},
            () -> c.crosshairStyle, v -> c.crosshairStyle = v));
        shape.add(new SliderRow(tr("tokia.opt.size"), () -> c.crosshairSize, v -> c.crosshairSize = v, 1, 20, "", 0));
        shape.add(new SliderRow(tr("tokia.opt.thickness"), () -> c.crosshairThickness, v -> c.crosshairThickness = v, 1, 6, "", 0));
        shape.add(new SliderRow(tr("tokia.opt.gap"), () -> c.crosshairGap, v -> c.crosshairGap = v, 0, 12, "", 0));
        shape.add(new SwitchRow(tr("tokia.opt.outline"), () -> c.crosshairOutline, v -> c.crosshairOutline = v));
        shape.add(new SwitchRow(tr("tokia.opt.dot"), () -> c.crosshairDot, v -> c.crosshairDot = v));

        // Crosshair: Seite "Farbe"
        List<Row> color = new ArrayList<>();
        color.add(new SwatchRow(() -> c.crosshairRgb, v -> c.crosshairRgb = v));
        color.add(new SliderRow(tr("tokia.opt.red"), () -> (c.crosshairRgb >> 16) & 0xFF,
            v -> c.crosshairRgb = (c.crosshairRgb & 0x00FFFF) | (v << 16), 0, 255, "", 0xFFFF5555));
        color.add(new SliderRow(tr("tokia.opt.green"), () -> (c.crosshairRgb >> 8) & 0xFF,
            v -> c.crosshairRgb = (c.crosshairRgb & 0xFF00FF) | (v << 8), 0, 255, "", 0xFF55FF55));
        color.add(new SliderRow(tr("tokia.opt.blue"), () -> c.crosshairRgb & 0xFF,
            v -> c.crosshairRgb = (c.crosshairRgb & 0xFFFF00) | v, 0, 255, "", 0xFF5577FF));
        color.add(new SliderRow(tr("tokia.opt.opacity"), () -> c.crosshairOpacity, v -> c.crosshairOpacity = v, 10, 100, "%", 0));

        List<Page> crosshairPages = new ArrayList<>();
        crosshairPages.add(new Page(tr("tokia.page.shape"), shape));
        crosshairPages.add(new Page(tr("tokia.page.color"), color));
        modules.add(new Module(tr("tokia.module.crosshair"), tr("tokia.module.crosshair.desc"),
            () -> c.crosshairEnabled, v -> c.crosshairEnabled = v, c::resetCrosshair, true, crosshairPages));

        // Info-HUD
        List<Row> info = new ArrayList<>();
        info.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.infoEnabled, v -> c.infoEnabled = v));
        info.add(new SwitchRow(tr("tokia.opt.fps"), () -> c.showFps, v -> c.showFps = v));
        info.add(new SwitchRow(tr("tokia.opt.ping"), () -> c.showPing, v -> c.showPing = v));
        info.add(new SwitchRow(tr("tokia.opt.coords"), () -> c.showCoords, v -> c.showCoords = v));
        info.add(new SwitchRow(tr("tokia.opt.cps"), () -> c.showCps, v -> c.showCps = v));
        info.add(new SwitchRow(tr("tokia.opt.clock"), () -> c.showClock, v -> c.showClock = v));
        info.add(new SwitchRow(tr("tokia.opt.background"), () -> c.infoBackground, v -> c.infoBackground = v));
        List<Row> extras = new ArrayList<>();
        extras.add(new SwitchRow(tr("tokia.opt.direction"), () -> c.showDirection, v -> c.showDirection = v));
        extras.add(new SwitchRow(tr("tokia.opt.speed"), () -> c.showSpeed, v -> c.showSpeed = v));
        extras.add(new SwitchRow(tr("tokia.opt.ram"), () -> c.showRam, v -> c.showRam = v));
        extras.add(new SwitchRow(tr("tokia.opt.session"), () -> c.showSession, v -> c.showSession = v));
        extras.add(new SwitchRow(tr("tokia.opt.reach"), () -> c.showReach, v -> c.showReach = v));
        extras.add(new SwitchRow(tr("tokia.opt.combo"), () -> c.showCombo, v -> c.showCombo = v));
        List<Page> infoPages = new ArrayList<>();
        infoPages.add(new Page(tr("tokia.page.display"), info));
        infoPages.add(new Page(tr("tokia.page.extras"), extras));
        modules.add(new Module(tr("tokia.module.info"), tr("tokia.module.info.desc"),
            () -> c.infoEnabled, v -> c.infoEnabled = v, c::resetInfo, false, infoPages));

        // Keystrokes
        List<Row> keys = new ArrayList<>();
        keys.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.keystrokes, v -> c.keystrokes = v));
        keys.add(new SwitchRow(tr("tokia.opt.keys_mouse"), () -> c.keystrokesMouse, v -> c.keystrokesMouse = v));
        keys.add(new SwitchRow(tr("tokia.opt.keys_space"), () -> c.keystrokesSpace, v -> c.keystrokesSpace = v));
        List<Page> keyPages = new ArrayList<>();
        keyPages.add(new Page("", keys));
        modules.add(new Module(tr("tokia.module.keystrokes"), tr("tokia.module.keystrokes.desc"),
            () -> c.keystrokes, v -> c.keystrokes = v, c::resetKeystrokes, false, keyPages));

        // Ruestung
        List<Row> armor = new ArrayList<>();
        armor.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.armorHud, v -> c.armorHud = v));
        armor.add(new TextRow(tr("tokia.module.armor.text")));
        List<Page> armorPages = new ArrayList<>();
        armorPages.add(new Page("", armor));
        modules.add(new Module(tr("tokia.module.armor"), tr("tokia.module.armor.desc"),
            () -> c.armorHud, v -> c.armorHud = v, c::resetArmor, false, armorPages));

        // Effekte
        List<Row> effects = new ArrayList<>();
        effects.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.effectsHud, v -> c.effectsHud = v));
        effects.add(new TextRow(tr("tokia.module.effects.text")));
        List<Page> effectPages = new ArrayList<>();
        effectPages.add(new Page("", effects));
        modules.add(new Module(tr("tokia.module.effects"), tr("tokia.module.effects.desc"),
            () -> c.effectsHud, v -> c.effectsHud = v, c::resetEffects, false, effectPages));

        // Item-Zaehler
        List<Row> items = new ArrayList<>();
        items.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.itemCounter, v -> c.itemCounter = v));
        items.add(new SwitchRow(tr("tokia.item.gapple"), () -> c.itemGapples, v -> c.itemGapples = v));
        items.add(new SwitchRow(tr("tokia.item.pearl"), () -> c.itemPearls, v -> c.itemPearls = v));
        items.add(new SwitchRow(tr("tokia.item.pot"), () -> c.itemPots, v -> c.itemPots = v));
        items.add(new SwitchRow(tr("tokia.item.arrow"), () -> c.itemArrows, v -> c.itemArrows = v));
        List<Page> itemPages = new ArrayList<>();
        itemPages.add(new Page("", items));
        modules.add(new Module(tr("tokia.module.items"), tr("tokia.module.items.desc"),
            () -> c.itemCounter, v -> c.itemCounter = v, c::resetItems, false, itemPages));

        // Toggle Sprint
        List<Row> sprint = new ArrayList<>();
        sprint.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.toggleSprint, v -> c.toggleSprint = v));
        sprint.add(new TextRow(tr("tokia.module.sprint.text")));
        List<Page> sprintPages = new ArrayList<>();
        sprintPages.add(new Page("", sprint));
        modules.add(new Module(tr("tokia.module.sprint"), tr("tokia.module.sprint.desc"),
            () -> c.toggleSprint, v -> c.toggleSprint = v, c::resetSprint, false, sprintPages));

        // Fullbright
        List<Row> bright = new ArrayList<>();
        bright.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.fullbright, v -> c.fullbright = v));
        bright.add(new TextRow(tr("tokia.module.fullbright.text")));
        List<Page> brightPages = new ArrayList<>();
        brightPages.add(new Page("", bright));
        modules.add(new Module(tr("tokia.module.fullbright"), tr("tokia.module.fullbright.desc"),
            () -> c.fullbright, v -> c.fullbright = v, c::resetFullbright, false, brightPages));

        // Zoom
        List<Row> zoom = new ArrayList<>();
        zoom.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.zoomEnabled, v -> c.zoomEnabled = v));
        zoom.add(new SliderRow(tr("tokia.opt.zoom_fov"), () -> c.zoomFov, v -> c.zoomFov = v, 30, 90, "", 0));
        zoom.add(new TextRow(tr("tokia.module.zoom.text")));
        List<Page> zoomPages = new ArrayList<>();
        zoomPages.add(new Page("", zoom));
        modules.add(new Module(tr("tokia.module.zoom"), tr("tokia.module.zoom.desc"),
            () -> c.zoomEnabled, v -> c.zoomEnabled = v, c::resetZoom, false, zoomPages));
    }

    // ------------------------------------------------------------------ Zeichnen

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        this.mx = mouseX;
        this.my = mouseY;
        hits.clear();
        if (dragging != null) {
            dragging.setFromMouse(mouseX);
        }
        if (modules.isEmpty()) {
            buildModules();
        }

        int pw = Math.min(this.width - 20, 430);
        int ph = Math.min(this.height - 10, 290);
        int px = (this.width - pw) / 2;
        int py = (this.height - ph) / 2;

        // Panel mit Rahmen und Seitenleiste
        g.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, BORDER);
        g.fill(px, py, px + pw, py + ph, PANEL);
        g.fill(px, py, px + SIDE_W, py + ph, SIDEBAR);
        g.fill(px + SIDE_W, py, px + SIDE_W + 1, py + ph, BORDER);

        // Seitenleiste
        g.text(this.font, "TOKIA", px + 12, py + 14, ACCENT, true);
        int pitch = Math.max(14, Math.min(24, (ph - 90) / Math.max(1, modules.size())));
        int rowH = pitch - 2;
        for (int i = 0; i < modules.size(); i++) {
            final Module m = modules.get(i);
            final int idx = i;
            int iy = py + 44 + i * pitch;
            boolean sel = i == selectedModule;
            if (sel) {
                g.fill(px + 4, iy, px + SIDE_W - 4, iy + rowH, 0xFF1E1A33);
                g.fill(px + 4, iy, px + 6, iy + rowH, ACCENT);
            } else if (inside(px + 4, iy, SIDE_W - 8, rowH)) {
                g.fill(px + 4, iy, px + SIDE_W - 4, iy + rowH, HOVER);
            }
            g.text(this.font, m.name, px + 14, iy + (rowH - 8) / 2, sel ? TEXT : SUB, false);
            hit(px + 4, iy, SIDE_W - 8, rowH, () -> {
                selectedModule = idx;
                selectedPage = 0;
            });
            boolean on = m.enabled.getAsBoolean();
            g.fill(px + SIDE_W - 16, iy + rowH / 2 - 3, px + SIDE_W - 10, iy + rowH / 2 + 3, on ? 0xFF55FF55 : 0xFF555566);
            hit(px + SIDE_W - 20, iy + rowH / 2 - 7, 14, 14, () -> m.setEnabled.accept(!m.enabled.getAsBoolean()));
        }
        int ebx = px + 8;
        int eby = py + ph - 40;
        int ebw = SIDE_W - 16;
        String editLabel = tr("tokia.menu.edit_hud");
        g.fill(ebx, eby, ebx + ebw, eby + 16, inside(ebx, eby, ebw, 16) ? 0xFF2E2E42 : 0xFF1C1C2B);
        g.text(this.font, editLabel, ebx + (ebw - this.font.width(editLabel)) / 2, eby + 4, TEXT, false);
        hit(ebx, eby, ebw, 16, () -> this.minecraft.gui.setScreen(new HudEditorScreen(this)));
        g.text(this.font, tr("tokia.menu.hint"), px + 12, py + ph - 14, SUB, false);

        // Inhalt
        if (selectedModule >= modules.size()) {
            selectedModule = 0;
        }
        final Module m = modules.get(selectedModule);
        if (selectedPage >= m.pages.size()) {
            selectedPage = 0;
        }
        int cx = px + SIDE_W + 14;
        int cw = pw - SIDE_W - 28;
        g.text(this.font, m.name, cx, py + 10, TEXT, true);
        g.text(this.font, m.desc, cx, py + 22, SUB, false);

        String resetLabel = tr("tokia.menu.reset");
        int rw = this.font.width(resetLabel) + 12;
        int rx = cx + cw - rw;
        int ry = py + 8;
        g.fill(rx, ry, rx + rw, ry + 14, inside(rx, ry, rw, 14) ? 0xFF2E2E42 : 0xFF1C1C2B);
        g.text(this.font, resetLabel, rx + 6, ry + 3, SUB, false);
        hit(rx, ry, rw, 14, m.reset);

        int rowsY = py + 42;
        if (m.pages.size() > 1) {
            int tx = cx;
            for (int i = 0; i < m.pages.size(); i++) {
                final int idx = i;
                String name = m.pages.get(i).name;
                int tw = this.font.width(name) + 14;
                boolean sel = i == selectedPage;
                int bg = sel ? 0xFF2A2250 : (inside(tx, py + 36, tw, 15) ? 0xFF22222F : 0xFF181824);
                g.fill(tx, py + 36, tx + tw, py + 51, bg);
                if (sel) {
                    g.fill(tx, py + 49, tx + tw, py + 51, ACCENT);
                }
                g.text(this.font, name, tx + 7, py + 40, sel ? TEXT : SUB, false);
                hit(tx, py + 36, tw, 15, () -> selectedPage = idx);
                tx += tw + 4;
            }
            rowsY = py + 58;
        }

        boolean preview = m.preview && cw >= 250;
        int rowsW = preview ? cw - 100 : cw;
        int yy = rowsY;
        for (Row row : m.pages.get(selectedPage).rows) {
            yy += row.render(g, cx, yy, rowsW);
        }
        if (preview) {
            drawPreview(g, cx + cw - 90, rowsY);
        }
    }

    private void drawPreview(GuiGraphicsExtractor g, int x, int y) {
        int s = 90;
        g.text(this.font, tr("tokia.menu.preview"), x, y, SUB, false);
        int by = y + 12;
        g.fill(x - 1, by - 1, x + s + 1, by + s + 1, BORDER);
        g.fill(x, by, x + s, by + s / 2 + 10, 0xFF6FA8DC);
        g.fill(x, by + s / 2 + 10, x + s, by + s, 0xFF5B8C3A);
        TokiaConfig c = TokiaConfig.get();
        if (c.crosshairEnabled) {
            CrosshairHud.draw(g, c, x + s / 2, by + s / 2);
        }
        String hex = String.format(Locale.ROOT, "#%06X", c.crosshairRgb & 0xFFFFFF);
        g.text(this.font, hex, x, by + s + 4, SUB, false);
    }

    // ------------------------------------------------------------------ Maus

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (h.contains(mx, my)) {
                if (h.slider != null) {
                    dragging = h.slider;
                    dragging.setFromMouse(mx);
                } else {
                    h.action.run();
                    TokiaConfig.save();
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) {
            dragging = null;
            TokiaConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        TokiaConfig.save();
        this.minecraft.gui.setScreen(this.parent);
    }

    private boolean inside(int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void hit(int x, int y, int w, int h, Runnable action) {
        hits.add(new Hit(x, y, w, h, action, null));
    }

    // ------------------------------------------------------------------ Datenklassen

    private final class Hit {
        final int x;
        final int y;
        final int w;
        final int h;
        final Runnable action;
        final SliderRow slider;

        Hit(int x, int y, int w, int h, Runnable action, SliderRow slider) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.action = action;
            this.slider = slider;
        }

        boolean contains(int px, int py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }

    private final class Module {
        final String name;
        final String desc;
        final BooleanSupplier enabled;
        final Consumer<Boolean> setEnabled;
        final Runnable reset;
        final boolean preview;
        final List<Page> pages;

        Module(String name, String desc, BooleanSupplier enabled, Consumer<Boolean> setEnabled,
               Runnable reset, boolean preview, List<Page> pages) {
            this.name = name;
            this.desc = desc;
            this.enabled = enabled;
            this.setEnabled = setEnabled;
            this.reset = reset;
            this.preview = preview;
            this.pages = pages;
        }
    }

    private final class Page {
        final String name;
        final List<Row> rows;

        Page(String name, List<Row> rows) {
            this.name = name;
            this.rows = rows;
        }
    }

    // ------------------------------------------------------------------ Zeilen

    /** Eine Einstellungszeile. render() zeichnet sie und gibt die benoetigte Hoehe zurueck. */
    private abstract class Row {
        abstract int render(GuiGraphicsExtractor g, int x, int y, int w);
    }

    private final class SwitchRow extends Row {
        final String label;
        final BooleanSupplier get;
        final Consumer<Boolean> set;

        SwitchRow(String label, BooleanSupplier get, Consumer<Boolean> set) {
            this.label = label;
            this.get = get;
            this.set = set;
        }

        @Override
        int render(GuiGraphicsExtractor g, int x, int y, int w) {
            if (inside(x, y, w, 20)) {
                g.fill(x, y, x + w, y + 20, HOVER);
            }
            g.text(font, label, x + 6, y + 6, TEXT, false);
            boolean on = get.getAsBoolean();
            int sx = x + w - 30;
            int sy = y + 5;
            g.fill(sx, sy, sx + 24, sy + 10, on ? ACCENT : OFF);
            int kx = on ? sx + 14 : sx + 2;
            g.fill(kx, sy + 2, kx + 8, sy + 8, 0xFFFFFFFF);
            hit(x, y, w, 20, () -> set.accept(!get.getAsBoolean()));
            return 20;
        }
    }

    private final class SliderRow extends Row {
        final String label;
        final IntSupplier get;
        final IntConsumer set;
        final int min;
        final int max;
        final String suffix;
        final int tint;
        int bx;
        int bw;

        SliderRow(String label, IntSupplier get, IntConsumer set, int min, int max, String suffix, int tint) {
            this.label = label;
            this.get = get;
            this.set = set;
            this.min = min;
            this.max = max;
            this.suffix = suffix;
            this.tint = tint;
        }

        void setFromMouse(int mouseX) {
            if (bw <= 0) {
                return;
            }
            float f = (mouseX - bx) / (float) bw;
            f = Math.max(0f, Math.min(1f, f));
            set.accept(min + Math.round(f * (max - min)));
        }

        @Override
        int render(GuiGraphicsExtractor g, int x, int y, int w) {
            if (inside(x, y, w, 26)) {
                g.fill(x, y, x + w, y + 26, HOVER);
            }
            int value = get.getAsInt();
            g.text(font, label, x + 6, y + 3, TEXT, false);
            String val = value + suffix;
            g.text(font, val, x + w - 6 - font.width(val), y + 3, SUB, false);
            bx = x + 6;
            bw = w - 12;
            int by = y + 17;
            g.fill(bx, by, bx + bw, by + 3, OFF);
            float f = (value - min) / (float) (max - min);
            int fx = bx + Math.round(f * bw);
            g.fill(bx, by, fx, by + 3, tint != 0 ? tint : ACCENT);
            g.fill(fx - 2, by - 3, fx + 2, by + 6, 0xFFFFFFFF);
            hits.add(new Hit(x, y, w, 26, null, this));
            return 26;
        }
    }

    private final class ChoiceRow extends Row {
        final String label;
        final String[] names;
        final IntSupplier get;
        final IntConsumer set;

        ChoiceRow(String label, String[] names, IntSupplier get, IntConsumer set) {
            this.label = label;
            this.names = names;
            this.get = get;
            this.set = set;
        }

        @Override
        int render(GuiGraphicsExtractor g, int x, int y, int w) {
            g.text(font, label, x + 6, y + 3, TEXT, false);
            int n = names.length;
            int segW = (w - 12) / n;
            int sy = y + 17;
            int current = get.getAsInt();
            for (int i = 0; i < n; i++) {
                final int idx = i;
                int sx = x + 6 + i * segW;
                boolean sel = i == current;
                int bg = sel ? ACCENT : (inside(sx, sy, segW, 14) ? 0xFF2E2E42 : 0xFF1C1C2B);
                g.fill(sx, sy, sx + segW - 2, sy + 14, bg);
                g.text(font, names[i], sx + (segW - 2 - font.width(names[i])) / 2, sy + 3, sel ? TEXT : SUB, false);
                hit(sx, sy, segW, 14, () -> set.accept(idx));
            }
            return 34;
        }
    }

    private final class SwatchRow extends Row {
        final IntSupplier get;
        final IntConsumer set;

        SwatchRow(IntSupplier get, IntConsumer set) {
            this.get = get;
            this.set = set;
        }

        @Override
        int render(GuiGraphicsExtractor g, int x, int y, int w) {
            int cols = 8;
            int size = 14;
            int gap = 3;
            int current = get.getAsInt() & 0xFFFFFF;
            for (int i = 0; i < TokiaConfig.PALETTE.length; i++) {
                final int rgb = TokiaConfig.PALETTE[i];
                int sx = x + 6 + (i % cols) * (size + gap);
                int sy = y + 3 + (i / cols) * (size + gap);
                if (rgb == current) {
                    g.fill(sx - 2, sy - 2, sx + size + 2, sy + size + 2, 0xFFFFFFFF);
                } else if (inside(sx, sy, size, size)) {
                    g.fill(sx - 1, sy - 1, sx + size + 1, sy + size + 1, SUB);
                }
                g.fill(sx, sy, sx + size, sy + size, 0xFF000000 | rgb);
                hit(sx, sy, size, size, () -> set.accept(rgb));
            }
            return 42;
        }
    }

    private final class TextRow extends Row {
        final String text;

        TextRow(String text) {
            this.text = text;
        }

        @Override
        int render(GuiGraphicsExtractor g, int x, int y, int w) {
            List<String> lines = wrap(text, w - 12);
            int ly = y + 4;
            for (String line : lines) {
                g.text(font, line, x + 6, ly, SUB, false);
                ly += 11;
            }
            return lines.size() * 11 + 8;
        }
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = cur.length() == 0 ? word : cur + " " + word;
            if (this.font.width(test) > maxWidth && cur.length() > 0) {
                out.add(cur.toString());
                cur = new StringBuilder(word);
            } else {
                cur = new StringBuilder(test);
            }
        }
        if (cur.length() > 0) {
            out.add(cur.toString());
        }
        return out;
    }
}
