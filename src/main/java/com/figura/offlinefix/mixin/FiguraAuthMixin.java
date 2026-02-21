package com.figura.offlinefix.mixin;

import org.figuramc.figura.FiguraMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FiguraMod.class, remap = false)
public class FiguraAuthMixin {
    
    @Inject(method = "isAuthenticated", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onIsAuthenticated(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
    
    @Inject(method = "isOnline", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onIsOnline(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}