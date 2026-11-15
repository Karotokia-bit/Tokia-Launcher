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
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Einstellungsmenue von Tokia (Standard-Taste: Rechte Shift-Taste).
 * Seitenleiste mit Modulen, rechts die Einstellungen des gewaehlten Moduls.
 */
public class TokiaMenuScreen extends Screen {
    private static final int PANEL = 0xF511141D;
    private static final int SIDEBAR = 0xFF10121A;
    private static final int BORDER = 0xFF2A2D3B;
    private static final int HOVER = 0x18FFFFFF;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int SUB = 0xFF9A9AB0;
    private static final int OFF = 0xFF3A3A4C;
    private static final int SIDE_W = 190;

    private final Screen parent;
    private final List<Hit> hits = new ArrayList<>();
    private List<Module> modules = new ArrayList<>();
    private int selectedModule = -1;
    private int selectedPage;
    private int selectedCategory;
    private int mx;
    private int my;
    private SliderRow dragging;
    private long openedAt;
    private final Map<Row, Float> switchMotion = new IdentityHashMap<>();

    public TokiaMenuScreen(Screen parent) {
        super(Component.translatable("tokia.menu.title"));
        this.parent = parent;
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    private int accent() {
        return 0xFF000000 | (TokiaConfig.get().menuAccentRgb & 0xFFFFFF);
    }

    private int accentSurface() {
        int rgb = accent();
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return 0xFF000000 | ((r * 42 / 255) << 16) | ((g * 42 / 255) << 8) | (b * 42 / 255);
    }

    // ------------------------------------------------------------------ Aufbau

    @Override
    protected void init() {
        buildModules();
        selectedModule = -1;
        selectedCategory = 0;
        openedAt = System.nanoTime();
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
        List<Row> crossExtra = new ArrayList<>();
        crossExtra.add(new SwitchRow(tr("tokia.opt.outline"), () -> c.crosshairOutline, v -> c.crosshairOutline = v));
        crossExtra.add(new SwitchRow(tr("tokia.opt.dot"), () -> c.crosshairDot, v -> c.crosshairDot = v));
        crossExtra.add(new SwitchRow(tr("tokia.opt.dynamic"), () -> c.crosshairDynamic, v -> c.crosshairDynamic = v));
        crossExtra.add(new TextRow(tr("tokia.opt.dynamic.text")));

        // Crosshair: Seite "Farbe"
        List<Row> color = new ArrayList<>();
        color.add(new SwitchRow(tr("tokia.opt.rainbow"), () -> c.crosshairRainbow, v -> c.crosshairRainbow = v));
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
        crosshairPages.add(new Page(tr("tokia.page.extras"), crossExtra));
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
        keysStyle.add(new StyleEditorRow(c.keys, false, false, false, false));
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
        armorStyle.add(new StyleEditorRow(c.armor, false, false, true, false));
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
        effectStyle.add(new StyleEditorRow(c.effects, false, false, true, false));
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

        // Extras: Bewegung, Warnungen, Werkzeuge
        List<Row> move = new ArrayList<>();
        move.add(new SwitchRow(tr("tokia.opt.auto_sprint"), () -> c.autoSprint, v -> c.autoSprint = v));
        move.add(new TextRow(tr("tokia.module.extras.sprint.text")));
        List<Row> warn = new ArrayList<>();
        warn.add(new SwitchRow(tr("tokia.opt.warn_armor"), () -> c.warnArmor, v -> c.warnArmor = v));
        warn.add(new SwitchRow(tr("tokia.opt.warn_tool"), () -> c.warnTool, v -> c.warnTool = v));
        warn.add(new SliderRow(tr("tokia.opt.warn_percent"), () -> c.warnPercent, v -> c.warnPercent = v, 5, 50, "%", 0));
        List<Row> tools = new ArrayList<>();
        tools.add(new TextRow(tr("tokia.module.extras.tools.text")));
        tools.add(new ButtonRow(tr("tokia.menu.clear_waypoints"), c::clearWaypoints));
        List<Page> extraPages = new ArrayList<>();
        extraPages.add(new Page(tr("tokia.page.movement"), move));
        extraPages.add(new Page(tr("tokia.page.warnings"), warn));
        extraPages.add(new Page(tr("tokia.page.tools"), tools));
        modules.add(new Module(tr("tokia.module.extras"), tr("tokia.module.extras.desc"),
            () -> c.autoSprint || c.warnArmor || c.warnTool, v -> {
                c.autoSprint = v;
                c.warnArmor = v;
                c.warnTool = v;
            }, c::resetExtras, false, extraPages));

        List<Row> designRows = new ArrayList<>();
        designRows.add(new TextRow(tr("tokia.design.accent_hint")));
        designRows.add(new SwatchRow(() -> c.menuAccentRgb, v -> c.menuAccentRgb = v));
        designRows.add(new SliderRow(tr("tokia.opt.red"), () -> (c.menuAccentRgb >> 16) & 0xFF,
            v -> c.menuAccentRgb = (c.menuAccentRgb & 0x00FFFF) | (v << 16), 0, 255, "", 0xFFFF6666));
        designRows.add(new SliderRow(tr("tokia.opt.green"), () -> (c.menuAccentRgb >> 8) & 0xFF,
            v -> c.menuAccentRgb = (c.menuAccentRgb & 0xFF00FF) | (v << 8), 0, 255, "", 0xFF66DD88));
        designRows.add(new SliderRow(tr("tokia.opt.blue"), () -> c.menuAccentRgb & 0xFF,
            v -> c.menuAccentRgb = (c.menuAccentRgb & 0xFFFF00) | v, 0, 255, "", 0xFF6699FF));
        designRows.add(new SliderRow(tr("tokia.design.rounding"), () -> c.menuRounding,
            v -> c.menuRounding = v, 0, 12, "", 0));
        List<Page> designPages = new ArrayList<>();
        designPages.add(new Page("", designRows));
        modules.add(new Module(tr("tokia.module.design"), tr("tokia.module.design.desc"),
            () -> false, v -> {}, () -> { c.menuAccentRgb = 0x8B5CF6; c.menuRounding = 9; }, false, designPages));
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

        float open = Math.min(1f, (System.nanoTime() - openedAt) / 260_000_000f);
        open = 1f - (float)Math.pow(1f - open, 3);
        int pw = Math.min(this.width - 40, 920);
        int ph = Math.min(this.height - 36, 600);
        int px = (this.width - pw) / 2;
        int py = (this.height - ph) / 2 + Math.round((1f - open) * 12);

        // Sanftes Overlay und abgerundete, leicht schwebende Oberflaeche.
        g.fill(0, 0, this.width, this.height, 0x78060910);
        rounded(g, px - 3, py - 3, pw + 6, ph + 6, 28, 0x4021263A);
        rounded(g, px - 1, py - 1, pw + 2, ph + 2, 27, BORDER);
        rounded(g, px, py, pw, ph, 26, PANEL);
        rounded(g, px + 1, py + 1, SIDE_W + 14, ph - 2, 25, SIDEBAR);
        g.fill(px + SIDE_W + 14, py + 28, px + SIDE_W + 15, py + ph - 28, 0xFF282B38);
        rounded(g, px + 18, py + 48, 48, 3, 2, accent());

        // Hub navigation uses feature categories; individual features live in visual cards.
        g.text(this.font, "TOKIA", px + 18, py + 16, TEXT, true);
        g.text(this.font, "CLIENT HUB", px + 18, py + 31, SUB, false);
        rounded(g, px + 18, py + 48, 48, 3, 2, accent());
        String[] categories = {"Alle", "HUD", "Gameplay", "Design"};
        for (int i = 0; i < categories.length; i++) {
            final int idx = i;
            int iy = py + 70 + i * 42;
            boolean sel = selectedCategory == i;
            rounded(g, px + 10, iy, SIDE_W - 20, 34, 14,
                sel ? accentSurface() : inside(px + 10, iy, SIDE_W - 20, 34) ? HOVER : 0x00000000);
            if (sel) rounded(g, px + 10, iy + 9, 3, 16, 2, accent());
            g.text(this.font, categories[i], px + 23, iy + 13, sel ? TEXT : SUB, sel);
            hit(px + 10, iy, SIDE_W - 20, 34, () -> selectedCategory = idx);
        }
        int ebx = px + 8;
        int eby = py + ph - 54;
        int ebw = SIDE_W - 24;
        String editLabel = tr("tokia.menu.edit_hud");
        rounded(g, ebx, eby, ebw, 30, 13, inside(ebx, eby, ebw, 30) ? 0xFF343047 : 0xFF242631);
        g.text(this.font, editLabel, ebx + (ebw - this.font.width(editLabel)) / 2, eby + 10, TEXT, false);
        hit(ebx, eby, ebw, 30, () -> this.minecraft.gui.setScreen(new HudEditorScreen(this)));
        g.text(this.font, tr("tokia.menu.hint"), px + 18, py + ph - 16, SUB, false);

        if (selectedModule < 0) {
            drawHub(g, px + SIDE_W + 36, py + 20, pw - SIDE_W - 58, ph - 40);
            return;
        }
        if (selectedModule >= modules.size()) selectedModule = -1;
        final Module m = modules.get(selectedModule);
        drawModuleSettings(g, px + SIDE_W + 36, py + 18, pw - SIDE_W - 58, ph - 36, m);
    }

    private void drawHub(GuiGraphicsExtractor g, int cx, int top, int cw, int height) {
        g.text(this.font, "Tokia Features", cx, top, TEXT, true);
        g.text(this.font, "Deine Tools auf einen Blick", cx, top + 15, SUB, false);
        int count = 0;
        for (int i = 0; i < modules.size(); i++) {
            if (belongsToCategory(i, selectedCategory)) count++;
        }
        String countLabel = count + " Features";
        int badgeW = this.font.width(countLabel) + 20;
        rounded(g, cx + cw - badgeW, top - 3, badgeW, 22, 11, 0xFF20232F);
        g.text(this.font, countLabel, cx + cw - badgeW + 10, top + 4, SUB, false);

        int gap = 12;
        int cols = cw >= 600 ? 3 : cw >= 390 ? 2 : 1;
        int cardW = (cw - gap * (cols - 1)) / cols;
        int cardH = Math.min(142, Math.max(116, (height - 38 - gap) / 2));
        int indexInGrid = 0;
        for (int i = 0; i < modules.size(); i++) {
            if (!belongsToCategory(i, selectedCategory)) continue;
            final int idx = i;
            Module m = modules.get(i);
            int col = indexInGrid % cols;
            int row = indexInGrid / cols;
            int x = cx + col * (cardW + gap);
            int y = top + 40 + row * (cardH + gap);
            boolean hover = inside(x, y, cardW, cardH);
            rounded(g, x, y + (hover ? -1 : 0), cardW, cardH, 16, hover ? 0xFF292D3B : 0xFF20232F);
            rounded(g, x + 1, y + (hover ? 0 : 1), cardW - 2, cardH - 2, 15, hover ? 0xFF252936 : 0xFF222530);
            drawFeatureIcon(g, x + cardW / 2, y + 39, i);
            String title = m.name;
            int tx = x + (cardW - font.width(title)) / 2;
            g.text(font, title, tx, y + 68, TEXT, true);
            String desc = trim(m.desc, cardW - 22);
            g.text(font, desc, x + (cardW - font.width(desc)) / 2, y + 82, SUB, false);
            int footerY = y + cardH - 28;
            boolean enabled = m.enabled.getAsBoolean();
            rounded(g, x + 1, footerY, cardW - 2, 27, 0,
                enabled ? accent() : 0xFF2A2D39);
            String action = enabled ? "AKTIV" : "OPTIONEN  ›";
            g.text(font, action, x + (cardW - font.width(action)) / 2, footerY + 9,
                enabled ? TEXT : SUB, enabled);
            // Main body opens customization; footer toggles the feature directly.
            hit(x, y, cardW, cardH - 29, () -> { selectedModule = idx; selectedPage = 0; });
            hit(x, footerY, cardW, 27, () -> m.setEnabled.accept(!m.enabled.getAsBoolean()));
            indexInGrid++;
        }
    }

    private boolean belongsToCategory(int module, int category) {
        if (category == 0) return true;
        if (category == 1) return module >= 1 && module <= 4;
        if (category == 2) return module == 0 || module == 5 || module == 6;
        return module == 7;
    }

    private String trim(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) return value;
        String result = value;
        while (!result.isEmpty() && font.width(result + "…") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "…";
    }

    /** Small original pictograms, drawn from clean geometric shapes without external assets. */
    private void drawFeatureIcon(GuiGraphicsExtractor g, int x, int y, int type) {
        int bg = accentSurface();
        rounded(g, x - 23, y - 23, 46, 46, 16, bg);
        int white = 0xFFEAF0FF;
        int tint = accent();
        switch (type) {
            case 0 -> { // Crosshair
                rounded(g, x - 2, y - 15, 4, 10, 2, white); rounded(g, x - 2, y + 5, 4, 10, 2, white);
                rounded(g, x - 15, y - 2, 10, 4, 2, white); rounded(g, x + 5, y - 2, 10, 4, 2, white);
                rounded(g, x - 2, y - 2, 4, 4, 2, tint);
            }
            case 1 -> { // HUD / information bars
                for (int k = 0; k < 3; k++) {
                    rounded(g, x - 13, y - 12 + k * 9, 26, 6, 3, 0xFF343A4B);
                    rounded(g, x - 10, y - 10 + k * 9, 4, 2, 1, k == 1 ? 0xFF67D6B1 : tint);
                    rounded(g, x - 4, y - 10 + k * 9, 14 - k * 2, 2, 1, white);
                }
            }
            case 2 -> { // Keystrokes
                rounded(g, x - 15, y - 10, 30, 22, 6, 0xFF343A4B);
                for (int k = 0; k < 3; k++) rounded(g, x - 10 + k * 8, y - 5, 5, 5, 2, k == 1 ? tint : white);
                rounded(g, x - 10, y + 3, 21, 5, 2, white);
            }
            case 3 -> { // Armor shield
                rounded(g, x - 12, y - 14, 24, 26, 9, tint);
                rounded(g, x - 8, y - 10, 16, 17, 6, white);
                g.fill(x - 6, y + 4, x + 1, y + 10, tint); g.fill(x + 1, y + 2, x + 7, y + 8, tint);
            }
            case 4 -> { // Effects bottle
                rounded(g, x - 6, y - 15, 12, 6, 2, white);
                rounded(g, x - 4, y - 9, 8, 4, 2, 0xFF69748C);
                rounded(g, x - 12, y - 5, 24, 20, 7, tint);
                rounded(g, x - 8, y + 1, 16, 8, 4, 0xFFA78BFA);
                rounded(g, x + 9, y - 15, 3, 3, 1, white);
            }
            case 5 -> { // Zoom lens
                rounded(g, x - 14, y - 14, 24, 24, 12, white);
                rounded(g, x - 9, y - 9, 14, 14, 7, bg);
                rounded(g, x + 5, y + 6, 13, 5, 2, tint);
            }
            case 6 -> { // Speed / extras
                rounded(g, x - 12, y - 13, 24, 26, 10, 0xFF343A4B);
                g.fill(x + 1, y - 14, x + 5, y - 2, tint); g.fill(x - 5, y - 3, x + 3, y + 1, tint);
                g.fill(x - 3, y, x + 1, y + 13, tint);
                rounded(g, x - 17, y - 1, 5, 3, 1, white); rounded(g, x + 12, y + 5, 5, 3, 1, white);
            }
            default -> { // Theme palette
                rounded(g, x - 13, y - 12, 26, 24, 12, white);
                rounded(g, x - 8, y - 7, 7, 7, 4, tint);
                rounded(g, x + 2, y - 7, 6, 6, 3, 0xFF67D6B1);
                rounded(g, x - 8, y + 2, 6, 6, 3, 0xFFFFC66D);
                rounded(g, x + 1, y + 2, 6, 6, 3, 0xFFFF7B8B);
            }
        }
    }

    private void drawModuleSettings(GuiGraphicsExtractor g, int cx, int top, int cw, int height, Module m) {
        if (selectedPage >= m.pages.size()) {
            selectedPage = 0;
        }
        rounded(g, cx, top, 72, 24, 12, inside(cx, top, 72, 24) ? 0xFF343047 : 0xFF242631);
        String back = "‹  ZURÜCK";
        g.text(font, back, cx + 10, top + 8, SUB, false);
        hit(cx, top, 72, 24, () -> selectedModule = -1);
        g.text(this.font, m.name, cx + 86, top + 1, TEXT, true);
        g.text(this.font, m.desc, cx + 86, top + 16, SUB, false);

        String resetLabel = tr("tokia.menu.reset");
        int rw = this.font.width(resetLabel) + 22;
        int rx = cx + cw - rw;
        int ry = top;
        rounded(g, rx, ry, rw, 27, 12, inside(rx, ry, rw, 27) ? 0xFF343047 : 0xFF242631);
        g.text(this.font, resetLabel, rx + 11, ry + 10, SUB, false);
        hit(rx, ry, rw, 27, m.reset);

        int rowsY = top + 50;
        if (m.pages.size() > 1) {
            int tx = cx;
            for (int i = 0; i < m.pages.size(); i++) {
                final int idx = i;
                String name = m.pages.get(i).name;
                int tw = this.font.width(name) + 28;
                boolean sel = i == selectedPage;
                int bg = sel ? accentSurface() : (inside(tx, top + 38, tw, 26) ? 0xFF292C39 : 0xFF1B1D28);
                rounded(g, tx, top + 38, tw, 26, 13, bg);
                g.text(this.font, name, tx + 14, top + 48, sel ? TEXT : SUB, false);
                hit(tx, top + 38, tw, 26, () -> selectedPage = idx);
                tx += tw + 8;
            }
            rowsY = top + 78;
        }

        boolean preview = m.preview && cw >= 250;
        int rowsW = preview ? cw - 148 : cw;
        int yy = rowsY;
        for (Row row : m.pages.get(selectedPage).rows) {
            int hitStart = hits.size();
            int contentHeight = row.render(g, cx + 14, yy + 10, rowsW - 28);
            while (hits.size() > hitStart) hits.remove(hits.size() - 1);
            int cardHeight = Math.max(38, contentHeight + 20);
            rounded(g, cx, yy, rowsW, cardHeight, 16, 0xFF1B1E29);
            // Redraw row above the card: cards sit behind their controls.
            row.render(g, cx + 14, yy + 10, rowsW - 28);
            yy += cardHeight + 10;
        }
        if (preview) {
            drawPreview(g, cx + cw - 122, rowsY);
        }
    }

    /** Rounded rectangles drawn as scanlines, so this works without custom textures or shaders. */
    private void rounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;
        int userRadius = TokiaConfig.get().menuRounding;
        int r = Math.max(0, Math.min(Math.round(radius * (userRadius / 12f)), Math.min(w, h) / 2));
        for (int row = 0; row < h; row++) {
            int inset = 0;
            if (row < r) {
                double dy = r - row - 0.5;
                inset = r - (int)Math.floor(Math.sqrt(Math.max(0, r * r - dy * dy)));
            } else if (row >= h - r) {
                double dy = row - (h - r) + 0.5;
                inset = r - (int)Math.floor(Math.sqrt(Math.max(0, r * r - dy * dy)));
            }
            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
        }
    }

