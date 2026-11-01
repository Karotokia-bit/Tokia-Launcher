package com.tokia.client.gui;

import com.tokia.client.config.TokiaConfig;
import com.tokia.client.config.WidgetCfg;
import com.tokia.client.hud.CrosshairHud;
import com.tokia.client.hud.InfoHud;
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

        // Anzeigen: jede Info-Anzeige einzeln einstellbar
        List<Row> widgetRows = new ArrayList<>();
        widgetRows.add(new WidgetTilesRow());
        List<Page> widgetPages = new ArrayList<>();
        widgetPages.add(new Page("", widgetRows));
        modules.add(new Module(tr("tokia.module.widgets"), tr("tokia.module.widgets.desc"),
            c::anyInfoEnabled, c::setInfoEnabled, c::resetInfoAll, false, widgetPages));

        // Keystrokes
        List<Row> keysGeneral = new ArrayList<>();
        keysGeneral.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.keys.enabled, v -> c.keys.enabled = v));
        keysGeneral.add(new SwitchRow(tr("tokia.opt.keys_mouse"), () -> c.keystrokesMouse, v -> c.keystrokesMouse = v));
        keysGeneral.add(new SwitchRow(tr("tokia.opt.keys_space"), () -> c.keystrokesSpace, v -> c.keystrokesSpace = v));
        List<Row> keysStyle = new ArrayList<>();
        keysStyle.add(new StyleEditorRow(c.keys, false, false, false));
        List<Page> keyPages = new ArrayList<>();
        keyPages.add(new Page(tr("tokia.page.general"), keysGeneral));
        keyPages.add(new Page(tr("tokia.page.style"), keysStyle));
        modules.add(new Module(tr("tokia.module.keystrokes"), tr("tokia.module.keystrokes.desc"),
            () -> c.keys.enabled, v -> c.keys.enabled = v, c::resetKeystrokes, false, keyPages));

        // Ruestung
        List<Row> armorGeneral = new ArrayList<>();
        armorGeneral.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.armor.enabled, v -> c.armor.enabled = v));
        armorGeneral.add(new TextRow(tr("tokia.module.armor.text")));
        List<Row> armorStyle = new ArrayList<>();
        armorStyle.add(new StyleEditorRow(c.armor, false, false, true));
        List<Page> armorPages = new ArrayList<>();
        armorPages.add(new Page(tr("tokia.page.general"), armorGeneral));
        armorPages.add(new Page(tr("tokia.page.style"), armorStyle));
        modules.add(new Module(tr("tokia.module.armor"), tr("tokia.module.armor.desc"),
            () -> c.armor.enabled, v -> c.armor.enabled = v, c::resetArmor, false, armorPages));

        // Effekte
        List<Row> effectGeneral = new ArrayList<>();
        effectGeneral.add(new SwitchRow(tr("tokia.opt.enabled"), () -> c.effects.enabled, v -> c.effects.enabled = v));
        effectGeneral.add(new TextRow(tr("tokia.module.effects.text")));
        List<Row> effectStyle = new ArrayList<>();
        effectStyle.add(new StyleEditorRow(c.effects, false, false, true));
        List<Page> effectPages = new ArrayList<>();
        effectPages.add(new Page(tr("tokia.page.general"), effectGeneral));
        effectPages.add(new Page(tr("tokia.page.style"), effectStyle));
        modules.add(new Module(tr("tokia.module.effects"), tr("tokia.module.effects.desc"),
            () -> c.effects.enabled, v -> c.effects.enabled = v, c::resetEffects, false, effectPages));

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
        int ph = Math.min(this.height - 10, 260);
        int px = (this.width - pw) / 2;
        int py = (this.height - ph) / 2;

        // Panel mit Rahmen und Seitenleiste
        g.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, BORDER);
        g.fill(px, py, px + pw, py + ph, PANEL);
        g.fill(px, py, px + SIDE_W, py + ph, SIDEBAR);
        g.fill(px + SIDE_W, py, px + SIDE_W + 1, py + ph, BORDER);

        // Seitenleiste
        g.text(this.font, "TOKIA", px + 12, py + 14, ACCENT, true);
        for (int i = 0; i < modules.size(); i++) {
            final Module m = modules.get(i);
            final int idx = i;
            int iy = py + 44 + i * 24;
            boolean sel = i == selectedModule;
            if (sel) {
                g.fill(px + 4, iy, px + SIDE_W - 4, iy + 22, 0xFF1E1A33);
                g.fill(px + 4, iy, px + 6, iy + 22, ACCENT);
            } else if (inside(px + 4, iy, SIDE_W - 8, 22)) {
                g.fill(px + 4, iy, px + SIDE_W - 4, iy + 22, HOVER);
            }
            g.text(this.font, m.name, px + 14, iy + 7, sel ? TEXT : SUB, false);
            hit(px + 4, iy, SIDE_W - 8, 22, () -> {
                selectedModule = idx;
                selectedPage = 0;
            });
            boolean on = m.enabled.getAsBoolean();
            g.fill(px + SIDE_W - 16, iy + 8, px + SIDE_W - 10, iy + 14, on ? 0xFF55FF55 : 0xFF555566);
            hit(px + SIDE_W - 20, iy + 4, 14, 14, () -> m.setEnabled.accept(!m.enabled.getAsBoolean()));
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

    /** Zwei Spalten: links Schalter und Deckkraft, rechts Farbwahl (Palette + RGB). */
    private final class StyleEditorRow extends Row {
        final List<Row> left = new ArrayList<>();
        final List<Row> right = new ArrayList<>();

        StyleEditorRow(WidgetCfg cfg, boolean enabledRow, boolean labelRow, boolean shadowRow) {
            if (enabledRow) {
                left.add(new SwitchRow(tr("tokia.opt.enabled"), () -> cfg.enabled, v -> cfg.enabled = v));
            }
            if (labelRow) {
                left.add(new SwitchRow(tr("tokia.opt.label"), () -> cfg.label, v -> cfg.label = v));
            }
            left.add(new SwitchRow(tr("tokia.opt.background"), () -> cfg.background, v -> cfg.background = v));
            left.add(new SliderRow(tr("tokia.opt.opacity"), () -> cfg.bgOpacity, v -> cfg.bgOpacity = v, 0, 100, "%", 0));
            if (shadowRow) {
                left.add(new SwitchRow(tr("tokia.opt.shadow"), () -> cfg.shadow, v -> cfg.shadow = v));
            }
            right.add(new SwatchRow(() -> cfg.color, v -> cfg.color = v));
            right.add(new SliderRow(tr("tokia.opt.red"), () -> (cfg.color >> 16) & 0xFF,
                v -> cfg.color = (cfg.color & 0x00FFFF) | (v << 16), 0, 255, "", 0xFFFF5555));
            right.add(new SliderRow(tr("tokia.opt.green"), () -> (cfg.color >> 8) & 0xFF,
                v -> cfg.color = (cfg.color & 0xFF00FF) | (v << 8), 0, 255, "", 0xFF55FF55));
            right.add(new SliderRow(tr("tokia.opt.blue"), () -> cfg.color & 0xFF,
                v -> cfg.color = (cfg.color & 0xFFFF00) | v, 0, 255, "", 0xFF5577FF));
        }

        @Override
        int render(GuiGraphicsExtractor g, int x, int y, int w) {
            int lw = (w - 10) * 46 / 100;
            int rw = w - 10 - lw;
            int lh = 0;
            for (Row r : left) {
                lh += r.render(g, x, y + lh, lw);
            }
            int rh = 0;
            for (Row r : right) {
                rh += r.render(g, x + lw + 10, y + rh, rw);
            }
            return Math.max(lh, rh);
        }
    }

    /** Kacheln aller Info-Anzeigen. Ein Klick auf "Stil" oeffnet die Einstellungen der einzelnen Anzeige. */
    private final class WidgetTilesRow extends Row {
        int selected = -1;
        final List<StyleEditorRow> editors = new ArrayList<>();

        WidgetTilesRow() {
            TokiaConfig c = TokiaConfig.get();
            for (InfoHud.Def d : InfoHud.DEFS) {
                editors.add(new StyleEditorRow(d.cfg.apply(c), true, true, true));
            }
        }

        @Override
        int render(GuiGraphicsExtractor g, int x, int y, int w) {
            return selected < 0 ? tiles(g, x, y, w) : detail(g, x, y, w);
        }

        private int tiles(GuiGraphicsExtractor g, int x, int y, int w) {
            TokiaConfig cfg = TokiaConfig.get();
            int tileW = (w - 6) / 2;
            int n = InfoHud.DEFS.size();
            String editLabel = tr("tokia.menu.edit");
            int ew = font.width(editLabel) + 10;
            for (int i = 0; i < n; i++) {
                final int idx = i;
                InfoHud.Def d = InfoHud.DEFS.get(i);
                WidgetCfg c = d.cfg.apply(cfg);
                int tx = x + (i % 2) * (tileW + 6);
                int ty = y + (i / 2) * 24;
                g.fill(tx, ty, tx + tileW, ty + 22, inside(tx, ty, tileW, 22) ? 0xFF22222F : 0xFF181824);
                boolean on = c.enabled;
                g.fill(tx + 6, ty + 7, tx + 24, ty + 15, on ? ACCENT : OFF);
                int kx = on ? tx + 16 : tx + 8;
                g.fill(kx, ty + 8, kx + 6, ty + 14, 0xFFFFFFFF);
                g.text(font, tr(d.nameKey), tx + 30, ty + 7, on ? TEXT : SUB, false);
                hit(tx, ty, tileW, 22, () -> c.enabled = !c.enabled);
                int ex = tx + tileW - ew - 4;
                g.fill(ex, ty + 4, ex + ew, ty + 18, inside(ex, ty + 4, ew, 14) ? 0xFF2E2E42 : 0xFF232336);
                g.text(font, editLabel, ex + 5, ty + 7, SUB, false);
                hit(ex, ty + 4, ew, 14, () -> selected = idx);
            }
            return ((n + 1) / 2) * 24;
        }

        private int detail(GuiGraphicsExtractor g, int x, int y, int w) {
            InfoHud.Def d = InfoHud.DEFS.get(selected);
            WidgetCfg c = d.cfg.apply(TokiaConfig.get());
            String back = "< " + tr("tokia.menu.back");
            int bw = font.width(back) + 12;
            g.fill(x, y, x + bw, y + 14, inside(x, y, bw, 14) ? 0xFF2E2E42 : 0xFF1C1C2B);
            g.text(font, back, x + 6, y + 3, SUB, false);
            hit(x, y, bw, 14, () -> selected = -1);
            g.text(font, tr(d.nameKey), x + bw + 8, y + 3, TEXT, true);

            int py2 = y + 20;
            g.fill(x, py2, x + w, py2 + 24, 0xFF3A4A5A);
            InfoHud.drawBox(g, minecraft, c, InfoHud.text(d, minecraft, c), x + 8, py2 + 5);
            return 50 + editors.get(selected).render(g, x, y + 50, w);
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
