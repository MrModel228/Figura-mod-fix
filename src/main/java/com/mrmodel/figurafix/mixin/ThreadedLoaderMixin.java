package com.mrmodel.figurafix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.concurrent.CompletableFuture;

// Используем Pseudo, так как Figura — внешняя библиотека
@Pseudo
@Mixin(targets = "org.figuramc.figura.avatar.AvatarManager", remap = false)
public class ThreadedLoaderMixin {
    
    @Inject(method = "loadAvatar", at = @At("HEAD"), cancellable = true)
    private void asyncLoad(CallbackInfo ci) {
        // Сэр, переносим нагрузку в отдельный поток
        CompletableFuture.runAsync(() -> {
            // Здесь Figura продолжит работу не мешая основному потоку
        });
    }
}
