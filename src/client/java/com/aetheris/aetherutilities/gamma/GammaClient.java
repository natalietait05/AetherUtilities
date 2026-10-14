
package com.aetheris.aetherutilities.gamma;

import com.aetheris.aetherutilities.client.AetherUtilitiesClient;
import com.aetheris.aetherutilities.client.AetherUtilitiesToast;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class GammaClient {

    public enum Mode {
        OFF,
        X3,
        X15
    }

    public static KeyMapping TOGGLE_KEY;

    // Estado inicial.
    private static Mode mode = Mode.X3;

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
                nextMode();
            }
        });
    }

    // Ciclo: OFF -> X3 -> X15 -> OFF.
    public static void nextMode() {

        mode = switch (mode) {
            case OFF -> Mode.X3;
            case X3 -> Mode.X15;
            case X15 -> Mode.OFF;
        };

        Minecraft minecraft = Minecraft.getInstance();

        minecraft.options.gamma().set(getGammaValue());

        String message = switch (mode) {
            case OFF -> "Gamma: OFF";
            case X3 -> "Gamma: x3";
            case X15 -> "Gamma: x15";
        };

        AetherUtilitiesToast.show(message);
    }

    public static Mode getMode() {
        return mode;
    }

    public static double getGammaValue() {
        return switch (mode) {
            case OFF -> 1.0;
            case X3 -> 3.0;
            case X15 -> 15.0;
        };
    }
}
