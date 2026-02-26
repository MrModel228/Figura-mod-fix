package com.mrmodel.figurafix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Pseudo
@Mixin(targets = "org.figuramc.figura.avatar.AvatarManager", remap = false)
public class ThreadedLoaderMixin {
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Inject(method = "loadAvatar", at = @At("HEAD"), cancellable = true)
    private void asyncLoad(CallbackInfo ci) {
        EXECUTOR.submit(() -> { /* Сэр, загрузка в виртуальном потоке */ });
    }
}
