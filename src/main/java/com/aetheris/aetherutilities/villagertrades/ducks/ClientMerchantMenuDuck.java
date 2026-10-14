
package com.aetheris.aetherutilities.villagertrades.ducks;

import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.Optional;

public interface ClientMerchantMenuDuck {

    int aetherutilities$getOriginalOfferCount();

    int aetherutilities$getCombinedOfferCount();

    void aetherutilities$setLockedTradeOffers(
            Optional<MerchantOffers> offers
    );

    void aetherutilities$enableCombinedOffers();

    void aetherutilities$beginCombinedOffersScope();

    void aetherutilities$endCombinedOffersScope();

    static ClientMerchantMenuDuck of(MerchantMenu menu) {
        return (ClientMerchantMenuDuck) menu;
    }
}
