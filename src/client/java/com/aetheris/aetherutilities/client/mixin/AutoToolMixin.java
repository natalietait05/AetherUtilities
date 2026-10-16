package com.aetheris.aetherutilities.client.mixin;

import com.aetheris.aetherutilities.autotool.AutoTool;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class AutoToolMixin {

    @Inject(
            method = "startAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherutilities$selectTool(
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (AutoTool.selectTool((Minecraft) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}