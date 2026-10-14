package com.aetheris.aetherutilities.villagertrades.mixins;

import com.aetheris.aetherutilities.villagertrades.ducks.VillagerDuck;
import com.aetheris.aetherutilities.villagertrades.ducks.ZombieVillagerDuck;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public class ZombieMixin {

    @Inject(
            method = "lambda$convertVillagerToZombieVillager$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/zombie/ZombieVillager;setVillagerXp(I)V"
            )
    )
    private void aetherutilities$transferTradesToZombieVillager(
            Villager villager,
            ServerLevel level,
            ZombieVillager zombie,
            CallbackInfo ci
    ) {
        VillagerDuck villagerDuck = (VillagerDuck) villager;

        ZombieVillagerDuck zombieDuck =
                (ZombieVillagerDuck) zombie;

        villagerDuck.aetherutilities$getLockedTradeData()
                .ifPresent(zombieDuck::aetherutilities$setLockedTradeData);
    }
}