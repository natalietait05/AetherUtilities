
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
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public final class AetherUtilitiesConfigScreen extends Screen {

    private final Screen parent;

    // ============================================================
    // LAYOUT
    // ============================================================

    private static final int PANEL_WIDTH = 430;
    private static final int CONTROL_WIDTH = 150;
    private static final int CONTROL_HEIGHT = 20;

    private static final int SECTION_STEP = 88;

    private static final int[] SECTION_OFFSETS = {
            0, 88, 176, 264
    };

    private int panelX;
    private int panelWidth;
    private int controlX;

    private int viewportTop;
    private int viewportBottom;
    private int viewportHeight;

    private int scrollOffset;
    private static final int CONTENT_HEIGHT = 354;

    // ============================================================
    // CONTROLS
    // ============================================================

    private Button lightLevelsKeyButton;
    private RadiusSlider radiusSlider;

    private Button zoomKeyButton;
    private ZoomSlider zoomSlider;

    private Button gammaModeButton;
    private Button gammaKeyButton;

    private Button autoRefillButton;
    private Button autoToolButton;

    private Button doneButton;

    // ============================================================
    // KEYBIND SELECTION
    // ============================================================

    private KeyMapping waitingForKey;
    private Button waitingForKeyButton;

    public AetherUtilitiesConfigScreen(Screen parent) {

        super(Component.literal("AetherUtilities"));

        this.parent = parent;
    }

    // ============================================================
    // INIT
    // ============================================================

    @Override
    protected void init() {

        this.clearWidgets();

        panelWidth = Math.min(PANEL_WIDTH, this.width - 32);
        panelX = (this.width - panelWidth) / 2;

        controlX = panelX + panelWidth - CONTROL_WIDTH - 22;

        viewportTop = 65;
        viewportBottom = this.height - 48;
        viewportHeight = Math.max(80, viewportBottom - viewportTop);

        scrollOffset = Math.min(
                scrollOffset,
                getMaxScroll()
        );

        createControls();

        updateControlPositions();

        // Done permanece fuera del área desplazable.
        doneButton = this.addRenderableWidget(
                Button.builder(
                                Component.literal("Done"),
                                button -> onClose()
                        )
                        .bounds(
                                this.width / 2 - 100,
                                this.height - 32,
                                200,
                                20
                        )
                        .build()
        );
    }

    // ============================================================
    // CREATE CONTROLS
    // ============================================================

    private void createControls() {

        // LIGHT LEVELS

        lightLevelsKeyButton = this.addRenderableWidget(
                Button.builder(
                                getKeyText(LightLevelsClient.TOGGLE_KEY),
                                button -> startKeySelection(
                                        LightLevelsClient.TOGGLE_KEY,
                                        button
                                )
                        )
                        .bounds(
                                controlX,
                                viewportTop,
                                CONTROL_WIDTH,
                                CONTROL_HEIGHT
                        )
                        .build()
        );

        radiusSlider = this.addRenderableWidget(
                new RadiusSlider(
                        controlX,
                        viewportTop,
                        CONTROL_WIDTH,
                        CONTROL_HEIGHT,
                        LightLevelsConfig.getRadius()
                )
        );

        // ZOOM

        zoomKeyButton = this.addRenderableWidget(
                Button.builder(
                                getKeyText(ZoomClient.ZOOM_KEY),
                                button -> startKeySelection(
                                        ZoomClient.ZOOM_KEY,
                                        button
                                )
                        )
                        .bounds(
                                controlX,
                                viewportTop,
                                CONTROL_WIDTH,
                                CONTROL_HEIGHT
                        )
                        .build()
        );

        zoomSlider = this.addRenderableWidget(
                new ZoomSlider(
                        controlX,
                        viewportTop,
                        CONTROL_WIDTH,
                        CONTROL_HEIGHT,
                        ZoomConfig.getZoom()
                )
        );

        // GAMMA

        gammaModeButton = this.addRenderableWidget(
                Button.builder(
                                Component.empty(),
                                button -> {
                                    GammaClient.nextMode();
                                    updateGammaButton();
                                }
                        )
                        .bounds(
                                controlX,
                                viewportTop,
                                CONTROL_WIDTH,
                                CONTROL_HEIGHT
                        )
                        .build()
        );

        gammaKeyButton = this.addRenderableWidget(
                Button.builder(
                                getKeyText(GammaClient.TOGGLE_KEY),
                                button -> startKeySelection(
                                        GammaClient.TOGGLE_KEY,
                                        button
                                )
                        )
                        .bounds(
                                controlX,
                                viewportTop,
                                CONTROL_WIDTH,
                                CONTROL_HEIGHT
                        )
                        .build()
        );

        // AUTOMATION

        autoRefillButton = this.addRenderableWidget(
                Button.builder(
                                Component.empty(),
                                button -> {
                                    AutoFeaturesConfig.setAutoRefillEnabled(
                                            !AutoFeaturesConfig.isAutoRefillEnabled()
                                    );

                                    updateAutoRefillButton();
                                }
                        )
                        .bounds(
                                controlX,
                                viewportTop,
                                CONTROL_WIDTH,
                                CONTROL_HEIGHT
                        )
                        .build()
        );

        autoToolButton = this.addRenderableWidget(
                Button.builder(
                                Component.empty(),
                                button -> {
                                    AutoFeaturesConfig.setAutoToolEnabled(
                                            !AutoFeaturesConfig.isAutoToolEnabled()
                                    );

                                    updateAutoToolButton();
                                }
                        )
                        .bounds(
                                controlX,
                                viewportTop,
                                CONTROL_WIDTH,
                                CONTROL_HEIGHT
                        )
                        .build()
        );

        updateGammaButton();
        updateAutoRefillButton();
        updateAutoToolButton();
    }

    // ============================================================
    // CONTROL POSITIONS
    // ============================================================

    private void updateControlPositions() {

        // LIGHT LEVELS

        positionWidget(
                lightLevelsKeyButton,
                24
        );

        positionWidget(
                radiusSlider,
                49
        );

        // ZOOM

        positionWidget(
                zoomKeyButton,
                SECTION_STEP + 24
        );

        positionWidget(
                zoomSlider,
                SECTION_STEP + 49
        );

        // GAMMA

        positionWidget(
                gammaModeButton,
                SECTION_STEP * 2 + 24
        );

        positionWidget(
                gammaKeyButton,
                SECTION_STEP * 2 + 49
        );

        // AUTOMATION

        positionWidget(
                autoRefillButton,
                SECTION_STEP * 3 + 24
        );

        positionWidget(
                autoToolButton,
                SECTION_STEP * 3 + 49
        );
    }

    private void positionWidget(
            AbstractWidget widget,
            int contentOffset
    ) {

        int y = viewportTop + contentOffset - scrollOffset;

        widget.setX(controlX);
        widget.setY(y);

        widget.visible = (
                y >= viewportTop
                        && y + widget.getHeight() <= viewportBottom
        );
    }

    private int getMaxScroll() {

        return Math.max(
                0,
                CONTENT_HEIGHT - viewportHeight
        );
    }

    // ============================================================
    // BUTTON TEXT
    // ============================================================

    private void updateGammaButton() {

        if (gammaModeButton == null) {
            return;
        }

        String modeText = switch (GammaClient.getMode()) {
            case OFF -> "OFF";
            case X3 -> "x3";
            case X15 -> "x15";
        };

        gammaModeButton.setMessage(
                Component.literal("Gamma: " + modeText)
        );
    }

    private void updateAutoRefillButton() {

        if (autoRefillButton == null) {
            return;
        }

        autoRefillButton.setMessage(
                Component.literal(
                        "Auto Refill: "
                                + (AutoFeaturesConfig.isAutoRefillEnabled()
                                ? "ON"
                                : "OFF")
                )
        );
    }

    private void updateAutoToolButton() {

        if (autoToolButton == null) {
            return;
        }

        autoToolButton.setMessage(
                Component.literal(
                        "Auto Tool: "
                                + (AutoFeaturesConfig.isAutoToolEnabled()
                                ? "ON"
                                : "OFF")
                )
        );
    }

    // ============================================================
    // KEYBIND SELECTION
    // ============================================================

    private void startKeySelection(
            KeyMapping keyMapping,
            Button button
    ) {

        waitingForKey = keyMapping;
        waitingForKeyButton = button;

        button.setMessage(
                Component.literal("Press a key...")
        );
    }

    private Component getKeyText(KeyMapping keyMapping) {

        if (keyMapping == null) {
            return Component.literal("Key: NONE");
        }

        return Component.literal(
                "Key: " + keyMapping.getTranslatedKeyMessage().getString()
        );
    }

    private void cancelKeySelection() {

        if (waitingForKeyButton != null && waitingForKey != null) {

            waitingForKeyButton.setMessage(
                    getKeyText(waitingForKey)
            );
        }

        waitingForKey = null;
        waitingForKeyButton = null;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {

        if (waitingForKey != null) {

            if (event.isEscape()) {

                cancelKeySelection();

                return true;
            }

            InputConstants.Key key = InputConstants.getKey(event);

            if (waitingForKey == LightLevelsClient.TOGGLE_KEY) {

                AetherUtilitiesKeybindConfig.setLightLevelsKey(key);

            } else if (waitingForKey == ZoomClient.ZOOM_KEY) {

                AetherUtilitiesKeybindConfig.setZoomKey(key);

            } else if (waitingForKey == GammaClient.TOGGLE_KEY) {

                AetherUtilitiesKeybindConfig.setGammaKey(key);
            }

            waitingForKeyButton.setMessage(
                    getKeyText(waitingForKey)
            );

            AetherUtilitiesKeybindConfig.save();

            waitingForKey = null;
            waitingForKeyButton = null;

            return true;
        }

        return super.keyPressed(event);
    }

    // ============================================================
    // SCROLLING
    // ============================================================

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {

        if (mouseX >= panelX
                && mouseX <= panelX + panelWidth
                && mouseY >= viewportTop
                && mouseY <= viewportBottom
                && getMaxScroll() > 0) {

            scrollOffset -= (int) Math.round(scrollY * 20);

            scrollOffset = Math.max(
                    0,
                    Math.min(scrollOffset, getMaxScroll())
            );

            updateControlPositions();

            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                scrollX,
                scrollY
        );
    }

    // ============================================================
    // BACKGROUND
    // ============================================================

    @Override
    public void extractBackground(
            @NonNull GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        super.extractBackground(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        // Un único panel oscuro y semitransparente.
        // El mundo sigue visible detrás de él.

        graphics.fill(
                panelX,
                viewportTop - 10,
                panelX + panelWidth,
                viewportBottom + 5,
                0xA0101010
        );
    }

    // ============================================================
    // SECTION TITLES AND DESCRIPTIONS
    // ============================================================

    private void drawSection(
            GuiGraphicsExtractor graphics,
            String title,
            int sectionIndex,
            String firstDescription,
            String secondDescription
    ) {

        int headerY = viewportTop
                + SECTION_OFFSETS[sectionIndex]
                - scrollOffset;

        // No dibujar títulos que estén fuera del viewport.
        if (headerY < viewportTop
                || headerY + 20 > viewportBottom) {
            return;
        }

        graphics.centeredText(
                this.font,
                Component.literal(title),
                panelX + panelWidth / 2,
                headerY,
                0xFFFFFFFF
        );

        // Línea divisoria bajo el título.
        graphics.fill(
                panelX + 18,
                headerY + 19,
                panelX + panelWidth - 18,
                headerY + 20,
                0x66777777
        );

        drawDescription(
                graphics,
                firstDescription,
                SECTION_OFFSETS[sectionIndex] + 24
        );

        drawDescription(
                graphics,
                secondDescription,
                SECTION_OFFSETS[sectionIndex] + 49
        );
    }

    private void drawDescription(
            GuiGraphicsExtractor graphics,
            String description,
            int contentOffset
    ) {

        int y = viewportTop + contentOffset - scrollOffset;

        if (y < viewportTop
                || y + CONTROL_HEIGHT > viewportBottom) {
            return;
        }

        graphics.text(
                this.font,
                Component.literal(description),
                panelX + 20,
                y + 6,
                0xFFE0E0E0,
                false
        );
    }

    // ============================================================
    // SCROLLBAR INDICATOR
    // ============================================================

    private void drawScrollbar(
            GuiGraphicsExtractor graphics
    ) {

        int maxScroll = getMaxScroll();

        if (maxScroll <= 0) {
            return;
        }

        int scrollbarX = panelX + panelWidth - 7;
        int trackHeight = viewportHeight;

        int thumbHeight = Math.max(
                24,
                (int) (
                        (double) viewportHeight
                                * viewportHeight
                                / CONTENT_HEIGHT
                )
        );

        int thumbTravel = trackHeight - thumbHeight;

        int thumbY = viewportTop;

        if (maxScroll > 0) {

            thumbY += (int) (
                    (double) scrollOffset
                            / maxScroll
                            * thumbTravel
            );
        }

        graphics.fill(
                scrollbarX,
                viewportTop,
                scrollbarX + 2,
                viewportBottom,
                0x55777777
        );

        graphics.fill(
                scrollbarX - 1,
                thumbY,
                scrollbarX + 3,
                thumbY + thumbHeight,
                0xFFAAAAAA
        );
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

        // Dibujar títulos y descripciones antes que los controles.
        drawSection(
                graphics,
                "LIGHT LEVELS",
                0,
                "Toggle light overlay",
                "Detection radius"
        );

        drawSection(
                graphics,
                "ZOOM",
                1,
                "Toggle zoom",
                "Zoom strength"
        );

        drawSection(
                graphics,
                "GAMMA",
                2,
                "Brightness mode",
                "Toggle gamma"
        );

        drawSection(
                graphics,
                "AUTOMATION",
                3,
                "Automatic inventory refill",
                "Automatic tool selection"
        );

        drawScrollbar(graphics);

        // Los controles se renderizan normalmente.
        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        graphics.centeredText(
                this.font,
                this.title,
                this.width / 2,
                25,
                0xFFFFFFFF
        );
    }

    // ============================================================
    // CLOSE
    // ============================================================

    @Override
    public void onClose() {

        cancelKeySelection();

        this.minecraft.gui.setScreen(parent);
    }

    // ============================================================
    // LIGHT LEVELS RADIUS SLIDER
    // ============================================================

    private static final class RadiusSlider
            extends AbstractSliderButton {

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
                    Component.literal(
                            "Radius: " + valueToRadius()
                    )
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

    private static final class ZoomSlider
            extends AbstractSliderButton {

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
