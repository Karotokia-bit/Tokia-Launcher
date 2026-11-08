package com.tokia.client.hud;

import com.tokia.client.config.TokiaConfig;
import com.tokia.client.config.WidgetCfg;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/** Liste aller verschiebbaren HUD-Elemente fuer den HUD-Editor. */
public final class HudModules {
    private HudModules() {
    }

    public static final class Entry {
        public final String nameKey;
        public final BooleanSupplier enabled;
        public final Supplier<int[]> size;
        public final DoubleSupplier getX;
        public final DoubleSupplier getY;
        public final BiConsumer<Double, Double> setPos;

        Entry(String nameKey, BooleanSupplier enabled, Supplier<int[]> size,
              DoubleSupplier getX, DoubleSupplier getY, BiConsumer<Double, Double> setPos) {
            this.nameKey = nameKey;
            this.enabled = enabled;
            this.size = size;
            this.getX = getX;
            this.getY = getY;
            this.setPos = setPos;
        }
    }

    private static Entry entry(String nameKey, WidgetCfg w, Supplier<int[]> size) {
        return new Entry(nameKey, () -> w.enabled, size, () -> w.x, () -> w.y, (x, y) -> {
            w.x = x;
            w.y = y;
        });
    }

    public static List<Entry> all() {
        TokiaConfig c = TokiaConfig.get();
        List<Entry> list = new ArrayList<>();
        for (InfoHud.Def d : InfoHud.DEFS) {
            list.add(entry(d.nameKey, d.cfg.apply(c), () -> InfoHud.size(d, Minecraft.getInstance())));
        }
        list.add(entry("tokia.module.keystrokes", c.keys, KeystrokesHud::size));
        list.add(entry("tokia.module.armor", c.armor, () -> ArmorHud.size(Minecraft.getInstance())));
        list.add(entry("tokia.module.effects", c.effects, () -> EffectsHud.size(Minecraft.getInstance())));
        return list;
    }
}
