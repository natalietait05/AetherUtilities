
package com.aetheris.aetherutilities.villagertrades;

import com.aetheris.aetherutilities.AetherUtilities;
import com.aetheris.aetherutilities.villagertrades.ducks.VillagerDuck;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.BooleanSupplier;

public final class LockedTradeData {

    private static final int MAX_LEVEL = 5;

    private static final String SAVE_KEY =
            "AetherUtilitiesLockedOffers";

    private final List<MerchantOffers> lockedOffers =
            new ArrayList<>();

    private final Queue<Consumer<MerchantOffers>> pendingRequests =
            new ArrayDeque<>();

    private boolean activelyGenerating;

    public LockedTradeData(Villager villager) {
        generateTrades(villager);
    }

    private LockedTradeData(List<MerchantOffers> offers) {
        lockedOffers.addAll(offers);
    }

    /*
     * Loads saved future trades.
     */
    public static Optional<LockedTradeData> load(
            ValueInput input,
            Entity entity
    ) {

        if (!(entity.level() instanceof ServerLevel)) {
            return Optional.empty();
        }

        Optional<List<MerchantOffers>> savedOffers =
                input.read(
                        SAVE_KEY,
                        MerchantOffers.CODEC.listOf()
                );

        return savedOffers.map(LockedTradeData::new);
    }

    /*
     * Saves future trades.
     */
    public void save(ValueOutput output) {

        output.store(
                SAVE_KEY,
                MerchantOffers.CODEC.listOf(),
                lockedOffers
        );
    }

    /*
     * Generates all future trade sets on the server thread.
     */
    public void generateTrades(Villager villager) {

        if (!(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (activelyGenerating) {
            return;
        }

        activelyGenerating = true;

        List<MerchantOffers> generatedOffers =
                new ArrayList<>();

        try {

            int currentLevel =
                    villager.getVillagerData().level();

            for (
                    int level = currentLevel + 1;
                    level <= MAX_LEVEL;
                    level++
            ) {

                VillagerData data =
                        villager.getVillagerData().withLevel(level);

                VillagerProfession profession =
                        data.profession().value();

                ResourceKey<TradeSet> tradeSet =
                        profession.getTrades(level);

                MerchantOffers offers =
                        new MerchantOffers();

                if (tradeSet != null) {

                    VillagerDuck.of(villager)
                            .aetherutilities$addOffersFromTradeSet(
                                    serverLevel,
                                    offers,
                                    tradeSet
                            );
                }

                // Keep one entry per level, even if it has no trades.
                generatedOffers.add(offers);
            }

            lockedOffers.clear();
            lockedOffers.addAll(generatedOffers);

        } catch (Exception exception) {

            AetherUtilities.LOGGER.error(
                    "Could not generate future villager trades",
                    exception
            );

        } finally {

            activelyGenerating = false;

            processPendingRequests();
        }
    }

    /*
     * Returns true when there are no future trade sets.
     */
    public boolean hasNoOffers() {
        return lockedOffers.isEmpty();
    }

    /*
     * Removes and returns the next level's trade set.
     */
    public Optional<MerchantOffers> popTradeSet() {

        if (lockedOffers.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                lockedOffers.removeFirst()
        );
    }

    /*
     * Combines all remaining future trade sets.
     */
    public MerchantOffers condense() {

        MerchantOffers result =
                new MerchantOffers();

        for (MerchantOffers offers : lockedOffers) {
            result.addAll(offers);
        }

        return result;
    }

    /*
     * Number of remaining future levels.
     */
    public int getRemainingLevels() {
        return lockedOffers.size();
    }

    public boolean isGenerating() {
        return activelyGenerating;
    }

    /*
     * Queues a request if generation is in progress.
     * Otherwise, responds immediately.
     */
    public void requestOffers(
            Consumer<MerchantOffers> callback
    ) {

        if (activelyGenerating) {
            pendingRequests.add(callback);
            return;
        }

        callback.accept(condense());
    }

    /*
     * Responds to queued requests after generation.
     */
    private void processPendingRequests() {

        if (activelyGenerating) {
            return;
        }

        while (!pendingRequests.isEmpty()) {

            Consumer<MerchantOffers> callback =
                    pendingRequests.poll();

            if (callback != null) {
                callback.accept(condense());
            }
        }
    }

    /*
     * Keeps the number of future sets synchronized with
     * the villager's current level.
     */
    public void tick(
            Villager villager,
            BooleanSupplier popCallback
    ) {

        int requiredSets =
                MAX_LEVEL - villager.getVillagerData().level();

        while (
                lockedOffers.size() > requiredSets
                        && popCallback.getAsBoolean()
        ) {
            // The callback removes and appends one unlocked set.
        }

        if (
                lockedOffers.size() < requiredSets
                        && !activelyGenerating
        ) {

            AetherUtilities.LOGGER.warn(
                    "Missing future villager trades. Regenerating."
            );

            generateTrades(villager);
        }

        processPendingRequests();
    }
}
