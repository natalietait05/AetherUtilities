
package com.aetheris.aetherutilities.navigation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public final class WaypointEditorScreen extends Screen {

    private final Screen parent;
    private final NavigationWaypoint editingWaypoint;

    private int waypointColor = 0xFFFFFF;

    private EditBox nameField;
    private EditBox xField;
    private EditBox yField;
    private EditBox zField;

    private String dimensionId = "minecraft:overworld";
    private String errorMessage = "";

    public WaypointEditorScreen(
            Screen parent,
            NavigationWaypoint waypoint
    ) {
        super(Component.literal(
                waypoint == null ? "Add Waypoint" : "Edit Waypoint"
        ));

        this.parent = parent;
        this.editingWaypoint = waypoint;

        // Load the existing color only once.
        // Do not reset it when returning from the color screen.
        if (waypoint != null) {
            this.waypointColor = waypoint.getColor();
            this.dimensionId = waypoint.getDimension();
        }
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        int startY = 70;

        // -------------------------------------------------
        // NAME
        // -------------------------------------------------

        nameField = createField(
                centerX - 110,
                startY,
                220,
                "Waypoint name"
        );

        // -------------------------------------------------
        // COORDINATES
        // -------------------------------------------------

        int coordinateWidth = 66;
        int coordinateGap = 10;

        int totalWidth =
                coordinateWidth * 3 + coordinateGap * 2;

        int coordinateStartX =
                centerX - totalWidth / 2;

        int coordinateY = 169;

        xField = createField(
                coordinateStartX,
                coordinateY,
                coordinateWidth,
                ""
        );

        yField = createField(
                coordinateStartX + coordinateWidth + coordinateGap,
                coordinateY,
                coordinateWidth,
                ""
        );

        zField = createField(
                coordinateStartX + (coordinateWidth + coordinateGap) * 2,
                coordinateY,
                coordinateWidth,
                ""
        );

        // -------------------------------------------------
        // EXISTING WAYPOINT
        // -------------------------------------------------

        if (editingWaypoint != null) {

            nameField.setValue(editingWaypoint.getName());

            xField.setValue(
                    Integer.toString(editingWaypoint.getX())
            );

            yField.setValue(
                    Integer.toString(editingWaypoint.getY())
            );

            zField.setValue(
                    Integer.toString(editingWaypoint.getZ())
            );

        } else {

            xField.setValue("0");
            yField.setValue("64");
            zField.setValue("0");

            Minecraft client = Minecraft.getInstance();

            if (client.level != null) {
                dimensionId = client.level.dimension().toString();
            }
        }

        // -------------------------------------------------
        // COLOR BUTTON
        // -------------------------------------------------

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("Color"),
                                button -> openColorScreen()
                        )
                        .bounds(
                                centerX - 55,
                                100,
                                110,
                                20
                        )
                        .build()
        );

        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("Save"),
                                button -> saveWaypoint()
                        )
                        .bounds(
                                centerX - 105,
                                this.height - 40,
                                100,
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
                                100,
                                20
                        )
                        .build()
        );
    }

    // -----------------------------------------------------
    // FIELD CREATION
    // -----------------------------------------------------

    private EditBox createField(
            int x,
            int y,
            int width,
            String label
    ) {

        EditBox field = new EditBox(
                this.font,
                x,
                y,
                width,
                20,
                Component.literal(label)
        );

        field.setMaxLength(128);

        return this.addRenderableWidget(field);
    }

    // -----------------------------------------------------
    // OPEN COLOR SCREEN
    // -----------------------------------------------------

    private void openColorScreen() {

        this.minecraft.gui.setScreen(
                new WaypointColorScreen(
                        this,
                        waypointColor,
                        color -> {
                            waypointColor = color;
                        }
                )
        );
    }

    // -----------------------------------------------------
    // DIMENSION DISPLAY NAME
    // -----------------------------------------------------

    private String getDimensionDisplayName() {

        return switch (dimensionId) {

            case "minecraft:overworld" ->
                    "Overworld";

            case "minecraft:the_nether" ->
                    "The Nether";

            case "minecraft:the_end" ->
                    "The End";

            default -> {

                String name = dimensionId;

                int separator = name.indexOf(':');

                if (separator >= 0) {
                    name = name.substring(separator + 1);
                }

                name = name.replace('/', ' ');
                name = name.replace('_', ' ');

                String[] words = name.split(" ");
                StringBuilder result = new StringBuilder();

                for (String word : words) {

                    if (word.isEmpty()) {
                        continue;
                    }

                    if (!result.isEmpty()) {
                        result.append(" ");
                    }

                    result.append(
                            Character.toUpperCase(word.charAt(0))
                    );

                    if (word.length() > 1) {
                        result.append(word.substring(1));
                    }
                }

                yield result.toString();
            }
        };
    }

    // -----------------------------------------------------
    // SAVE WAYPOINT
    // -----------------------------------------------------

    private void saveWaypoint() {

        String name = nameField.getValue().trim();

        if (name.isEmpty()) {
            errorMessage = "Waypoint name cannot be empty";
            return;
        }

        try {

            int x = Integer.parseInt(xField.getValue().trim());
            int y = Integer.parseInt(yField.getValue().trim());
            int z = Integer.parseInt(zField.getValue().trim());

            if (editingWaypoint == null) {

                NavigationWaypoint waypoint =
                        new NavigationWaypoint(
                                name,
                                x,
                                y,
                                z,
                                dimensionId,
                                waypointColor
                        );

                NavigationConfig.addWaypoint(waypoint);

            } else {

                editingWaypoint.setName(name);

                editingWaypoint.setPosition(
                        x,
                        y,
                        z
                );

                editingWaypoint.setColor(waypointColor);

                NavigationConfig.saveChanges();
            }

            this.minecraft.gui.setScreen(parent);

        } catch (NumberFormatException exception) {

            errorMessage = "Coordinates must be valid integers";
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
                this.width / 2 - 150,
                20,
                this.width / 2 + 150,
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
                this.title,
                centerX,
                35,
                0xFFFFFFFF
        );

        // NAME

        graphics.centeredText(
                this.font,
                Component.literal("NAME"),
                centerX,
                57,
                0xFFAAAAAA
        );

        // COLOR PREVIEW

        graphics.fill(
                centerX + 65,
                104,
                centerX + 77,
                116,
                0xFF000000 | waypointColor
        );

        graphics.text(
                this.font,
                Component.literal(
                        String.format("#%06X", waypointColor)
                ),
                centerX + 83,
                106,
                0xFFFFFFFF,
                false
        );

        // COORDINATES TITLE

        graphics.centeredText(
                this.font,
                Component.literal("COORDINATES"),
                centerX,
                140,
                0xFFAAAAAA
        );

        // X, Y, Z LABELS

        int coordinateWidth = 66;
        int coordinateGap = 10;

        int totalWidth =
                coordinateWidth * 3 + coordinateGap * 2;

        int startX = centerX - totalWidth / 2;

        int labelY = 155;

        graphics.centeredText(
                this.font,
                Component.literal("X"),
                startX + coordinateWidth / 2,
                labelY,
                0xFFFFFFFF
        );

        graphics.centeredText(
                this.font,
                Component.literal("Y"),
                startX + coordinateWidth + coordinateGap + coordinateWidth / 2,
                labelY,
                0xFFFFFFFF
        );

        graphics.centeredText(
                this.font,
                Component.literal("Z"),
                startX + (coordinateWidth + coordinateGap) * 2 + coordinateWidth / 2,
                labelY,
                0xFFFFFFFF
        );

        // DIMENSION

        graphics.centeredText(
                this.font,
                Component.literal("DIMENSION"),
                centerX,
                210,
                0xFFAAAAAA
        );

        graphics.centeredText(
                this.font,
                Component.literal(getDimensionDisplayName()),
                centerX,
                225,
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
