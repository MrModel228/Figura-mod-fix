package com.figura.offlinefix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "org.figuramc.figura.FiguraMod", remap = false)
public class FiguraAuthMixin {
    
    @Inject(method = "isAuthenticated", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onIsAuthenticated(CallbackInfoReturnable<Boolean> cir) {
        // Всегда возвращаем true, чтобы Figura думала, что авторизация прошла успешно
        cir.setReturnValue(true);
    }
    
    @Inject(method = "isOnline", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onIsOnline(CallbackInfoReturnable<Boolean> cir) {
        // Всегда возвращаем true, чтобы Figura думала, что мы онлайн
        cir.setReturnValue(true);
    }
}
