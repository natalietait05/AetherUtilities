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

    // Altura deseada del texto en píxeles de pantalla.
    private static final double TARGET_TEXT_HEIGHT_PIXELS = 18.0;

    // Ángulo máximo para mostrar el nombre y la distancia.
    private static final double LABEL_ANGLE_DEGREES = 12.0;

    private FloatingWaypointRenderer() {
    }

    public static void initialize() {
        LevelRenderEvents.COLLECT_SUBMITS.register(
                FloatingWaypointRenderer::render
        );

        System.out.println(
                "[AetherUtilities Navigation] Floating renderer initialized"
        );
    }

    private static void render(LevelRenderContext context) {

        Minecraft minecraft = Minecraft.getInstance();

        Player player = minecraft.player;

        if (player == null || minecraft.level == null) {
            return;
        }

        if (!NavigationConfig.isFloatingEnabled()) {
            return;
        }

        Vec3 camera =
                context.levelState().cameraRenderState.pos;

        Font font = minecraft.font;

        SubmitNodeCollector collector =
                context.submitNodeCollector();

        String dimension =
                minecraft.level.dimension().toString();

        int screenHeight =
                Math.max(1, minecraft.getWindow().getHeight());

        double fovRadians = Math.toRadians(
                minecraft.options.fov().get()
        );

        double halfFovTangent =
                Math.tan(fovRadians / 2.0);

        Vec3 lookDirection =
                player.getViewVector(1.0F).normalize();

        for (NavigationWaypoint waypoint :
                NavigationConfig.getWaypoints()) {

            if (!waypoint.isEnabled()) {
                continue;
            }

            if (!waypoint.getDimension().equals(dimension)) {
                continue;
            }

            Vec3 target = new Vec3(
                    waypoint.getX() + 0.5,
                    waypoint.getY() + 0.5,
                    waypoint.getZ() + 0.5
            );

            double distance = camera.distanceTo(target);

            if (distance > MAX_RENDER_DISTANCE) {
                continue;
            }

            Vec3 directionToWaypoint =
                    target.subtract(camera).normalize();

            boolean showLabel =
                    lookDirection.dot(directionToWaypoint)
                            >= Math.cos(
                            Math.toRadians(LABEL_ANGLE_DEGREES)
                    );

            renderWaypoint(
                    waypoint,
                    distance,
                    target,
                    camera,
                    font,
                    collector,
                    screenHeight,
                    halfFovTangent,
                    showLabel
            );
        }
    }

    private static void renderWaypoint(
            NavigationWaypoint waypoint,
            double distance,
            Vec3 target,
            Vec3 camera,
            Font font,
            SubmitNodeCollector collector,
            int screenHeight,
            double halfFovTangent,
            boolean showLabel
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
                waypoint.getName() + "  " + distanceText;

        FormattedCharSequence circle =
                Component.literal("●").getVisualOrderText();

        FormattedCharSequence text =
                Component.literal(label).getVisualOrderText();

        float circleWidth = font.width(circle);

        float gap = 5.0F;

        float textWidth = font.width(text);

        float totalWidth =
                circleWidth + gap + textWidth;

        /*
         * Escala proporcional a la distancia.
         *
         * Compensa la perspectiva de la cámara:
         * cuanto más lejos está el waypoint, mayor es
         * su tamaño en el mundo para mantener el mismo
         * tamaño aparente en pantalla.
         */

        double worldUnitsPerPixel =
                (2.0 * distance * halfFovTangent)
                        / screenHeight;

        float textScale = (float) (
                worldUnitsPerPixel
                        * (TARGET_TEXT_HEIGHT_PIXELS / 9.0)
        );

        PoseStack poseStack = new PoseStack();

        poseStack.pushPose();

        // Posición relativa a la cámara.
        poseStack.translate(
                target.x - camera.x,
                target.y + 1.01 - camera.y,
                target.z - camera.z
        );

        /*
         * Billboard con yaw y pitch explícitos.
         *
         * Evita el roll que puede producir rotationTo()
         * y mantiene el texto orientado hacia la cámara.
         */

        double dx = camera.x - target.x;
        double dy = camera.y - target.y;
        double dz = camera.z - target.z;

        double horizontalDistance =
                Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.atan2(-dx, -dz);

        float pitch = (float) Math.atan2(
                dy,
                horizontalDistance
        );

        org.joml.Quaternionf billboardRotation =
                new org.joml.Quaternionf()
                        .rotationY(yaw)
                        .rotateX(pitch);

        poseStack.rotate(billboardRotation);

        // Escala adaptativa.
        poseStack.scale(
                -textScale,
                -textScale,
                textScale
        );

        /*
         * CÍRCULO
         *
         * Siempre visible, incluso cuando el jugador
         * no está mirando directamente el waypoint.
         */

        collector.submitText(
                poseStack,
                -circleWidth / 2.0F,
                -4.0F,
                circle,
                false,
                Font.DisplayMode.SEE_THROUGH,
                0xF000F0,
                0xFF000000 | (waypoint.getColor() & 0xFFFFFF),
                0,
                0
        );

        /*
         * NOMBRE Y DISTANCIA
         *
         * Solo aparecen cuando el jugador apunta
         * suficientemente cerca del waypoint.
         */

        if (showLabel) {

            float labelStart =
                    -totalWidth / 2.0F + circleWidth + gap;

            collector.submitText(
                    poseStack,
                    labelStart,
                    -4.0F,
                    text,
                    false,
                    Font.DisplayMode.SEE_THROUGH,
                    0xF000F0,
                    0xFFFFFFFF,
                    0,
                    0xA0000000
            );
        }

        poseStack.popPose();
    }
}