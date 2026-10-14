package com.aetheris.aetherutilities.client.mixin;

import com.aetheris.aetherutilities.villagertrades.ducks.ClientMerchantMenuDuck;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.MerchantScreen$TradeOfferButton")
public class TradeOfferButtonMixin {

    @Shadow
    @Final
    private MerchantScreen this$0;

    @WrapMethod(method = "extractToolTip")
    private void aetherutilities$useCombinedOffersForTooltip(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            Operation<Void> original
    ) {
        ClientMerchantMenuDuck duck =
                (ClientMerchantMenuDuck) this.this$0.getMenu();

        duck.aetherutilities$beginCombinedOffersScope();

        try {
            original.call(graphics, mouseX, mouseY);
        } finally {
            duck.aetherutilities$endCombinedOffersScope();
        }
    }
}