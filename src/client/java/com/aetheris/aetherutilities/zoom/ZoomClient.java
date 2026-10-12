package com.aetheris.aetherutilities.zoom;

import com.aetheris.aetherutilities.client.AetherUtilitiesClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class ZoomClient {

    public static KeyMapping ZOOM_KEY;

    private ZoomClient() {
    }

    public static void initialize() {

        ZOOM_KEY = new KeyMapping(
                "key.aetherutilities.zoom",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_C,
                AetherUtilitiesClient.CATEGORY
        );

        ZoomConfig.load();
    }

    public static boolean isZooming() {

        return ZOOM_KEY != null && ZOOM_KEY.isDown();
    }
}