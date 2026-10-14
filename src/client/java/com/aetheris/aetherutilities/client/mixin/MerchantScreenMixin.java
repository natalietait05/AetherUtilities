
package com.aetheris.aetherutilities.client.mixin;

import com.aetheris.aetherutilities.villagertrades.ducks.ClientMerchantMenuDuck;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin
        extends AbstractContainerScreen<MerchantMenu> {

    public MerchantScreenMixin(
            MerchantMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
    }

    @Inject(
            method = "extractContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/MerchantMenu;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"
            )
    )
    private void aetherutilities$enableCombinedOffersForRendering(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci
    ) {
        ClientMerchantMenuDuck duck =
                (ClientMerchantMenuDuck) this.menu;

        duck.aetherutilities$enableCombinedOffers();
    }

    @Inject(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/MerchantMenu;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"
            )
    )
    private void aetherutilities$enableCombinedOffersForClick(
            MouseButtonEvent event,
            boolean doubleClick,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ((ClientMerchantMenuDuck) this.menu)
                .aetherutilities$enableCombinedOffers();
    }

    @Inject(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/MerchantMenu;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"
            )
    )
    private void aetherutilities$enableCombinedOffersForScrolling(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ((ClientMerchantMenuDuck) this.menu)
                .aetherutilities$enableCombinedOffers();
    }

    @Inject(
            method = "mouseDragged",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/MerchantMenu;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"
            )
    )
    private void aetherutilities$enableCombinedOffersForDragging(
            MouseButtonEvent event,
            double deltaX,
            double deltaY,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ((ClientMerchantMenuDuck) this.menu)
                .aetherutilities$enableCombinedOffers();
    }
}
