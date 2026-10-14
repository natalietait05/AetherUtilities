
package com.aetheris.aetherutilities.villagertrades.ducks;

import com.aetheris.aetherutilities.villagertrades.LockedTradeData;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;

import java.util.Optional;

public interface VillagerDuck {

    static VillagerDuck of(Villager villager) {
        return (VillagerDuck) villager;
    }

    void aetherutilities$setLockedTradeData(
            LockedTradeData data
    );

    Optional<LockedTradeData> aetherutilities$getLockedTradeData();

    void aetherutilities$regenerateTrades();

    Optional<MerchantOffers> aetherutilities$getCondensedOffers();

    void aetherutilities$requestOffers(ServerPlayer player);

    void aetherutilities$addOffersFromTradeSet(
            ServerLevel serverLevel,
            MerchantOffers offers,
            ResourceKey<TradeSet> tradeSet
    );
}
