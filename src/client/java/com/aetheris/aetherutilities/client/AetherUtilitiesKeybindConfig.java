package com.aetheris.aetherutilities.client;

import com.aetheris.aetherutilities.lightlevels.LightLevelsClient;
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

    private AetherUtilitiesKeybindConfig() {
    }

    public static void load() {
        Path path = getConfigPath();
        if (!Files.exists(path)) return;

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            loadKey(properties, LIGHT_LEVELS_KEY, LightLevelsClient.TOGGLE_KEY);
            loadKey(properties, ZOOM_KEY, ZoomClient.ZOOM_KEY);
        } catch (IOException e) {
            System.err.println("Could not load AetherUtilities keybind configuration");
            e.printStackTrace();
        }
    }

    private static void loadKey(Properties properties, String property, KeyMapping keyMapping) {
        if (keyMapping == null) return;
        String value = properties.getProperty(property);
        if (value == null || value.isBlank()) return;
        try {
            keyMapping.setKey(InputConstants.getKey(value));
        } catch (Exception e) {
            System.err.println("Could not load keybind: " + property);
        }
    }

    public static void setLightLevelsKey(InputConstants.Key key) {
        if (LightLevelsClient.TOGGLE_KEY != null && key != null) {
            LightLevelsClient.TOGGLE_KEY.setKey(key);
        }
    }

    public static void setZoomKey(InputConstants.Key key) {
        if (ZoomClient.ZOOM_KEY != null && key != null) {
            ZoomClient.ZOOM_KEY.setKey(key);
        }
    }

    public static void save() {
        Path path = getConfigPath();
        Properties properties = new Properties();
        saveKey(properties, LIGHT_LEVELS_KEY, LightLevelsClient.TOGGLE_KEY);
        saveKey(properties, ZOOM_KEY, ZoomClient.ZOOM_KEY);

        try {
            Files.createDirectories(path.getParent());
            try (OutputStream output = Files.newOutputStream(path)) {
                properties.store(output, "AetherUtilities keybind configuration");
            }
        } catch (IOException e) {
            System.err.println("Could not save AetherUtilities keybind configuration");
            e.printStackTrace();
        }
    }

    private static void saveKey(Properties properties, String property, KeyMapping keyMapping) {
        if (keyMapping != null) {
            properties.setProperty(property, keyMapping.saveString());
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("aetherutilities-keybinds.properties");
    }
}
