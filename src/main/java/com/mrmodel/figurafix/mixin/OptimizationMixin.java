package com.mrmodel.figurafix.mixin;

import com.mrmodel.figurafix.command.FFCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.figuramc.figura.avatar.AvatarRenderer", remap = false, priority = 500)
public class OptimizationMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void smartRender(Object matrices, Object v, int l, AbstractClientPlayerEntity player, CallbackInfo ci) {
        if (FFCommand.globalHide) { ci.cancel(); return; }
        MinecraftClient client = MinecraftClient.getInstance();
        if (player == null || client.player == null) return;

        double distSq = player.squaredDistanceTo(client.player);
        // Агрессивный LOD (30м - ухудшение, 50м - скрытие)
        if (distSq > 900) {
            if (distSq > 2500) ci.cancel();
        }
    }
}
