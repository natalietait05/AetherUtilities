
package com.aetheris.aetherutilities.villagertrades.mixins;

import com.aetheris.aetherutilities.AetherUtilities;
import com.aetheris.aetherutilities.villagertrades.LockedTradeData;
import com.aetheris.aetherutilities.villagertrades.ducks.VillagerDuck;
import com.aetheris.aetherutilities.villagertrades.networking.ClientboundLockedTradesPayload;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerDataHolder;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Villager.class)
public abstract class VillagerMixin
        extends AbstractVillager
        implements ReputationEventHandler, VillagerDataHolder, VillagerDuck {

    /*
     * VANILLA DATA
     */

    @Shadow
    public abstract @NotNull VillagerData getVillagerData();


    /*
     * LOCKED TRADE DATA
     */

    @Unique
    private LockedTradeData aetherutilities$lockedTradeData;


    /*
     * CONSTRUCTOR
     */

    public VillagerMixin(
            EntityType<? extends AbstractVillager> entityType,
            Level level
    ) {

        super(entityType, level);
    }


    /*
     * INTERNAL DATA ACCESS
     */

    @Unique
    private Optional<LockedTradeData> aetherutilities$getData() {

        return Optional.ofNullable(
                this.aetherutilities$lockedTradeData
        );
    }


    /*
     * SAVE DATA
     */

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("HEAD")
    )
    private void aetherutilities$saveLockedTrades(
            ValueOutput output,
            CallbackInfo ci
    ) {

        aetherutilities$getData().ifPresent(
                data -> data.save(output)
        );
    }


    /*
     * LOAD DATA
     */

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void aetherutilities$loadLockedTrades(
            ValueInput input,
            CallbackInfo ci
    ) {

        this.aetherutilities$lockedTradeData =
                LockedTradeData.load(
                        input,
                        (Villager) (Object) this
                ).orElse(null);
    }


    /*
     * VILLAGER TICK
     *
     * Initializes missing data and keeps the remaining
     * future trade sets synchronized with the villager level.
     */

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void aetherutilities$tickLockedTrades(
            CallbackInfo ci
    ) {

        Villager villager =
                (Villager) (Object) this;

        /*
         * Do not generate future trades before vanilla
         * has initialized the villager's actual offers.
         */
        if (this.offers == null || this.offers.isEmpty()) {
            return;
        }

        /*
         * Generate future trades for existing villagers
         * that do not have saved data.
         */
        if (this.aetherutilities$lockedTradeData == null) {

            this.aetherutilities$lockedTradeData =
                    new LockedTradeData(villager);

            return;
        }

        /*
         * Keep the remaining trade sets synchronized.
         */
        this.aetherutilities$lockedTradeData.tick(
                villager,
                this::aetherutilities$appendLockedOffer
        );
    }


    /*
     * LEVEL UP
     *
     * When vanilla attempts to generate trades for a new
     * villager level, use the already generated trade set.
     */

    @Inject(
            method = "updateTrades",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherutilities$useLockedTradesOnLevelUp(
            CallbackInfo ci
    ) {

        /*
         * Let vanilla initialize the first set of offers.
         */
        if (this.offers == null || this.offers.isEmpty()) {

            this.aetherutilities$lockedTradeData = null;

            return;
        }

        /*
         * If a future trade set exists, append it and
         * prevent vanilla from generating duplicate offers.
         */
        if (aetherutilities$appendLockedOffer()) {
            ci.cancel();
        }
    }


    /*
     * APPEND FUTURE TRADE SET
     *
     * Returns true only if a trade set was successfully
     * removed from LockedTradeData and appended.
     */

    @Unique
    private boolean aetherutilities$appendLockedOffer() {

        if (this.offers == null) {
            return false;
        }

        LockedTradeData data =
                this.aetherutilities$lockedTradeData;

        if (data == null) {
            return false;
        }

        Optional<MerchantOffers> nextOffers =
                data.popTradeSet();

        if (nextOffers.isEmpty()) {
            return false;
        }

        this.offers.addAll(
                nextOffers.get()
        );

        return true;
    }


    /*
     * ADD OFFERS BRIDGE
     *
     * Exposes the protected vanilla method to LockedTradeData.
     */

    @Override
    public void aetherutilities$addOffersFromTradeSet(
            ServerLevel serverLevel,
            MerchantOffers offers,
            ResourceKey<TradeSet> tradeSet
    ) {

        this.addOffersFromTradeSet(
                serverLevel,
                offers,
                tradeSet
        );
    }


    /*
     * DUCK IMPLEMENTATION
     */

    @Override
    public void aetherutilities$setLockedTradeData(
            LockedTradeData data
    ) {

        this.aetherutilities$lockedTradeData = data;
    }


    @Override
    public Optional<LockedTradeData> aetherutilities$getLockedTradeData() {

        return aetherutilities$getData();
    }


    /*
     * REGENERATE FUTURE TRADES
     */

    @Override
    public void aetherutilities$regenerateTrades() {

        Villager villager =
                (Villager) (Object) this;

        this.aetherutilities$lockedTradeData =
                new LockedTradeData(villager);
    }


    /*
     * GET ALL FUTURE TRADES
     */

    @Override
    public Optional<MerchantOffers> aetherutilities$getCondensedOffers() {

        LockedTradeData data =
                this.aetherutilities$lockedTradeData;

        if (data == null) {

            aetherutilities$regenerateTrades();

            return Optional.empty();
        }

        MerchantOffers offers =
                data.condense();

        if (offers.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(offers);
    }


    /*
     * SEND FUTURE TRADES TO CLIENT
     */

    @Override
    public void aetherutilities$requestOffers(
            ServerPlayer player
    ) {

        /*
         * First clear any previously displayed future offers.
         */
        ServerPlayNetworking.send(
                player,
                new ClientboundLockedTradesPayload(
                        Optional.empty()
                )
        );

        /*
         * Generate data if this villager does not have it yet.
         */
        if (this.aetherutilities$lockedTradeData == null) {

            aetherutilities$regenerateTrades();
        }

        LockedTradeData data =
                this.aetherutilities$lockedTradeData;

        if (data == null) {
            return;
        }

        /*
         * Send the offers.
         *
         * LockedTradeData handles pending callbacks if
         * generation is currently in progress.
         */
        data.requestOffers(
                offers -> {

                    if (!player.isRemoved()
                            && player.connection != null) {

                        ServerPlayNetworking.send(
                                player,
                                new ClientboundLockedTradesPayload(
                                        offers
                                )
                        );
                    }
                }
        );
    }
}
