
package com.aetheris.aetherutilities.navigation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class NavigationLocatorSync {

    private static ClientWaypointManager currentManager;

    private static final Map<UUID, TrackedEntry> TRACKED =
            new HashMap<>();

    private NavigationLocatorSync() {
    }

    // -----------------------------------------------------
    // INITIALIZE
    // -----------------------------------------------------

    public static void initialize() {

        ClientTickEvents.END_CLIENT_TICK.register(
                NavigationLocatorSync::tick
        );
    }

    // -----------------------------------------------------
    // TICK
    // -----------------------------------------------------

    private static void tick(Minecraft client) {

        if (client.player == null || client.level == null) {
            clearTrackedWaypoints();
            return;
        }

        ClientWaypointManager manager =
                client.player.connection.getWaypointManager();

        if (manager != currentManager) {

            TRACKED.clear();

            currentManager = manager;
        }

        if (!NavigationConfig.isLocatorEnabled()) {

            removeAll(manager);

            return;
        }

        String currentDimension =
                client.level.dimension().toString();

        Set<UUID> desiredIds = new HashSet<>();

        for (NavigationWaypoint waypoint :
                NavigationConfig.getWaypoints()) {

            if (!waypoint.isEnabled()) {
                continue;
            }

            if (!waypoint.getDimension().equals(currentDimension)) {
                continue;
            }

            UUID uuid;

            try {

                uuid = UUID.fromString(waypoint.getId());

            } catch (IllegalArgumentException exception) {

                continue;
            }

            desiredIds.add(uuid);

            syncWaypoint(manager, waypoint, uuid);
        }

        // REMOVE WAYPOINTS THAT ARE NO LONGER ACTIVE

        TRACKED.entrySet().removeIf(entry -> {

            if (desiredIds.contains(entry.getKey())) {
                return false;
            }

            manager.untrackWaypoint(
                    entry.getValue().waypoint()
            );

            return true;
        });
    }

    // -----------------------------------------------------
    // SYNC SINGLE WAYPOINT
    // -----------------------------------------------------

    private static void syncWaypoint(
            ClientWaypointManager manager,
            NavigationWaypoint waypoint,
            UUID uuid
    ) {

        Signature signature = new Signature(
                waypoint.getX(),
                waypoint.getY(),
                waypoint.getZ(),
                waypoint.getColor() & 0xFFFFFF
        );

        TrackedEntry existing = TRACKED.get(uuid);

        if (existing != null
                && existing.signature().equals(signature)) {

            return;
        }

        // REMOVE OLD VERSION

        if (existing != null) {

            manager.untrackWaypoint(
                    existing.waypoint()
            );
        }

        // CREATE VANILLA WAYPOINT ICON

        Waypoint.Icon icon = new Waypoint.Icon();

        icon.color = Optional.of(
                0xFF000000 | (waypoint.getColor() & 0xFFFFFF)
        );

        // CREATE VANILLA TRACKED WAYPOINT

        TrackedWaypoint trackedWaypoint =
                TrackedWaypoint.setPosition(
                        uuid,
                        icon,
                        new BlockPos(
                                waypoint.getX(),
                                waypoint.getY(),
                                waypoint.getZ()
                        )
                );

        manager.trackWaypoint(trackedWaypoint);

        TRACKED.put(
                uuid,
                new TrackedEntry(
                        signature,
                        trackedWaypoint
                )
        );
    }

    // -----------------------------------------------------
    // REMOVE ALL
    // -----------------------------------------------------

    private static void removeAll(
            ClientWaypointManager manager
    ) {

        for (TrackedEntry entry : TRACKED.values()) {

            manager.untrackWaypoint(
                    entry.waypoint()
            );
        }

        TRACKED.clear();
    }

    // -----------------------------------------------------
    // CLEAR
    // -----------------------------------------------------

    private static void clearTrackedWaypoints() {

        if (currentManager != null) {
            removeAll(currentManager);
        }

        currentManager = null;
    }

    // -----------------------------------------------------
    // DATA
    // -----------------------------------------------------

    private record Signature(
            int x,
            int y,
            int z,
            int color
    ) {
    }

    private record TrackedEntry(
            Signature signature,
            TrackedWaypoint waypoint
    ) {
    }
}
