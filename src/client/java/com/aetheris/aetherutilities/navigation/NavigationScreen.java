
package com.aetheris.aetherutilities.navigation;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class NavigationScreen extends Screen {

    private final Screen parent;

    private Button floatingButton;
    private Button locatorButton;

    private int pageOffset = 0;

    private NavigationScreen(Screen parent, int pageOffset) {
        super(Component.literal("AetherNavigation"));
        this.parent = parent;
        this.pageOffset = pageOffset;
    }

    public NavigationScreen(Screen parent) {
        this(parent, 0);
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        int buttonWidth = 220;
        int buttonHeight = 20;

        floatingButton = this.addRenderableWidget(
                Button.builder(
                        Component.empty(),
                        button -> {
                            NavigationConfig.setFloatingEnabled(
                                    !NavigationConfig.isFloatingEnabled()
                            );
                            updateButtons();
                        }
                ).bounds(
                        centerX - buttonWidth / 2,
                        35,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        locatorButton = this.addRenderableWidget(
                Button.builder(
                        Component.empty(),
                        button -> {
                            NavigationConfig.setLocatorEnabled(
                                    !NavigationConfig.isLocatorEnabled()
                            );
                            updateButtons();
                        }
                ).bounds(
                        centerX - buttonWidth / 2,
                        60,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Add Waypoint"),
                        button -> this.minecraft.gui.setScreen(
                                new WaypointEditorScreen(this, null)
                        )
                ).bounds(
                        centerX - buttonWidth / 2,
                        90,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        createWaypointRows();

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Previous"),
                        button -> changePage(-1)
                ).bounds(
                        centerX - 105,
                        this.height - 55,
                        100,
                        20
                ).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Next"),
                        button -> changePage(1)
                ).bounds(
                        centerX + 5,
                        this.height - 55,
                        100,
                        20
                ).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> onClose()
                ).bounds(
                        centerX - 100,
                        this.height - 30,
                        200,
                        20
                ).build()
        );

        updateButtons();
    }

    private void createWaypointRows() {

        List<NavigationWaypoint> waypoints =
                NavigationConfig.getWaypoints();

        int centerX = this.width / 2;
        int rowHeight = 25;
        int startY = 125;

        int availableRows = Math.max(
                1,
                (this.height - 195) / rowHeight
        );

        int maxOffset = Math.max(
                0,
                waypoints.size() - availableRows
        );

        pageOffset = Math.max(
                0,
                Math.min(pageOffset, maxOffset)
        );

        int end = Math.min(
                waypoints.size(),
                pageOffset + availableRows
        );

        for (int i = pageOffset; i < end; i++) {

            NavigationWaypoint waypoint = waypoints.get(i);

            int rowY = startY + (i - pageOffset) * rowHeight;

            int labelX = centerX - 190;

            String label =
                    waypoint.getName()
                            + "  ["
                            + waypoint.getX()
                            + ", "
                            + waypoint.getY()
                            + ", "
                            + waypoint.getZ()
                            + "]";

            this.addRenderableWidget(
                    Button.builder(
                            Component.literal(label),
                            button -> openEditor(waypoint)
                    ).bounds(
                            labelX,
                            rowY,
                            215,
                            20
                    ).build()
            );

            this.addRenderableWidget(
                    Button.builder(
                            Component.literal(
                                    waypoint.isEnabled() ? "ON" : "OFF"
                            ),
                            button -> {
                                waypoint.setEnabled(!waypoint.isEnabled());
                                NavigationConfig.saveChanges();
                                refreshScreen();
                            }
                    ).bounds(
                            labelX + 220,
                            rowY,
                            45,
                            20
                    ).build()
            );

            this.addRenderableWidget(
                    Button.builder(
                            Component.literal("Edit"),
                            button -> openEditor(waypoint)
                    ).bounds(
                            labelX + 270,
                            rowY,
                            45,
                            20
                    ).build()
            );

            this.addRenderableWidget(
                    Button.builder(
                            Component.literal("Delete"),
                            button -> {
                                NavigationConfig.removeWaypoint(
                                        waypoint.getId()
                                );
                                refreshScreen();
                            }
                    ).bounds(
                            labelX + 320,
                            rowY,
                            60,
                            20
                    ).build()
            );
        }
    }

    private void openEditor(NavigationWaypoint waypoint) {

        this.minecraft.gui.setScreen(
                new WaypointEditorScreen(this, waypoint)
        );
    }

    private void changePage(int direction) {

        int availableRows = Math.max(
                1,
                (this.height - 195) / 25
        );

        pageOffset += direction * availableRows;

        refreshScreen();
    }

    private void refreshScreen() {

        this.minecraft.gui.setScreen(
                new NavigationScreen(parent, pageOffset)
        );
    }

    private void updateButtons() {

        if (floatingButton != null) {
            floatingButton.setMessage(
                    Component.literal(
                            "Floating Waypoints: "
                                    + (NavigationConfig.isFloatingEnabled()
                                    ? "ON"
                                    : "OFF")
                    )
            );
        }

        if (locatorButton != null) {
            locatorButton.setMessage(
                    Component.literal(
                            "Locator Bar: "
                                    + (NavigationConfig.isLocatorEnabled()
                                    ? "ON"
                                    : "OFF")
                    )
            );
        }
    }

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
                this.width / 2 - 205,
                15,
                this.width / 2 + 205,
                this.height - 15,
                0xA0101010
        );
    }

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

        graphics.centeredText(
                this.font,
                this.title,
                this.width / 2,
                18,
                0xFFFFFFFF
        );

        graphics.centeredText(
                this.font,
                Component.literal("WAYPOINTS"),
                this.width / 2,
                115,
                0xFFAAAAAA
        );

        if (NavigationConfig.getWaypoints().isEmpty()) {
            graphics.centeredText(
                    this.font,
                    Component.literal("No waypoints created yet"),
                    this.width / 2,
                    145,
                    0xFFAAAAAA
            );
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}
