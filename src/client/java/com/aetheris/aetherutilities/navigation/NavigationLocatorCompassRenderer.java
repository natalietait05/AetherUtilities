
package com.aetheris.aetherutilities.navigation;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class NavigationLocatorCompassRenderer {

    private static final String[] DIRECTIONS_8 = {
            "N", "NE", "E", "SE",
            "S", "SW", "W", "NW"
    };

    private static final float[] BEARINGS_8 = {
            0.0F, 45.0F, 90.0F, 135.0F,
            180.0F, 225.0F, 270.0F, 315.0F
    };

    private static final String[] DIRECTIONS_4 = {
            "N", "E", "S", "W"
    };

    private static final float[] BEARINGS_4 = {
            0.0F, 90.0F, 180.0F, 270.0F
    };

    private static final float VISIBLE_DEGREES = 60.0F;

    private static final float PIXELS_PER_DEGREE =
            173.0F / 120.0F;

    private static final int BAR_WIDTH = 182;

    private NavigationLocatorCompassRenderer() {
    }

    public static void initialize() {

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.INFO_BAR,
                Identifier.fromNamespaceAndPath(
                        "aetherutilities",
                        "navigation_locator_compass"
                ),
                NavigationLocatorCompassRenderer::render
        );
    }

    private static void render(
            GuiGraphicsExtractor graphics,
            net.minecraft.client.DeltaTracker tickCounter
    ) {

        if (NavigationConfig.getCompassMode()
                != NavigationConfig.CompassMode.ON_LOCATOR_BAR) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        Player player = minecraft.player;

        if (player == null || minecraft.level == null) {
            return;
        }

        boolean eightDirections =
                NavigationConfig.isCompassEightDirections();

        String[] directions = eightDirections
                ? DIRECTIONS_8
                : DIRECTIONS_4;

        float[] bearings = eightDirections
                ? BEARINGS_8
                : BEARINGS_4;

        float heading = normalizeDegrees(
                player.getYRot() + 180.0F
        );

        int guiWidth = graphics.guiWidth();
        int guiHeight = graphics.guiHeight();

        int centerX = guiWidth / 2;

        int barLeft = centerX - BAR_WIDTH / 2;
        int barTop = guiHeight - 24 - 5;

        // Keep the direction labels just above the vanilla bar.
        int labelY = barTop - 10;

        for (int i = 0; i < directions.length; i++) {

            float difference = wrapDegrees(
                    bearings[i] - heading
            );

            if (Math.abs(difference) > VISIBLE_DEGREES) {
                continue;
            }

            int x = centerX + Math.round(
                    difference * PIXELS_PER_DEGREE
            );

            if (x < barLeft || x > barLeft + BAR_WIDTH) {
                continue;
            }

            boolean highlighted =
                    Math.abs(difference) <= 22.5F;

            int color = highlighted
                    ? 0xFFFFD76A
                    : 0xFFFFFFFF;

            graphics.centeredText(
                    minecraft.font,
                    Component.literal(directions[i]),
                    x,
                    labelY,
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
