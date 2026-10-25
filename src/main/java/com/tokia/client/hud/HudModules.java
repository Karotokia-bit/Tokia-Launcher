package com.tokia.client.hud;

import com.tokia.client.config.TokiaConfig;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/** Liste aller verschiebbaren HUD-Module fuer den HUD-Editor. */
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

    public static List<Entry> all() {
        TokiaConfig c = TokiaConfig.get();
        List<Entry> list = new ArrayList<>();
        list.add(new Entry("tokia.module.info", () -> c.infoEnabled,
            () -> InfoHud.size(Minecraft.getInstance()),
            () -> c.infoX, () -> c.infoY, (x, y) -> {
                c.infoX = x;
                c.infoY = y;
            }));
        list.add(new Entry("tokia.module.keystrokes", () -> c.keystrokes,
            KeystrokesHud::size,
            () -> c.keysX, () -> c.keysY, (x, y) -> {
                c.keysX = x;
                c.keysY = y;
            }));
        list.add(new Entry("tokia.module.armor", () -> c.armorHud,
            () -> ArmorHud.size(Minecraft.getInstance()),
            () -> c.armorX, () -> c.armorY, (x, y) -> {
                c.armorX = x;
                c.armorY = y;
            }));
        list.add(new Entry("tokia.module.effects", () -> c.effectsHud,
            () -> EffectsHud.size(Minecraft.getInstance()),
            () -> c.effectsX, () -> c.effectsY, (x, y) -> {
                c.effectsX = x;
                c.effectsY = y;
            }));
        list.add(new Entry("tokia.module.items", () -> c.itemCounter,
            () -> ItemCounterHud.size(Minecraft.getInstance()),
            () -> c.itemsX, () -> c.itemsY, (x, y) -> {
                c.itemsX = x;
                c.itemsY = y;
            }));
        return list;
    }
}
