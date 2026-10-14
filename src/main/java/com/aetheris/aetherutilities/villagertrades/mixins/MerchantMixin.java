package com.aetheris.aetherutilities.villagertrades.mixins;

import com.aetheris.aetherutilities.villagertrades.ducks.VillagerDuck;
import com.aetheris.aetherutilities.villagertrades.networking.ClientboundLockedTradesPayload;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(Merchant.class)
public interface MerchantMixin {

    @WrapOperation(
            method = "openTradingScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;sendMerchantOffers(ILnet/minecraft/world/item/trading/MerchantOffers;IIZZ)V"
            )
    )
    private void aetherutilities$sendLockedOffers(
            Player player,
            int containerId,
            MerchantOffers offers,
            int merchantLevel,
            int merchantXp,
            boolean showProgressBar,
            boolean canRestock,
            Operation<Void> original
    ) {
        // Primero se envían los intercambios normales.
        original.call(
                player,
                containerId,
                offers,
                merchantLevel,
                merchantXp,
                showProgressBar,
                canRestock
        );

        // Solo nos interesan los aldeanos.
        if (!((Merchant) this instanceof Villager villager)) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        VillagerDuck duck = (VillagerDuck) villager;

        Optional<MerchantOffers> lockedOffers =
                duck.aetherutilities$getCondensedOffers();

        if (lockedOffers.isPresent()) {
            ServerPlayNetworking.send(
                    serverPlayer,
                    new ClientboundLockedTradesPayload(lockedOffers)
            );
        } else {
            duck.aetherutilities$requestOffers(serverPlayer);
        }
    }
}