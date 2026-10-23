
package com.aetheris.aetherutilities.client;

import com.aetheris.aetherutilities.AetherUtilities;
import com.aetheris.aetherutilities.inventorysorter.InventorySorterClient;
import com.aetheris.aetherutilities.lightlevels.LightLevelsClient;
import com.aetheris.aetherutilities.zoom.ZoomClient;
import com.aetheris.aetherutilities.gamma.GammaClient;
import com.aetheris.aetherutilities.villagertrades.LockedTradesClient;
import com.aetheris.aetherutilities.autorefill.AutoRefill;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import com.aetheris.aetherutilities.autotool.AutoTool;
import com.aetheris.aetherutilities.navigation.NavigationConfig;
import com.aetheris.aetherutilities.navigation.NavigationClient;


public final class AetherUtilitiesClient implements ClientModInitializer {

    public static KeyMapping.Category CATEGORY;
    public static KeyMapping CONFIG_KEY;

    @Override
    public void onInitializeClient() {

        CATEGORY = KeyMapping.Category.register(
                AetherUtilities.id("controls")
        );

        CONFIG_KEY = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.aetherutilities.config",
                        InputConstants.KEY_K,
                        CATEGORY
                )
        );

        AutoFeaturesConfig.load();
        NavigationConfig.load();

        LightLevelsClient.initialize();
        ZoomClient.initialize();
        InventorySorterClient.initialize();
        GammaClient.initialize();
        LockedTradesClient.initialize();
        NavigationClient.initialize();

        AetherUtilitiesKeybindConfig.load();

        AutoRefill.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(AutoTool::onClientTick);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (CONFIG_KEY.consumeClick()) {

                Screen parent = client.gui.screen();

                client.gui.setScreen(
                        new AetherUtilitiesConfigScreen(parent)
                );
            }
        });
    }
}
