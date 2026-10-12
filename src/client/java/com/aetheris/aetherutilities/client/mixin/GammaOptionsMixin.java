package com.aetheris.aetherutilities.client.mixin;

import com.aetheris.aetherutilities.gamma.GammaClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(Options.class)
public abstract class GammaOptionsMixin {

    @WrapOperation(
            method = "processDumpedOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Options$OptionAccess;process(Ljava/lang/String;Lnet/minecraft/client/OptionInstance;)V"
            )
    )
    private <T> void aetherutilities$preventGammaSave(
            @Coerce Object access,
            String name,
            OptionInstance<T> option,
            Operation<Void> original
    ) {

        if (name.equals("gamma") && GammaClient.isEnabled()) {
            return;
        }

        original.call(access, name, option);
    }
}