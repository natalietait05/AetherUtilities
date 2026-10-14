package com.aetheris.aetherutilities.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.MerchantScreen$TradeOfferButton")
public interface TradeOfferButtonAccessor {

    @Invoker("getIndex")
    int aetherutilities$getIndex();
}