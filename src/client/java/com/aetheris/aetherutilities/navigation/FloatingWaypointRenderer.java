
package com.aetheris.aetherutilities.navigation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class FloatingWaypointRenderer {

    private static final double MAX_RENDER_DISTANCE = 256.0;

    private static final float TEXT_SCALE = 1.0F / 40.0F;

    private FloatingWaypointRenderer() {
    }

    // -----------------------------------------------------
    // INITIALIZE
    // -----------------------------------------------------

    public static void initialize() {

        LevelRenderEvents.COLLECT_SUBMITS.register(
                FloatingWaypointRenderer::render
        );
    }

    // -----------------------------------------------------
    // RENDER
    // -----------------------------------------------------

    private static void render(LevelRenderContext context) {

        if (!NavigationConfig.isFloatingEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        Player player = minecraft.player;

        if (player == null || minecraft.level == null) {
            return;
        }

        Vec3 camera =
                context.levelState().cameraRenderState.pos;

        var cameraOrientation =
                context.levelState().cameraRenderState.orientation;

        Font font = minecraft.font;

        SubmitNodeCollector collector =
                context.submitNodeCollector();

        String currentDimension =
                minecraft.level.dimension().toString();

        for (NavigationWaypoint waypoint :
                NavigationConfig.getWaypoints()) {

            if (!waypoint.isEnabled()) {
                continue;
            }

            if (!waypoint.getDimension().equals(currentDimension)) {
                continue;
            }

            Vec3 target = new Vec3(
                    waypoint.getX() + 0.5,
                    waypoint.getY() + 0.5,
                    waypoint.getZ() + 0.5
            );

            double dx = target.x - player.getX();
            double dy = target.y - player.getY();
            double dz = target.z - player.getZ();

            double distance = Math.sqrt(
                    dx * dx + dy * dy + dz * dz
            );

            if (distance > MAX_RENDER_DISTANCE) {
                continue;
            }

            renderWaypoint(
                    waypoint,
                    distance,
                    target,
                    camera,
                    cameraOrientation,
                    font,
                    collector
            );
        }
    }

    // -----------------------------------------------------
    // RENDER SINGLE WAYPOINT
    // -----------------------------------------------------

    private static void renderWaypoint(
            NavigationWaypoint waypoint,
            double distance,
            Vec3 target,
            Vec3 camera,
            org.joml.Quaternionf cameraOrientation,
            Font font,
            SubmitNodeCollector collector
    ) {

        String distanceText;

        if (distance >= 1000.0) {

            distanceText = String.format(
                    "%.1f km",
                    distance / 1000.0
            );

        } else {

            distanceText = String.format(
                    "%.0f m",
                    distance
            );
        }

        String label =
                waypoint.getName()
                        + "  "
                        + distanceText;

        FormattedCharSequence circle =
                Component.literal("●").getVisualOrderText();

        FormattedCharSequence text =
                Component.literal(label).getVisualOrderText();

        float circleWidth = font.width(circle);
        float textWidth = font.width(text);

        float gap = 4.0F;

        float totalWidth =
                circleWidth + gap + textWidth;

        float startX = -totalWidth / 2.0F;

        PoseStack poseStack = new PoseStack();

        poseStack.translate(
                target.x - camera.x,
                target.y + 0.8 - camera.y,
                target.z - camera.z
        );

        poseStack.rotate(cameraOrientation);

        poseStack.scale(
                TEXT_SCALE,
                TEXT_SCALE,
                TEXT_SCALE
        );

        // COLORED CIRCLE

        collector.submitText(
                poseStack,
                startX,
                -4.0F,
                circle,
                true,
                Font.DisplayMode.NORMAL,
                0xF000F0,
                0xFF000000 | (waypoint.getColor() & 0xFFFFFF),
                0,
                0
        );

        // WAYPOINT NAME AND DISTANCE

        collector.submitText(
                poseStack,
                startX + circleWidth + gap,
                -4.0F,
                text,
                true,
                Font.DisplayMode.NORMAL,
                0xF000F0,
                0xFFFFFFFF,
                0,
                0
        );
    }
}
