
package com.aetheris.aetherutilities.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AutoFeaturesConfig {

    private static final String AUTO_REFILL_KEY = "auto_refill";
    private static final String AUTO_TOOL_KEY = "auto_tool";

    private static boolean autoRefillEnabled = false;
    private static boolean autoToolEnabled = false;

    private AutoFeaturesConfig() {
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public static boolean isAutoRefillEnabled() {
        return autoRefillEnabled;
    }

    public static boolean isAutoToolEnabled() {
        return autoToolEnabled;
    }

    // ============================================================
    // SETTERS
    // ============================================================

    public static void setAutoRefillEnabled(boolean enabled) {
        autoRefillEnabled = enabled;
        save();
    }

    public static void setAutoToolEnabled(boolean enabled) {
        autoToolEnabled = enabled;
        save();
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

            autoRefillEnabled = Boolean.parseBoolean(
                    properties.getProperty(
                            AUTO_REFILL_KEY,
                            "false"
                    )
            );

            autoToolEnabled = Boolean.parseBoolean(
                    properties.getProperty(
                            AUTO_TOOL_KEY,
                            "false"
                    )
            );

        } catch (IOException e) {

            System.err.println(
                    "Could not load AetherUtilities feature configuration"
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
                AUTO_REFILL_KEY,
                Boolean.toString(autoRefillEnabled)
        );

        properties.setProperty(
                AUTO_TOOL_KEY,
                Boolean.toString(autoToolEnabled)
        );

        try {

            Files.createDirectories(path.getParent());

            try (OutputStream output = Files.newOutputStream(path)) {

                properties.store(
                        output,
                        "AetherUtilities feature configuration"
                );
            }

        } catch (IOException e) {

            System.err.println(
                    "Could not save AetherUtilities feature configuration"
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
                .resolve("aetherutilities-features.properties");
    }
}
