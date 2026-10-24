package com.tokia.client.gui;

import com.tokia.client.config.TokiaConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Einstellungsmenue von Tokia (Standard-Taste: Rechte Shift-Taste). */
public class TokiaMenuScreen extends Screen {
    private static final int COLS = 3;
    private static final int BW = 110;
    private static final int BH = 20;
    private static final int GAP_X = 5;
    private static final int STEP_Y = 24;
    private static final int TOP = 44;
    private static final int ROWS = 7;

    private final Screen parent;

    public TokiaMenuScreen(Screen parent) {
        super(Component.translatable("tokia.menu.title"));
        this.parent = parent;
    }

    private int colX(int col) {
        int total = COLS * BW + (COLS - 1) * GAP_X;
        return (this.width - total) / 2 + col * (BW + GAP_X);
    }

    private int rowY(int row) {
        return TOP + row * STEP_Y;
    }

    @Override
    protected void init() {
        TokiaConfig c = TokiaConfig.get();

        // Spalte 0: Crosshair
        toggle(0, 0, "crosshair", () -> c.crosshairEnabled, v -> c.crosshairEnabled = v);
        cycle(0, 1, "style", "tokia.style.", () -> c.crosshairStyle, v -> c.crosshairStyle = v, TokiaConfig.STYLE_COUNT);
        cycle(0, 2, "color", "tokia.color.", () -> c.crosshairColor, v -> c.crosshairColor = v, TokiaConfig.COLORS.length);
        number(0, 3, "size", () -> c.crosshairSize, v -> c.crosshairSize = v, 1, 15);
        number(0, 4, "thickness", () -> c.crosshairThickness, v -> c.crosshairThickness = v, 1, 4);
        number(0, 5, "gap", () -> c.crosshairGap, v -> c.crosshairGap = v, 0, 10);
        toggle(0, 6, "outline", () -> c.crosshairOutline, v -> c.crosshairOutline = v);

        // Spalte 1: Info-HUD
        toggle(1, 0, "info", () -> c.infoEnabled, v -> c.infoEnabled = v);
        toggle(1, 1, "fps", () -> c.showFps, v -> c.showFps = v);
        toggle(1, 2, "ping", () -> c.showPing, v -> c.showPing = v);
        toggle(1, 3, "coords", () -> c.showCoords, v -> c.showCoords = v);
        toggle(1, 4, "cps", () -> c.showCps, v -> c.showCps = v);
        toggle(1, 5, "clock", () -> c.showClock, v -> c.showClock = v);
        cycle(1, 6, "corner", "tokia.corner.", () -> c.infoCorner, v -> c.infoCorner = v, TokiaConfig.CORNER_COUNT);

        // Spalte 2: weitere Module
        toggle(2, 0, "keystrokes", () -> c.keystrokes, v -> c.keystrokes = v);
        toggle(2, 1, "armor", () -> c.armorHud, v -> c.armorHud = v);
        toggle(2, 2, "effects", () -> c.effectsHud, v -> c.effectsHud = v);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
            .bounds(this.width / 2 - 100, rowY(ROWS) + 6, 200, BH).build());
    }

    private static Component entry(String key, Component value) {
        return Component.translatable("tokia.menu.entry", Component.translatable("tokia.menu." + key), value);
    }

    private static Component onOff(boolean value) {
        return Component.translatable(value ? "options.on" : "options.off");
    }

    private void toggle(int col, int row, String key, BooleanSupplier get, Consumer<Boolean> set) {
        this.addRenderableWidget(Button.builder(entry(key, onOff(get.getAsBoolean())), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(entry(key, onOff(get.getAsBoolean())));
            TokiaConfig.save();
        }).bounds(colX(col), rowY(row), BW, BH).build());
    }

    private void cycle(int col, int row, String key, String valuePrefix, IntSupplier get, IntConsumer set, int count) {
        this.addRenderableWidget(Button.builder(
            entry(key, Component.translatable(valuePrefix + Math.floorMod(get.getAsInt(), count))), b -> {
                set.accept((get.getAsInt() + 1) % count);
                b.setMessage(entry(key, Component.translatable(valuePrefix + Math.floorMod(get.getAsInt(), count))));
                TokiaConfig.save();
            }).bounds(colX(col), rowY(row), BW, BH).build());
    }

    private void number(int col, int row, String key, IntSupplier get, IntConsumer set, int min, int max) {
        this.addRenderableWidget(Button.builder(entry(key, Component.literal(String.valueOf(get.getAsInt()))), b -> {
            int next = get.getAsInt() + 1;
            set.accept(next > max ? min : next);
            b.setMessage(entry(key, Component.literal(String.valueOf(get.getAsInt()))));
            TokiaConfig.save();
        }).bounds(colX(col), rowY(row), BW, BH).build());
    }

    @Override
    public void onClose() {
        TokiaConfig.save();
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        String title = this.title.getString();
        graphics.text(this.font, title, this.width / 2 - this.font.width(title) / 2, 14, 0xFFFFFFFF, true);
        header(graphics, 0, "tokia.menu.cat.crosshair");
        header(graphics, 1, "tokia.menu.cat.info");
        header(graphics, 2, "tokia.menu.cat.modules");
    }

    private void header(GuiGraphicsExtractor graphics, int col, String key) {
        graphics.text(this.font, Component.translatable(key).getString(), colX(col), TOP - 14, 0xFFAAAAAA, true);
    }
}
