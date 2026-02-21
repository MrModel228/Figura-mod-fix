package com.figura.offlinefix.mixin;

import com.figura.offlinefix.FiguraOfflineFix;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.UUID;

@Pseudo
@Mixin(targets = "org.figuramc.figura.avatar.AvatarManager", remap = false)
public class AvatarManagerMixin {
    
    @Inject(method = "canLoadFor", at = @At("HEAD"), cancellable = true, remap = false)
    private void allowOffline(UUID uuid, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true); // Разрешаем загрузку даже если мы "оффлайн"
    }

    @Inject(method = "loadAvatar", at = @At("TAIL"), remap = false)
    private static void onCacheAvatar(UUID uuid, NbtCompound nbt, CallbackInfo ci) {
        if (nbt != null) {
            FiguraOfflineFix.saveBackup(uuid.toString(), nbt);
        }
    }
}