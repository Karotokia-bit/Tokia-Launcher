package com.tokia.client.gui;

import com.tokia.client.config.TokiaConfig;
import com.tokia.client.hud.HudLayout;
import com.tokia.client.hud.HudModules;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** HUD-Editor: Anzeigen mit der Maus an eine neue Stelle ziehen. */
public class HudEditorScreen extends Screen {
    private static final int ACCENT = 0xFF8B5CF6;
    private static final int SUB = 0xFF9A9AB0;

    private final Screen parent;
    private List<HudModules.Entry> entries = new ArrayList<>();
    private HudModules.Entry dragging;
    private int grabX;
    private int grabY;
    private int mx;
    private int my;
    private int resetX;
    private int resetY;
    private int resetW;

    public HudEditorScreen(Screen parent) {
        super(Component.translatable("tokia.menu.edit_hud"));
        this.parent = parent;
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    @Override
    protected void init() {
        entries = HudModules.all();
    }

    private boolean inside(int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        this.mx = mouseX;
        this.my = mouseY;
        if (entries.isEmpty()) {
            entries = HudModules.all();
        }

        if (dragging != null) {
            int[] s = dragging.size.get();
            double nx = HudLayout.fracX(mx - grabX, s[0], this.width);
            double ny = HudLayout.fracY(my - grabY, s[1], this.height);
            dragging.setPos.accept(nx, ny);
        }

        for (HudModules.Entry e : entries) {
            int[] s = e.size.get();
            int x = HudLayout.x(e.getX.getAsDouble(), s[0], this.width);
            int y = HudLayout.y(e.getY.getAsDouble(), s[1], this.height);
            boolean on = e.enabled.getAsBoolean();
            boolean active = e == dragging || inside(x, y, s[0], s[1]);
            int fill = on ? (active ? 0x808B5CF6 : 0x408B5CF6) : 0x40808080;
            int border = on ? ACCENT : 0xFF808080;
            g.fill(x, y, x + s[0], y + s[1], fill);
            g.fill(x - 1, y - 1, x + s[0] + 1, y, border);
            g.fill(x - 1, y + s[1], x + s[0] + 1, y + s[1] + 1, border);
            g.fill(x - 1, y, x, y + s[1], border);
            g.fill(x + s[0], y, x + s[0] + 1, y + s[1], border);
            String label = tr(e.nameKey);
            int ly = y >= 14 ? y - 11 : y + s[1] + 3;
            g.text(this.font, label, x, ly, 0xFFFFFFFF, true);
        }

        String title = tr("tokia.editor.hint");
        g.text(this.font, title, this.width / 2 - this.font.width(title) / 2, this.height / 2 - 4, 0xFFFFFFFF, true);

        String resetLabel = tr("tokia.editor.reset");
        resetW = this.font.width(resetLabel) + 16;
        resetX = this.width / 2 - resetW / 2;
        resetY = this.height / 2 + 10;
        g.fill(resetX, resetY, resetX + resetW, resetY + 16, inside(resetX, resetY, resetW, 16) ? 0xFF2E2E42 : 0xFF1C1C2B);
        g.text(this.font, resetLabel, resetX + 8, resetY + 4, SUB, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (inside(resetX, resetY, resetW, 16)) {
            TokiaConfig.get().resetPositions();
            TokiaConfig.save();
            return true;
        }
        for (int i = entries.size() - 1; i >= 0; i--) {
            HudModules.Entry e = entries.get(i);
            int[] s = e.size.get();
            int x = HudLayout.x(e.getX.getAsDouble(), s[0], this.width);
            int y = HudLayout.y(e.getY.getAsDouble(), s[1], this.height);
            if (inside(x, y, s[0], s[1])) {
                dragging = e;
                grabX = mx - x;
                grabY = my - y;
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
}
