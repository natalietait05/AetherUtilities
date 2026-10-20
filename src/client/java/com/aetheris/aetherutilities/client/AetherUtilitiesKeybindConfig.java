package com.aetheris.aetherutilities.client;

import com.aetheris.aetherutilities.gamma.GammaClient;
import com.aetheris.aetherutilities.lightlevels.LightLevelsClient;
import com.aetheris.aetherutilities.navigation.NavigationClient;
import com.aetheris.aetherutilities.zoom.ZoomClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AetherUtilitiesKeybindConfig {

    private static final String LIGHT_LEVELS_KEY = "light_levels";
    private static final String ZOOM_KEY = "zoom";
    private static final String GAMMA_KEY = "gamma";
    private static final String NAVIGATION_KEY = "navigation";

    private AetherUtilitiesKeybindConfig() {
    }

    // ============================================================
    // LOAD
    // ============================================================

    public static void load() {

        Path path = getConfigPath();

        if (!Files.exists(path)) {
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(path)) {

            properties.load(input);

            loadKey(
                    properties,
                    LIGHT_LEVELS_KEY,
                    LightLevelsClient.TOGGLE_KEY
            );

            loadKey(
                    properties,
                    ZOOM_KEY,
                    ZoomClient.ZOOM_KEY
            );

            loadKey(
                    properties,
                    GAMMA_KEY,
                    GammaClient.TOGGLE_KEY
            );

            loadKey(
                    properties,
                    NAVIGATION_KEY,
                    NavigationClient.NAVIGATION_KEY
            );

        } catch (IOException e) {

            System.err.println(
                    "Could not load AetherUtilities keybind configuration"
            );

            e.printStackTrace();
        }
    }

    private static void loadKey(
            Properties properties,
            String property,
            KeyMapping keyMapping
    ) {

        if (keyMapping == null) {
            return;
        }

        String value = properties.getProperty(property);

        if (value == null || value.isBlank()) {
            return;
        }

        try {

            keyMapping.setKey(InputConstants.getKey(value));

            KeyMapping.resetMapping();

        } catch (Exception e) {

            System.err.println(
                    "Could not load keybind: " + property
            );
        }
    }

    // ============================================================
    // SET KEY
    // ============================================================

    public static void setLightLevelsKey(InputConstants.Key key) {

        if (LightLevelsClient.TOGGLE_KEY != null && key != null) {

            LightLevelsClient.TOGGLE_KEY.setKey(key);

            KeyMapping.resetMapping();
        }
    }

    public static void setZoomKey(InputConstants.Key key) {

        if (ZoomClient.ZOOM_KEY != null && key != null) {

            ZoomClient.ZOOM_KEY.setKey(key);

            KeyMapping.resetMapping();
        }
    }

    public static void setGammaKey(InputConstants.Key key) {

        if (GammaClient.TOGGLE_KEY != null && key != null) {

            GammaClient.TOGGLE_KEY.setKey(key);

            KeyMapping.resetMapping();
        }
    }

    public static void setNavigationKey(InputConstants.Key key) {

        if (NavigationClient.NAVIGATION_KEY != null && key != null) {

            NavigationClient.NAVIGATION_KEY.setKey(key);

            KeyMapping.resetMapping();
        }
    }

    // ============================================================
    // SAVE
    // ============================================================

    public static void save() {

        Path path = getConfigPath();

        Properties properties = new Properties();

        saveKey(
                properties,
                LIGHT_LEVELS_KEY,
                LightLevelsClient.TOGGLE_KEY
        );

        saveKey(
                properties,
                ZOOM_KEY,
                ZoomClient.ZOOM_KEY
        );

        saveKey(
                properties,
                GAMMA_KEY,
                GammaClient.TOGGLE_KEY
        );

        saveKey(
                properties,
                NAVIGATION_KEY,
                NavigationClient.NAVIGATION_KEY
        );

        try {

            Files.createDirectories(path.getParent());

            try (OutputStream output = Files.newOutputStream(path)) {

                properties.store(
                        output,
                        "AetherUtilities keybind configuration"
                );
            }

        } catch (IOException e) {

            System.err.println(
                    "Could not save AetherUtilities keybind configuration"
            );

            e.printStackTrace();
        }
    }

    private static void saveKey(
            Properties properties,
            String property,
            KeyMapping keyMapping
    ) {

        if (keyMapping != null) {

            properties.setProperty(
                    property,
                    keyMapping.saveString()
            );
        }
    }

    // ============================================================
    // PATH
    // ============================================================

    private static Path getConfigPath() {

        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve("aetherutilities-keybinds.properties");
    }
}