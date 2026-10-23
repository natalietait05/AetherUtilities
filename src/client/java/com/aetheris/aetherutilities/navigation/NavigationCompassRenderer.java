
package com.aetheris.aetherutilities.navigation;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class NavigationCompassRenderer {

    private static final String[] DIRECTIONS = {
            "N", "NE", "E", "SE",
            "S", "SW", "W", "NW"
    };

    private static final float[] BEARINGS = {
            0.0F, 45.0F, 90.0F, 135.0F,
            180.0F, 225.0F, 270.0F, 315.0F
    };

    private static final float PIXELS_PER_DEGREE = 0.8F;

    private static final int COMPASS_WIDTH = 320;
    private static final int COMPASS_HEIGHT = 28;

    private NavigationCompassRenderer() {
    }

    public static void initialize() {

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(
                        "aetherutilities",
                        "navigation_compass"
                ),
                NavigationCompassRenderer::render
        );
    }

    private static void render(
            GuiGraphicsExtractor graphics,
            net.minecraft.client.DeltaTracker tickCounter
    ) {

        Minecraft minecraft = Minecraft.getInstance();

        Player player = minecraft.player;

        if (player == null || minecraft.level == null) {
            return;
        }

        if (!NavigationConfig.isCompassEnabled()) {
            return;
        }

        float yaw = player.getYRot();

        /*
         * Minecraft yaw:
         *
         *  0   = South
         * -90  = East
         *  90  = West
         * 180  = North
         *
         * Convertimos el yaw a una orientación
         * de brújula donde 0 grados representa Norte.
         */

        float heading = normalizeDegrees(yaw + 180.0F);

        int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;

        int left = centerX - COMPASS_WIDTH / 2;
        int top = 8;

        // Background.
        graphics.fill(
                left,
                top,
                left + COMPASS_WIDTH,
                top + COMPASS_HEIGHT,
                0x90000000
        );

        // Bottom border.
        graphics.fill(
                left,
                top + COMPASS_HEIGHT - 1,
                left + COMPASS_WIDTH,
                top + COMPASS_HEIGHT,
                0x88777777
        );

        /*
         * Center indicator.
         */

        graphics.fill(
                centerX - 1,
                top + 2,
                centerX + 1,
                top + COMPASS_HEIGHT - 3,
                0xFFFFFFFF
        );

        /*
         * Render all eight directions.
         */

        for (int i = 0; i < DIRECTIONS.length; i++) {

            float difference = wrapDegrees(
                    BEARINGS[i] - heading
            );

            int x = centerX
                    + Math.round(
                    difference * PIXELS_PER_DEGREE
            );

            if (x < left + 12 || x > left + COMPASS_WIDTH - 12) {
                continue;
            }

            boolean highlighted =
                    Math.abs(difference) <= 22.5F;

            int color = highlighted
                    ? 0xFFFFD76A
                    : 0xFFDDDDDD;

            int markerColor = highlighted
                    ? 0xFFFFD76A
                    : 0xFF888888;

            // Direction marker.
            graphics.fill(
                    x,
                    top + 5,
                    x + 1,
                    top + 11,
                    markerColor
            );

            // Direction label.
            graphics.centeredText(
                    minecraft.font,
                    Component.literal(DIRECTIONS[i]),
                    x,
                    top + 13,
                    color
            );
        }
    }

    private static float normalizeDegrees(float degrees) {

        return (degrees % 360.0F + 360.0F) % 360.0F;
    }

    private static float wrapDegrees(float degrees) {

        degrees %= 360.0F;

        if (degrees >= 180.0F) {
            degrees -= 360.0F;
        }

        if (degrees < -180.0F) {
            degrees += 360.0F;
        }

        return degrees;
    }
}
