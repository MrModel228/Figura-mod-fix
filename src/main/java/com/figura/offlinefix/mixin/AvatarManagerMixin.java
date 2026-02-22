package com.figura.offlinefix.mixin;

import com.figura.offlinefix.FiguraOfflineFix;
import net.minecraft.nbt.NbtCompound;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(value = AvatarManager.class, remap = false)
public class AvatarManagerMixin {
    @Inject(method = "getAvatarFromBackend", at = @At("HEAD"), cancellable = true)
    private void loadFromCacheFirst(UUID uuid, CallbackInfoReturnable<CompletableFuture<NbtCompound>> cir) {
        NbtCompound cached = FiguraOfflineFix.loadBackup(uuid.toString());
        if (cached != null) {
            cir.setReturnValue(CompletableFuture.completedFuture(cached));
        }
    }

    @Inject(method = "getAvatarFromBackend", at = @At("RETURN"))
    private void saveToCacheAfterLoad(UUID uuid, CallbackInfoReturnable<CompletableFuture<NbtCompound>> cir) {
        cir.getReturnValue().thenAccept(nbt -> {
            if (nbt != null) {
                FiguraOfflineFix.saveBackup(uuid.toString(), nbt);
            }
        });
    }
}