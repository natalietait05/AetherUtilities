
package com.aetheris.aetherutilities.villagertrades;

import com.aetheris.aetherutilities.villagertrades.ducks.ClientMerchantMenuDuck;
import com.aetheris.aetherutilities.villagertrades.networking.ClientboundLockedTradesPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.player.LocalPlayer;

public final class LockedTradesClient {

    private LockedTradesClient() {
    }

    public static void initialize() {

        ClientPlayNetworking.registerGlobalReceiver(
                ClientboundLockedTradesPayload.PACKET_ID,
                (payload, context) -> handlePayload(payload)
        );
    }

    private static void handlePayload(
            ClientboundLockedTradesPayload payload
    ) {

        Minecraft minecraft = Minecraft.getInstance();

        LocalPlayer player = minecraft.player;

        if (player == null) {
            return;
        }

        if (!(minecraft.gui.screen() instanceof MerchantScreen screen)) {
            return;
        }

        ClientMerchantMenuDuck duck =
                ClientMerchantMenuDuck.of(screen.getMenu());

        duck.aetherutilities$setLockedTradeOffers(
                payload.offers()
        );
    }
}
