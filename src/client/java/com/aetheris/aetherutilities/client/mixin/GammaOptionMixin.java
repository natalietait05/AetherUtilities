
package com.aetheris.aetherutilities.client.mixin;

import com.aetheris.aetherutilities.gamma.GammaClient;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionInstance.class)
public abstract class GammaOptionMixin<T> {

    @Shadow
    @Final
    Component caption;

    @Inject(
            method = "get",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherutilities$getGamma(
            CallbackInfoReturnable<Double> info
    ) {
        if (isGammaOption()) {
            info.setReturnValue(
                    GammaClient.getGammaValue()
            );
        }
    }

    @Inject(
            method = "set",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherutilities$setGamma(
            T value,
            CallbackInfo info
    ) {
        if (isGammaOption()) {
            info.cancel();
        }
    }

    @Unique
    private boolean isGammaOption() {

        if (caption.getContents() instanceof TranslatableContents contents) {
            return contents.getKey().equals("options.gamma");
        }

        return false;
    }
}
