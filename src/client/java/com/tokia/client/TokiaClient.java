package com.tokia.client;

import com.tokia.TokiaMod;
import com.tokia.client.config.TokiaConfig;
import com.tokia.client.gui.TokiaMenuScreen;
import com.tokia.client.hud.AlertHud;
import com.tokia.client.hud.ArmorHud;
import com.tokia.client.hud.CrosshairHud;
import com.tokia.client.hud.EffectsHud;
import com.tokia.client.hud.InfoHud;
import com.tokia.client.hud.KeystrokesHud;
import com.tokia.client.util.ClickCounter;
import com.tokia.client.util.PlayerStats;
import com.tokia.client.util.SprintHelper;
import com.tokia.client.util.Stopwatch;
import com.tokia.client.util.Waypoints;
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

    private static final KeyMapping STOPWATCH_TOGGLE = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.tokia.stopwatch_toggle", SDLScancode.SDL_SCANCODE_V, CATEGORY));

    private static final KeyMapping STOPWATCH_RESET = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.tokia.stopwatch_reset", SDLScancode.SDL_SCANCODE_G, CATEGORY));

    private static final KeyMapping WAYPOINT_ADD = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.tokia.waypoint_add", SDLScancode.SDL_SCANCODE_B, CATEGORY));

    private static final KeyMapping WAYPOINT_NEXT = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.tokia.waypoint_next", SDLScancode.SDL_SCANCODE_N, CATEGORY));

    private static final KeyMapping WAYPOINT_DELETE = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.tokia.waypoint_delete", SDLScancode.SDL_SCANCODE_M, CATEGORY));

    @Override
    public void onInitializeClient() {
        TokiaConfig.load();

        CrosshairHud.register();
        InfoHud.register();
        KeystrokesHud.register();
        ArmorHud.register();
        EffectsHud.register();
        AlertHud.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClickCounter.tick(client.options.keyAttack.isDown(), client.options.keyUse.isDown());
            if (client.player != null) {
                PlayerStats.tick(client.player.getX(), client.player.getZ());
            } else {
                PlayerStats.reset();
            }
            ZoomHandler.tick(client, ZOOM.isDown());
            SprintHelper.tick(client);
            while (STOPWATCH_TOGGLE.consumeClick()) {
                Stopwatch.toggle();
            }
            while (STOPWATCH_RESET.consumeClick()) {
                Stopwatch.reset();
            }
            while (WAYPOINT_ADD.consumeClick()) {
                Waypoints.add(client);
            }
            while (WAYPOINT_NEXT.consumeClick()) {
                Waypoints.next();
            }
            while (WAYPOINT_DELETE.consumeClick()) {
                Waypoints.deleteActive();
            }
            while (OPEN_MENU.consumeClick()) {
                client.gui.setScreen(new TokiaMenuScreen(null));
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(ZoomHandler::reset);

        TokiaMod.LOGGER.info("Tokia Client bereit - Menue mit Rechter Shift-Taste, Zoom mit C");
    }
}
