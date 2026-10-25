package com.tokia.client;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.gui.TokiaMenuScreen;
import com.tokia.client.hud.ArmorHud;
import com.tokia.client.hud.CrosshairHud;
import com.tokia.client.hud.EffectsHud;
import com.tokia.client.hud.InfoHud;
import com.tokia.client.hud.ItemCounterHud;
import com.tokia.client.hud.KeystrokesHud;
import com.tokia.client.util.ClickCounter;
import com.tokia.client.util.CombatStats;
import com.tokia.client.util.FullbrightHandler;
import com.tokia.client.util.SprintHandler;
import com.tokia.client.util.PlayerStats;
import com.tokia.client.util.ZoomHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.sdl.SDLScancode;

public class TokiaClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(TokiaMod.id("main"));

    private static final KeyMapping OPEN_MENU = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.tokia.open_menu", SDLScancode.SDL_SCANCODE_RSHIFT, CATEGORY));

    private static final KeyMapping ZOOM = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.tokia.zoom", SDLScancode.SDL_SCANCODE_C, CATEGORY));

    @Override
    public void onInitializeClient() {
        TokiaConfig.load();

        CrosshairHud.register();
        InfoHud.register();
        KeystrokesHud.register();
        ArmorHud.register();
        EffectsHud.register();
        ItemCounterHud.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClickCounter.tick(client.options.keyAttack.isDown(), client.options.keyUse.isDown());
            if (client.player != null) {
                PlayerStats.tick(client.player.getX(), client.player.getZ());
            } else {
                PlayerStats.reset();
            }
            ZoomHandler.tick(client, ZOOM.isDown());
            CombatStats.tick(client, client.options.keyAttack.isDown());
            SprintHandler.tick(client);
            FullbrightHandler.tick(client);
            while (OPEN_MENU.consumeClick()) {
                client.gui.setScreen(new TokiaMenuScreen(null));
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(ZoomHandler::reset);
        ClientLifecycleEvents.CLIENT_STOPPING.register(FullbrightHandler::reset);

        TokiaMod.LOGGER.info("Tokia Client bereit - Menue mit Rechter Shift-Taste, Zoom mit C");
    }
}
