
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

    private static final String FLOATING_KEY = "floating_enabled";
    private static final String LOCATOR_KEY = "locator_enabled";
    private static final String COMPASS_KEY = "compass_enabled";
    private static final String COUNT_KEY = "waypoint_count";

    private static final List<NavigationWaypoint> WAYPOINTS =
            new ArrayList<>();

    private static boolean floatingEnabled = true;
    private static boolean locatorEnabled = true;
    private static boolean compassEnabled = true;

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

    public static boolean isCompassEnabled() {
        return compassEnabled;
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

    public static void setCompassEnabled(boolean enabled) {
        compassEnabled = enabled;
        save();
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

            compassEnabled = Boolean.parseBoolean(
                    properties.getProperty(COMPASS_KEY, "true")
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
                COMPASS_KEY,
                Boolean.toString(compassEnabled)
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
