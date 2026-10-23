
package com.aetheris.aetherutilities.navigation;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NavigationCompassRenderer {

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

    private static final float PIXELS_PER_DEGREE = 0.48F;

    private static final int COMPASS_WIDTH = 112;
    private static final int COMPASS_HEIGHT = 18;

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

        if (NavigationConfig.getCompassMode()
                != NavigationConfig.CompassMode.ON_TOP) {
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

        int centerX =
                minecraft.getWindow().getGuiScaledWidth() / 2;

        int left = centerX - COMPASS_WIDTH / 2;
        int top = 8;

        graphics.fill(
                left,
                top,
                left + COMPASS_WIDTH,
                top + COMPASS_HEIGHT,
                0x90000000
        );

        graphics.fill(
                left,
                top + COMPASS_HEIGHT - 1,
                left + COMPASS_WIDTH,
                top + COMPASS_HEIGHT,
                0x88777777
        );

        List<DirectionPosition> positions =
                new ArrayList<>();

        for (int i = 0; i < directions.length; i++) {

            float difference = wrapDegrees(
                    bearings[i] - heading
            );

            positions.add(
                    new DirectionPosition(
                            directions[i],
                            difference
                    )
            );
        }

        positions.sort(
                Comparator.comparingDouble(
                        position -> Math.abs(position.difference())
                )
        );

        if (positions.size() > 3) {
            positions = new ArrayList<>(
                    positions.subList(0, 3)
            );
        }

        positions.sort(
                Comparator.comparingDouble(
                        DirectionPosition::difference
                )
        );

        for (DirectionPosition position : positions) {

            int x = centerX + Math.round(
                    position.difference() * PIXELS_PER_DEGREE
            );

            boolean highlighted =
                    Math.abs(position.difference()) <= 22.5F;

            int color = highlighted
                    ? 0xFFFFD76A
                    : 0xFFDDDDDD;

            int markerColor = highlighted
                    ? 0xFFFFD76A
                    : 0xFF888888;

            graphics.fill(
                    x,
                    top + 3,
                    x + 1,
                    top + 8,
                    markerColor
            );

            graphics.centeredText(
                    minecraft.font,
                    Component.literal(position.name()),
                    x,
                    top + 9,
                    color
            );
        }

        graphics.fill(
                centerX - 1,
                top + 2,
                centerX + 1,
                top + COMPASS_HEIGHT - 3,
                0xFFFFFFFF
        );
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

    private record DirectionPosition(
            String name,
            float difference
    ) {
    }
}
