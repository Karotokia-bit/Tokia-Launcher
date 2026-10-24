package com.tokia.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.gui.TokiaMenuScreen;
import com.tokia.client.hud.ArmorHud;
import com.tokia.client.hud.CrosshairHud;
import com.tokia.client.hud.EffectsHud;
import com.tokia.client.hud.InfoHud;
import com.tokia.client.hud.KeystrokesHud;
import com.tokia.client.util.ClickCounter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public class TokiaClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(TokiaMod.id("main"));

    private static final KeyMapping OPEN_MENU = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.tokia.open_menu", InputConstants.Type.KEYSYM, InputConstants.KEY_RSHIFT, CATEGORY));

    @Override
    public void onInitializeClient() {
        TokiaConfig.load();

        CrosshairHud.register();
        InfoHud.register();
        KeystrokesHud.register();
        ArmorHud.register();
        EffectsHud.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClickCounter.tick(client.options.keyAttack.isDown(), client.options.keyUse.isDown());
            while (OPEN_MENU.consumeClick()) {
                client.gui.setScreen(new TokiaMenuScreen(null));
            }
        });

        TokiaMod.LOGGER.info("Tokia Client bereit - Menue mit Rechter Shift-Taste");
    }
}
