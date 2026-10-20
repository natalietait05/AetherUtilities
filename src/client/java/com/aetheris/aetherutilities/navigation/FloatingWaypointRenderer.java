
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

        var cameraOrientation =
                context.levelState().cameraRenderState.orientation;

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

            renderWaypoint(
                    waypoint,
                    distance,
                    target,
                    camera,
                    cameraOrientation,
                    font,
                    collector,
                    screenHeight,
                    halfFovTangent
            );
        }
    }

    private static void renderWaypoint(
            NavigationWaypoint waypoint,
            double distance,
            Vec3 target,
            Vec3 camera,
            org.joml.Quaternionfc cameraOrientation,
            Font font,
            SubmitNodeCollector collector,
            int screenHeight,
            double halfFovTangent
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

        float gap = 4.0F;

        float totalWidth =
                circleWidth + gap + font.width(text);

        /*
         * Escala proporcional a la distancia.
         *
         * Esto compensa la perspectiva de la cámara:
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

        // Dirección desde el waypoint hacia la cámara.
        org.joml.Vector3f directionToCamera =
                new org.joml.Vector3f(
                        (float) (camera.x - target.x),
                        (float) (camera.y - target.y),
                        (float) (camera.z - target.z)
                );

        if (directionToCamera.lengthSquared() > 0.0001F) {

            directionToCamera.normalize();

            // Orienta la superficie del marcador hacia la cámara.
            org.joml.Quaternionf billboardRotation =
                    new org.joml.Quaternionf().rotationTo(
                            new org.joml.Vector3f(0.0F, 0.0F, 1.0F),
                            directionToCamera
                    );

            poseStack.rotate(billboardRotation);
        }

        poseStack.scale(
                textScale,
                textScale,
                textScale
        );

        // Círculo con el color configurado en el waypoint.
        collector.submitText(
                poseStack,
                -totalWidth / 2.0F,
                -4.0F,
                circle,
                true,
                Font.DisplayMode.NORMAL,
                0xF000F0,
                0xFF000000 | (waypoint.getColor() & 0xFFFFFF),
                0,
                0
        );

        // Nombre y distancia en blanco.
        collector.submitText(
                poseStack,
                -totalWidth / 2.0F + circleWidth + gap,
                -4.0F,
                text,
                true,
                Font.DisplayMode.NORMAL,
                0xF000F0,
                0xFFFFFFFF,
                0,
                0
        );

        poseStack.popPose();
    }
}
