
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
    private Button compassButton;

    private int pageOffset = 0;

    private static final int LABEL_WIDTH = 190;
    private static final int TOGGLE_WIDTH = 45;
    private static final int DELETE_WIDTH = 22;
    private static final int ROW_GAP = 5;
    private static final int ROW_HEIGHT = 25;

    private NavigationScreen(Screen parent, int pageOffset) {
        super(Component.literal("Waypoints"));
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

        compassButton = this.addRenderableWidget(
                Button.builder(
                        Component.empty(),
                        button -> {
                            NavigationConfig.setCompassEnabled(
                                    !NavigationConfig.isCompassEnabled()
                            );
                            updateButtons();
                        }
                ).bounds(
                        centerX - buttonWidth / 2,
                        85,
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
                        115,
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

        int startY = 150;

        int availableRows = Math.max(
                1,
                (this.height - 220) / ROW_HEIGHT
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

        int rowWidth =
                LABEL_WIDTH
                        + ROW_GAP
                        + TOGGLE_WIDTH
                        + ROW_GAP
                        + DELETE_WIDTH;

        int rowX = centerX - rowWidth / 2;

        for (int i = pageOffset; i < end; i++) {

            NavigationWaypoint waypoint = waypoints.get(i);

            int rowY = startY + (i - pageOffset) * ROW_HEIGHT;

            String coordinates =
                    " ["
                            + waypoint.getX()
                            + ", "
                            + waypoint.getY()
                            + ", "
                            + waypoint.getZ()
                            + "]";

            String displayName = createShortenedLabel(
                    waypoint.getName(),
                    coordinates,
                    LABEL_WIDTH - 12
            );

            String label = displayName + coordinates;

            this.addRenderableWidget(
                    Button.builder(
                            Component.literal(label),
                            button -> openEditor(waypoint)
                    ).bounds(
                            rowX,
                            rowY,
                            LABEL_WIDTH,
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
                            rowX + LABEL_WIDTH + ROW_GAP,
                            rowY,
                            TOGGLE_WIDTH,
                            20
                    ).build()
            );

            this.addRenderableWidget(
                    Button.builder(
                            Component.literal("X"),
                            button -> {
                                NavigationConfig.removeWaypoint(
                                        waypoint.getId()
                                );
                                refreshScreen();
                            }
                    ).bounds(
                            rowX + LABEL_WIDTH + ROW_GAP
                                    + TOGGLE_WIDTH + ROW_GAP,
                            rowY,
                            DELETE_WIDTH,
                            20
                    ).build()
            );
        }
    }

    private String createShortenedLabel(
            String name,
            String coordinates,
            int maxWidth
    ) {

        int availableNameWidth =
                maxWidth - this.font.width(coordinates);

        if (availableNameWidth <= 0) {
            return "";
        }

        if (this.font.width(name) <= availableNameWidth) {
            return name;
        }

        String ellipsis = "...";

        int ellipsisWidth = this.font.width(ellipsis);

        if (availableNameWidth <= ellipsisWidth) {
            return "";
        }

        String shortened = name;

        while (!shortened.isEmpty()
                && this.font.width(shortened + ellipsis)
                > availableNameWidth) {

            shortened = shortened.substring(
                    0,
                    shortened.length() - 1
            );
        }

        return shortened + ellipsis;
    }

    private void openEditor(NavigationWaypoint waypoint) {

        this.minecraft.gui.setScreen(
                new WaypointEditorScreen(this, waypoint)
        );
    }

    private void changePage(int direction) {

        int availableRows = Math.max(
                1,
                (this.height - 220) / ROW_HEIGHT
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

        if (compassButton != null) {
            compassButton.setMessage(
                    Component.literal(
                            "Compass: "
                                    + (NavigationConfig.isCompassEnabled()
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
                140,
                0xFFAAAAAA
        );

        if (NavigationConfig.getWaypoints().isEmpty()) {
            graphics.centeredText(
                    this.font,
                    Component.literal("No waypoints created yet"),
                    this.width / 2,
                    170,
                    0xFFAAAAAA
            );
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}
