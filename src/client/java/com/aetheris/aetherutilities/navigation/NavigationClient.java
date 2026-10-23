
package com.aetheris.aetherutilities.navigation;

import com.aetheris.aetherutilities.client.AetherUtilitiesClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;

public final class NavigationClient {

    public static KeyMapping NAVIGATION_KEY;

    private NavigationClient() {
    }

    public static void initialize() {

        NAVIGATION_KEY = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.aetherutilities.navigation",
                        InputConstants.KEY_M,
                        AetherUtilitiesClient.CATEGORY
                )
        );

        // Initialize floating world markers.
        FloatingWaypointRenderer.initialize();

        // Initialize native locator bar synchronization.
        NavigationLocatorSync.initialize();

        // Initialize compass HUD.
        NavigationCompassRenderer.initialize();

        // Navigation screen key.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (NAVIGATION_KEY.consumeClick()) {

                Screen parent = client.gui.screen();

                client.gui.setScreen(
                        new NavigationScreen(parent)
                );
            }
        });
    }
}
