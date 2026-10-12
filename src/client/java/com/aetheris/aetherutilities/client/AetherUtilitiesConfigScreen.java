package com.aetheris.aetherutilities.client;

import com.aetheris.aetherutilities.gamma.GammaClient;
import com.aetheris.aetherutilities.lightlevels.LightLevelsClient;
import com.aetheris.aetherutilities.lightlevels.LightLevelsConfig;
import com.aetheris.aetherutilities.lightlevels.LightLevelsRenderer;
import com.aetheris.aetherutilities.zoom.ZoomClient;
import com.aetheris.aetherutilities.zoom.ZoomConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public final class AetherUtilitiesConfigScreen extends Screen {

    private final Screen parent;

    private Button lightLevelsKeyButton;
    private Button zoomKeyButton;
    private Button gammaKeyButton;

    private KeyMapping waitingForKey;

    public AetherUtilitiesConfigScreen(Screen parent) {
        super(Component.literal("AetherUtilities"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        int buttonWidth = 140;
        int gap = 8;

        int leftX = centerX - buttonWidth - gap / 2;
        int rightX = centerX + gap / 2;

        int startY = 50;

        // ============================================================
        // LIGHT LEVELS
        // ============================================================

        Button lightLevelsLabel = this.addRenderableWidget(
                Button.builder(Component.literal("Light Levels"), button -> {})
                        .bounds(leftX, startY, buttonWidth, 20)
                        .build()
        );

        lightLevelsLabel.active = false;

        lightLevelsKeyButton = this.addRenderableWidget(
                Button.builder(
                                getKeyText(LightLevelsClient.TOGGLE_KEY),
                                button -> startKeySelection(
                                        LightLevelsClient.TOGGLE_KEY,
                                        button
                                )
                        )
                        .bounds(rightX, startY, buttonWidth, 20)
                        .build()
        );

        this.addRenderableWidget(
                new RadiusSlider(
                        centerX - 70,
                        startY + 25,
                        140,
                        20,
                        LightLevelsConfig.getRadius()
                )
        );

        // ============================================================
        // ZOOM
        // ============================================================

        int zoomY = startY + 60;

        Button zoomLabel = this.addRenderableWidget(
                Button.builder(Component.literal("Zoom"), button -> {})
                        .bounds(leftX, zoomY, buttonWidth, 20)
                        .build()
        );

        zoomLabel.active = false;

        zoomKeyButton = this.addRenderableWidget(
                Button.builder(
                                getKeyText(ZoomClient.ZOOM_KEY),
                                button -> startKeySelection(
                                        ZoomClient.ZOOM_KEY,
                                        button
                                )
                        )
                        .bounds(rightX, zoomY, buttonWidth, 20)
                        .build()
        );

        this.addRenderableWidget(
                new ZoomSlider(
                        centerX - 70,
                        zoomY + 25,
                        140,
                        20,
                        ZoomConfig.getZoom()
                )
        );

        // ============================================================
        // GAMMA
        // ============================================================

        int gammaY = startY + 120;

        Button gammaLabel = this.addRenderableWidget(
                Button.builder(Component.literal("Gamma"), button -> {})
                        .bounds(leftX, gammaY, buttonWidth, 20)
                        .build()
        );

        gammaLabel.active = false;

        gammaKeyButton = this.addRenderableWidget(
                Button.builder(
                                getKeyText(GammaClient.TOGGLE_KEY),
                                button -> startKeySelection(
                                        GammaClient.TOGGLE_KEY,
                                        button
                                )
                        )
                        .bounds(rightX, gammaY, buttonWidth, 20)
                        .build()
        );

        // ============================================================
        // DONE
        // ============================================================

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("Done"),
                                button -> onClose()
                        )
                        .bounds(centerX - 100, startY + 165, 200, 20)
                        .build()
        );
    }

    // ============================================================
    // KEYBIND SELECTION
    // ============================================================

    private void startKeySelection(KeyMapping keyMapping, Button button) {

        waitingForKey = keyMapping;

        button.setMessage(Component.literal("Press a key..."));
    }

    private Component getKeyText(KeyMapping keyMapping) {

        if (keyMapping == null) {
            return Component.literal("Key: NONE");
        }

        return Component.literal(
                "Key: " + keyMapping.getTranslatedKeyMessage().getString()
        );
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {

        if (waitingForKey != null) {

            if (event.isEscape()) {

                waitingForKey = null;

                lightLevelsKeyButton.setMessage(
                        getKeyText(LightLevelsClient.TOGGLE_KEY)
                );

                zoomKeyButton.setMessage(
                        getKeyText(ZoomClient.ZOOM_KEY)
                );

                gammaKeyButton.setMessage(
                        getKeyText(GammaClient.TOGGLE_KEY)
                );

                return true;
            }

            InputConstants.Key key = InputConstants.getKey(event);

            if (waitingForKey == LightLevelsClient.TOGGLE_KEY) {

                AetherUtilitiesKeybindConfig.setLightLevelsKey(key);

                lightLevelsKeyButton.setMessage(
                        getKeyText(LightLevelsClient.TOGGLE_KEY)
                );

            } else if (waitingForKey == ZoomClient.ZOOM_KEY) {

                AetherUtilitiesKeybindConfig.setZoomKey(key);

                zoomKeyButton.setMessage(
                        getKeyText(ZoomClient.ZOOM_KEY)
                );

            } else if (waitingForKey == GammaClient.TOGGLE_KEY) {

                AetherUtilitiesKeybindConfig.setGammaKey(key);

                gammaKeyButton.setMessage(
                        getKeyText(GammaClient.TOGGLE_KEY)
                );
            }

            AetherUtilitiesKeybindConfig.save();

            waitingForKey = null;

            return true;
        }

        return super.keyPressed(event);
    }

    // ============================================================
    // RENDER
    // ============================================================

    @Override
    public void extractRenderState(
            @NonNull GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(
                this.font,
                this.title,
                this.width / 2,
                25,
                0xFFFFFFFF
        );
    }

    @Override
    public void onClose() {

        this.minecraft.gui.setScreen(parent);
    }

    // ============================================================
    // LIGHT LEVELS RADIUS SLIDER
    // ============================================================

    private static final class RadiusSlider extends AbstractSliderButton {

        private static final int MIN_RADIUS =
                LightLevelsRenderer.MIN_RADIUS;

        private static final int MAX_RADIUS =
                LightLevelsRenderer.MAX_RADIUS;

        private RadiusSlider(
                int x,
                int y,
                int width,
                int height,
                int radius
        ) {

            super(
                    x,
                    y,
                    width,
                    height,
                    Component.empty(),
                    (double) (radius - MIN_RADIUS)
                            / (MAX_RADIUS - MIN_RADIUS)
            );

            updateMessage();
        }

        private int valueToRadius() {

            return MIN_RADIUS
                    + (int) Math.round(
                    this.value * (MAX_RADIUS - MIN_RADIUS)
            );
        }

        @Override
        protected void updateMessage() {

            setMessage(
                    Component.literal("Radius: " + valueToRadius())
            );
        }

        @Override
        protected void applyValue() {

            LightLevelsConfig.setRadius(valueToRadius());

            LightLevelsConfig.save();

            updateMessage();
        }
    }

    // ============================================================
    // ZOOM SLIDER
    // ============================================================

    private static final class ZoomSlider extends AbstractSliderButton {

        private static final double MIN_ZOOM = 2.0;
        private static final double MAX_ZOOM = 6.0;

        private ZoomSlider(
                int x,
                int y,
                int width,
                int height,
                double zoom
        ) {

            super(
                    x,
                    y,
                    width,
                    height,
                    Component.empty(),
                    (zoom - MIN_ZOOM) / (MAX_ZOOM - MIN_ZOOM)
            );

            updateMessage();
        }

        private double valueToZoom() {

            return MIN_ZOOM
                    + this.value * (MAX_ZOOM - MIN_ZOOM);
        }

        @Override
        protected void updateMessage() {

            setMessage(
                    Component.literal(
                            String.format(
                                    "Zoom: %.1fx",
                                    valueToZoom()
                            )
                    )
            );
        }

        @Override
        protected void applyValue() {

            ZoomConfig.setZoom(valueToZoom());

            ZoomConfig.save();

            updateMessage();
        }
    }
}