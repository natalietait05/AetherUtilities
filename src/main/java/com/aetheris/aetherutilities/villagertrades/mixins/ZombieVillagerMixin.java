package com.aetheris.aetherutilities.villagertrades.mixins;

import com.aetheris.aetherutilities.villagertrades.LockedTradeData;
import com.aetheris.aetherutilities.villagertrades.ducks.VillagerDuck;
import com.aetheris.aetherutilities.villagertrades.ducks.ZombieVillagerDuck;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerMixin
        extends Zombie
        implements ZombieVillagerDuck {

    @Unique
    private LockedTradeData aetherutilities$lockedTradeData;

    public ZombieVillagerMixin(
            EntityType<? extends Zombie> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("HEAD")
    )
    private void aetherutilities$saveLockedTradeData(
            ValueOutput output,
            CallbackInfo ci
    ) {
        if (aetherutilities$lockedTradeData != null) {
            aetherutilities$lockedTradeData.save(output);
        }
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void aetherutilities$loadLockedTradeData(
            ValueInput input,
            CallbackInfo ci
    ) {
        aetherutilities$lockedTradeData =
                LockedTradeData.load(
                        input,
                        (ZombieVillager) (Object) this
                ).orElse(null);
    }

    @Inject(
            method = "lambda$finishConversion$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/npc/villager/Villager;setVillagerXp(I)V"
            )
    )
    private void aetherutilities$transferTradesToVillager(
            ServerLevel level,
            Villager villager,
            CallbackInfo ci
    ) {
        VillagerDuck villagerDuck = (VillagerDuck) villager;

        villagerDuck.aetherutilities$setLockedTradeData(
                aetherutilities$lockedTradeData
        );
    }

    @Override
    public void aetherutilities$setLockedTradeData(
            LockedTradeData data
    ) {
        this.aetherutilities$lockedTradeData = data;
    }

    @Override
    public LockedTradeData aetherutilities$getLockedTradeData() {
        return this.aetherutilities$lockedTradeData;
    }
}