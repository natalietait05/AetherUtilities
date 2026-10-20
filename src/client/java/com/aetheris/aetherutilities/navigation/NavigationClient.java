
package com.aetheris.aetherutilities.navigation;

import com.aetheris.aetherutilities.client.AetherUtilitiesClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.InputConstants;


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
