package com.figura.offlinefix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "org.figuramc.figura.utils.UserUtils", remap = false)
public class UserUtilsMixin {
    @Inject(method = "isProfileVerified", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void figuraOfflineFix$isProfileVerified(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "isOnlineMode", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void figuraOfflineFix$isOnlineMode(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "getBackendStatus", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void figuraOfflineFix$getBackendStatus(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(1); // Статус "Connected"
    }

    @Inject(method = "getMaxComplexity", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void figuraOfflineFix$getMaxComplexity(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(1000000); // 1 млн лимит сложности
    }
}