    private void drawPreview(GuiGraphicsExtractor g, int x, int y) {
        int s = 90;
        g.text(this.font, tr("tokia.menu.preview"), x, y, SUB, false);
        int by = y + 12;
        rounded(g, x - 1, by - 1, s + 2, s + 2, 9, BORDER);
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
            rounded(g, x + 2, y + 1, w - 4, 18, 6, HOVER);
            }
            g.text(font, label, x + 6, y + 6, TEXT, false);
            boolean on = get.getAsBoolean();
            int sx = x + w - 30;
            int sy = y + 5;
            float target = on ? 1f : 0f;
            float current = switchMotion.getOrDefault(this, target);
            current += (target - current) * 0.28f;
            switchMotion.put(this, current);
            rounded(g, sx, sy, 26, 12, 6, on ? accent() : OFF);
            int kx = sx + 2 + Math.round(current * 12);
            rounded(g, kx, sy + 2, 8, 8, 4, 0xFFFFFFFF);
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
            rounded(g, bx, by, bw, 4, 2, OFF);
            float f = (value - min) / (float) (max - min);
            int fx = bx + Math.round(f * bw);
            rounded(g, bx, by, Math.max(0, fx - bx), 4, 2, tint != 0 ? tint : accent());
            rounded(g, fx - 3, by - 2, 7, 8, 4, 0xFFFFFFFF);
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
                int bg = sel ? accent() : (inside(sx, sy, segW, 14) ? 0xFF2E2E42 : 0xFF1C1C2B);
                rounded(g, sx, sy, segW - 3, 16, 5, bg);
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
                    rounded(g, sx - 2, sy - 2, size + 4, size + 4, 5, 0xFFFFFFFF);
                } else if (inside(sx, sy, size, size)) {
                    rounded(g, sx - 1, sy - 1, size + 2, size + 2, 5, SUB);
                }
                rounded(g, sx, sy, size, size, 4, 0xFF000000 | rgb);
                hit(sx, sy, size, size, () -> set.accept(rgb));
            }
            return 42;
        }
    }

    /** Zwei Spalten: links Schalter und Deckkraft, rechts Farbwahl (Palette + RGB). */
    private final class StyleEditorRow extends Row {
        final List<Row> left = new ArrayList<>();
        final List<Row> right = new ArrayList<>();

        StyleEditorRow(WidgetCfg cfg, boolean enabledRow, boolean labelRow, boolean shadowRow, boolean dynamicRow) {
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
            if (dynamicRow) {
                left.add(new SwitchRow(tr("tokia.opt.dynamic_color"), () -> cfg.dynamicColor, v -> cfg.dynamicColor = v));
            }
            right.add(new SwitchRow(tr("tokia.opt.rainbow"), () -> cfg.rainbow, v -> cfg.rainbow = v));
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
                editors.add(new StyleEditorRow(d.cfg.apply(c), true, true, true, d.supportsDynamicColor()));
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
                rounded(g, tx, ty, tileW, 22, 7, inside(tx, ty, tileW, 22) ? 0xFF292936 : 0xFF1B1B27);
                boolean on = c.enabled;
                rounded(g, tx + 6, ty + 7, 20, 9, 5, on ? accent() : OFF);
                int kx = on ? tx + 16 : tx + 8;
                rounded(g, kx, ty + 8, 6, 6, 3, 0xFFFFFFFF);
                g.text(font, tr(d.nameKey), tx + 30, ty + 7, on ? TEXT : SUB, false);
                hit(tx, ty, tileW, 22, () -> c.enabled = !c.enabled);
                int ex = tx + tileW - ew - 4;
                rounded(g, ex, ty + 4, ew, 14, 5, inside(ex, ty + 4, ew, 14) ? 0xFF343047 : 0xFF252534);
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
            rounded(g, x, y, bw, 16, 6, inside(x, y, bw, 16) ? 0xFF343047 : 0xFF242431);
            g.text(font, back, x + 6, y + 3, SUB, false);
            hit(x, y, bw, 14, () -> selected = -1);
            g.text(font, tr(d.nameKey), x + bw + 8, y + 3, TEXT, true);

            int py2 = y + 20;
            g.fill(x, py2, x + w, py2 + 24, 0xFF3A4A5A);
            InfoHud.drawBox(g, minecraft, c, InfoHud.text(d, minecraft, c), InfoHud.colorFor(d, minecraft, c), x + 8, py2 + 5);
            return 50 + editors.get(selected).render(g, x, y + 50, w);
        }
    }

    private final class ButtonRow extends Row {
        final String label;
        final Runnable action;

        ButtonRow(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }

        @Override
        int render(GuiGraphicsExtractor g, int x, int y, int w) {
            int bw = font.width(label) + 16;
            rounded(g, x + 6, y + 3, bw, 18, 6, inside(x + 6, y + 3, bw, 18) ? 0xFF343047 : 0xFF242431);
            g.text(font, label, x + 14, y + 7, TEXT, false);
            hit(x + 6, y + 3, bw, 16, action);
            return 22;
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

