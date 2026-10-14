
package com.aetheris.aetherutilities.client.mixin;

import com.aetheris.aetherutilities.villagertrades.ducks.ClientMerchantMenuDuck;

import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin implements ClientMerchantMenuDuck {

    @Shadow
    @Final
    private Merchant trader;

    @Unique
    private MerchantOffers aetherutilities$combinedOffers;

    @Unique
    private boolean aetherutilities$useCombinedOffers;

    @Unique
    private int aetherutilities$combinedOffersScopeDepth;

    @Override
    public int aetherutilities$getOriginalOfferCount() {
        return trader.getOffers().size();
    }

    @Override
    public int aetherutilities$getCombinedOfferCount() {
        return aetherutilities$combinedOffers == null
                ? trader.getOffers().size()
                : aetherutilities$combinedOffers.size();
    }

    /*
     * Receives future trades from the server.
     */
    @Override
    public void aetherutilities$setLockedTradeOffers(
            Optional<MerchantOffers> maybeOffers
    ) {

        MerchantOffers combined = new MerchantOffers();

        combined.addAll(trader.getOffers());

        maybeOffers.ifPresent(combined::addAll);

        aetherutilities$combinedOffers = combined;
    }

    /*
     * Enables combined offers for the next getOffers() call.
     */
    @Override
    public void aetherutilities$enableCombinedOffers() {

        aetherutilities$useCombinedOffers = true;
    }

    /*
     * Begins a scope in which getOffers() always returns
     * the combined list.
     */
    @Override
    public void aetherutilities$beginCombinedOffersScope() {

        aetherutilities$combinedOffersScopeDepth++;
    }

    /*
     * Ends the combined offers scope.
     */
    @Override
    public void aetherutilities$endCombinedOffersScope() {

        aetherutilities$combinedOffersScopeDepth = Math.max(
                0,
                aetherutilities$combinedOffersScopeDepth - 1
        );
    }

    /*
     * Intercepts the menu's offer list.
     */
    @Inject(
            method = "getOffers",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherutilities$useCombinedOffers(
            CallbackInfoReturnable<MerchantOffers> cir
    ) {

        if (aetherutilities$combinedOffers == null) {
            return;
        }

        if (aetherutilities$combinedOffersScopeDepth > 0) {

            cir.setReturnValue(
                    aetherutilities$combinedOffers
            );

            return;
        }

        if (aetherutilities$useCombinedOffers) {

            aetherutilities$useCombinedOffers = false;

            cir.setReturnValue(
                    aetherutilities$combinedOffers
            );
        }
    }
}
