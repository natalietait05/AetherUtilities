
package com.aetheris.aetherutilities.navigation;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public final class NavigationConfig {

    public enum CompassMode {
        ON_TOP,
        ON_LOCATOR_BAR,
        OFF
    }

    private static final String FLOATING_KEY = "floating_enabled";
    private static final String LOCATOR_KEY = "locator_enabled";
    private static final String COMPASS_MODE_KEY = "compass_mode";
    private static final String COMPASS_DIRECTIONS_KEY = "compass_eight_directions";
    private static final String COUNT_KEY = "waypoint_count";

    private static final List<NavigationWaypoint> WAYPOINTS =
            new ArrayList<>();

    private static boolean floatingEnabled = true;
    private static boolean locatorEnabled = true;

    private static CompassMode compassMode = CompassMode.ON_TOP;
    private static boolean compassEightDirections = true;

    private NavigationConfig() {
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public static boolean isFloatingEnabled() {
        return floatingEnabled;
    }

    public static boolean isLocatorEnabled() {
        return locatorEnabled;
    }

    public static CompassMode getCompassMode() {
        return compassMode;
    }

    public static boolean isCompassEnabled() {
        return compassMode != CompassMode.OFF;
    }

    public static boolean isCompassEightDirections() {
        return compassEightDirections;
    }

    public static List<NavigationWaypoint> getWaypoints() {
        return Collections.unmodifiableList(WAYPOINTS);
    }

    // ============================================================
    // TOGGLES
    // ============================================================

    public static void setFloatingEnabled(boolean enabled) {
        floatingEnabled = enabled;
        save();
    }

    public static void setLocatorEnabled(boolean enabled) {
        locatorEnabled = enabled;
        save();
    }

    public static void setCompassMode(CompassMode mode) {
        compassMode = mode != null
                ? mode
                : CompassMode.OFF;

        save();
    }

    public static void setCompassEightDirections(boolean enabled) {
        compassEightDirections = enabled;
        save();
    }

    // Compatibility with previous boolean-based code.
    public static void setCompassEnabled(boolean enabled) {
        setCompassMode(
                enabled
                        ? CompassMode.ON_TOP
                        : CompassMode.OFF
        );
    }

    // ============================================================
    // WAYPOINT MANAGEMENT
    // ============================================================

    public static void addWaypoint(NavigationWaypoint waypoint) {

        if (waypoint == null) {
            return;
        }

        WAYPOINTS.add(waypoint);
        save();
    }

    public static void removeWaypoint(String id) {

        WAYPOINTS.removeIf(
                waypoint -> waypoint.getId().equals(id)
        );

        save();
    }

    public static NavigationWaypoint getWaypoint(String id) {

        return WAYPOINTS.stream()
                .filter(waypoint -> waypoint.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public static void saveChanges() {
        save();
    }

    public static void clearWaypoints() {
        WAYPOINTS.clear();
        save();
    }

    // ============================================================
    // LOAD
    // ============================================================

    public static void load() {

        WAYPOINTS.clear();

        Path path = getConfigPath();

        if (!Files.exists(path)) {
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(path)) {

            properties.load(input);

            floatingEnabled = Boolean.parseBoolean(
                    properties.getProperty(FLOATING_KEY, "true")
            );

            locatorEnabled = Boolean.parseBoolean(
                    properties.getProperty(LOCATOR_KEY, "true")
            );

            // New three-state compass configuration.
            String savedCompassMode =
                    properties.getProperty(COMPASS_MODE_KEY);

            if (savedCompassMode != null) {

                try {
                    compassMode = CompassMode.valueOf(
                            savedCompassMode
                    );
                } catch (IllegalArgumentException exception) {
                    compassMode = CompassMode.ON_TOP;
                }

            } else {

                // Migrate old configuration.
                boolean oldCompassEnabled = Boolean.parseBoolean(
                        properties.getProperty("compass_enabled", "true")
                );

                compassMode = oldCompassEnabled
                        ? CompassMode.ON_TOP
                        : CompassMode.OFF;
            }

            compassEightDirections = Boolean.parseBoolean(
                    properties.getProperty(
                            COMPASS_DIRECTIONS_KEY,
                            "true"
                    )
            );

            int count = Integer.parseInt(
                    properties.getProperty(COUNT_KEY, "0")
            );

            for (int i = 0; i < count; i++) {

                String prefix = "waypoint." + i + ".";

                String id = properties.getProperty(prefix + "id");
                String name = properties.getProperty(prefix + "name");

                String dimension = properties.getProperty(
                        prefix + "dimension",
                        "minecraft:overworld"
                );

                if (id == null || name == null) {
                    continue;
                }

                int x = Integer.parseInt(
                        properties.getProperty(prefix + "x", "0")
                );

                int y = Integer.parseInt(
                        properties.getProperty(prefix + "y", "0")
                );

                int z = Integer.parseInt(
                        properties.getProperty(prefix + "z", "0")
                );

                int color = Integer.parseInt(
                        properties.getProperty(prefix + "color", "16777215")
                );

                boolean enabled = Boolean.parseBoolean(
                        properties.getProperty(prefix + "enabled", "true")
                );

                WAYPOINTS.add(
                        new NavigationWaypoint(
                                id,
                                name,
                                x,
                                y,
                                z,
                                dimension,
                                color,
                                enabled
                        )
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Could not load AetherUtilities navigation configuration"
            );

            e.printStackTrace();
        }
    }

    // ============================================================
    // SAVE
    // ============================================================

    public static void save() {

        Path path = getConfigPath();

        Properties properties = new Properties();

        properties.setProperty(
                FLOATING_KEY,
                Boolean.toString(floatingEnabled)
        );

        properties.setProperty(
                LOCATOR_KEY,
                Boolean.toString(locatorEnabled)
        );

        properties.setProperty(
                COMPASS_MODE_KEY,
                compassMode.name()
        );

        properties.setProperty(
                COMPASS_DIRECTIONS_KEY,
                Boolean.toString(compassEightDirections)
        );

        properties.setProperty(
                COUNT_KEY,
                Integer.toString(WAYPOINTS.size())
        );

        for (int i = 0; i < WAYPOINTS.size(); i++) {

            NavigationWaypoint waypoint = WAYPOINTS.get(i);

            String prefix = "waypoint." + i + ".";

            properties.setProperty(prefix + "id", waypoint.getId());
            properties.setProperty(prefix + "name", waypoint.getName());
            properties.setProperty(prefix + "x", Integer.toString(waypoint.getX()));
            properties.setProperty(prefix + "y", Integer.toString(waypoint.getY()));
            properties.setProperty(prefix + "z", Integer.toString(waypoint.getZ()));
            properties.setProperty(prefix + "dimension", waypoint.getDimension());
            properties.setProperty(prefix + "color", Integer.toString(waypoint.getColor()));
            properties.setProperty(prefix + "enabled", Boolean.toString(waypoint.isEnabled()));
        }

        try {

            Files.createDirectories(path.getParent());

            try (OutputStream output = Files.newOutputStream(path)) {

                properties.store(
                        output,
                        "AetherUtilities Navigation Configuration"
                );
            }

        } catch (IOException e) {

            System.err.println(
                    "Could not save AetherUtilities navigation configuration"
            );

            e.printStackTrace();
        }
    }

    // ============================================================
    // PATH
    // ============================================================

    private static Path getConfigPath() {

        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve("aetherutilities-navigation.properties");
    }
}
