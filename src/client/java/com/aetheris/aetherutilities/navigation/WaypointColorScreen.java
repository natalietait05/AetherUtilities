
package com.aetheris.aetherutilities.navigation;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public final class WaypointColorScreen extends Screen {

    private static final int[] PRESET_COLORS = {
            0xFFFFFF, // White
            0xFF5555, // Red
            0xFFAA00, // Orange
            0xFFFF55, // Yellow
            0x55FF55, // Green
            0x55FFFF, // Cyan
            0x5555FF, // Blue
            0xAA55FF  // Purple
    };

    private static final String[] PRESET_NAMES = {
            "White",
            "Red",
            "Orange",
            "Yellow",
            "Green",
            "Cyan",
            "Blue",
            "Purple"
    };

    private final Screen parent;
    private final Consumer<Integer> colorCallback;

    private int selectedColor;

    private EditBox hexField;

    private String errorMessage = "";

    public WaypointColorScreen(
            Screen parent,
            int currentColor,
            Consumer<Integer> colorCallback
    ) {
        super(Component.literal("Waypoint Color"));

        this.parent = parent;
        this.selectedColor = currentColor & 0xFFFFFF;
        this.colorCallback = colorCallback;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        // -------------------------------------------------
        // HEXADECIMAL INPUT
        // -------------------------------------------------

        hexField = new EditBox(
                this.font,
                centerX - 35,
                82,
                90,
                20,
                Component.literal("Hexadecimal")
        );

        hexField.setMaxLength(6);

        hexField.setResponder(value -> {

            String filtered = value
                    .replaceAll("[^0-9a-fA-F]", "");

            if (!filtered.equals(value)) {
                hexField.setValue(filtered);
            }

            errorMessage = "";
        });

        hexField.setValue(
                String.format("%06X", selectedColor)
        );

        this.addRenderableWidget(hexField);

        // -------------------------------------------------
        // PRESET COLORS
        // -------------------------------------------------

        int buttonWidth = 70;
        int buttonHeight = 22;
        int gap = 6;

        int columns = 4;

        int totalWidth =
                columns * buttonWidth
                        + (columns - 1) * gap;

        int firstX = centerX - totalWidth / 2;
        int firstY = 132;

        for (int i = 0; i < PRESET_COLORS.length; i++) {

            final int index = i;

            int column = i % columns;
            int row = i / columns;

            int x = firstX + column * (buttonWidth + gap);
            int y = firstY + row * (buttonHeight + gap);

            this.addRenderableWidget(
                    Button.builder(
                                    Component.literal(PRESET_NAMES[i]),
                                    button -> selectPreset(index)
                            )
                            .bounds(
                                    x,
                                    y,
                                    buttonWidth,
                                    buttonHeight
                            )
                            .build()
            );
        }

        // -------------------------------------------------
        // DONE
        // -------------------------------------------------

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("Done"),
                                button -> confirmColor()
                        )
                        .bounds(
                                centerX - 95,
                                this.height - 40,
                                90,
                                20
                        )
                        .build()
        );

        // -------------------------------------------------
        // CANCEL
        // -------------------------------------------------

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("Cancel"),
                                button -> onClose()
                        )
                        .bounds(
                                centerX + 5,
                                this.height - 40,
                                90,
                                20
                        )
                        .build()
        );
    }

    // -----------------------------------------------------
    // SELECT PRESET
    // -----------------------------------------------------

    private void selectPreset(int index) {

        selectedColor = PRESET_COLORS[index];

        hexField.setValue(
                String.format("%06X", selectedColor)
        );

        errorMessage = "";
    }

    // -----------------------------------------------------
    // CONFIRM COLOR
    // -----------------------------------------------------

    private void confirmColor() {

        String hex = hexField.getValue().trim();

        if (!hex.matches("[0-9a-fA-F]{6}")) {

            errorMessage = "Enter exactly 6 hexadecimal digits";

            return;
        }

        try {

            selectedColor = Integer.parseInt(hex, 16);

            colorCallback.accept(selectedColor);

            this.minecraft.gui.setScreen(parent);

        } catch (NumberFormatException exception) {

            errorMessage = "Invalid hexadecimal color";
        }
    }

    // -----------------------------------------------------
    // BACKGROUND
    // -----------------------------------------------------

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

        graphics.fill(
                this.width / 2 - 175,
                20,
                this.width / 2 + 175,
                this.height - 20,
                0xA0101010
        );
    }

    // -----------------------------------------------------
    // RENDER
    // -----------------------------------------------------

    @Override
    public void extractRenderState(
            @NonNull GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        int centerX = this.width / 2;

        // TITLE

        graphics.centeredText(
                this.font,
                Component.literal("WAYPOINT COLOR"),
                centerX,
                35,
                0xFFFFFFFF
        );

        // HEX LABEL

        graphics.text(
                this.font,
                Component.literal("Hexadecimal:"),
                centerX - 105,
                88,
                0xFFAAAAAA,
                false
        );

        // FIXED #

        graphics.text(
                this.font,
                Component.literal("#"),
                centerX - 47,
                88,
                0xFFFFFFFF,
                false
        );

        // PRESET LABEL

        graphics.centeredText(
                this.font,
                Component.literal("BASIC COLORS"),
                centerX,
                116,
                0xFFAAAAAA
        );

        // COLOR PREVIEW

        graphics.centeredText(
                this.font,
                Component.literal("Selected color:"),
                centerX,
                195,
                0xFFAAAAAA
        );

        graphics.fill(
                centerX - 7,
                210,
                centerX + 7,
                224,
                0xFF000000 | selectedColor
        );

        graphics.centeredText(
                this.font,
                Component.literal(
                        String.format("#%06X", selectedColor)
                ),
                centerX,
                230,
                0xFFFFFFFF
        );

        // ERROR

        if (!errorMessage.isEmpty()) {

            graphics.centeredText(
                    this.font,
                    Component.literal(errorMessage),
                    centerX,
                    this.height - 65,
                    0xFFFF5555
            );
        }
    }

    // -----------------------------------------------------
    // CLOSE
    // -----------------------------------------------------

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}
