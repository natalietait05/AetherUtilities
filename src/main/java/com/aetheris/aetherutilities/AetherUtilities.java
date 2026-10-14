package com.aetheris.aetherutilities;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aetheris.aetherutilities.villagertrades.networking.ClientboundLockedTradesPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class AetherUtilities implements ModInitializer {

    public static final String MOD_ID = "aetherutilities";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(
                ClientboundLockedTradesPayload.PACKET_ID,
                ClientboundLockedTradesPayload.PACKET_CODEC
        );

        LOGGER.info("AetherUtilities initialized");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }


}
