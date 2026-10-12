package com.aetheris.aetherutilities.gamma;

import com.aetheris.aetherutilities.client.AetherUtilitiesClient;
import com.aetheris.aetherutilities.client.AetherUtilitiesToast;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;

public final class GammaClient {

    public static KeyMapping TOGGLE_KEY;

    private static boolean enabled = false;

    private GammaClient() {
    }

    public static void initialize() {

        TOGGLE_KEY = new KeyMapping(
                "key.aetherutilities.gamma",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_G,
                AetherUtilitiesClient.CATEGORY
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (TOGGLE_KEY.consumeClick()) {
                toggle();
            }
        });
    }

    public static void toggle() {

        enabled = !enabled;

        AetherUtilitiesToast.show(
                enabled ? "Gamma: ON" : "Gamma: OFF"
        );
    }

    public static boolean isEnabled() {
        return enabled;
    }
}