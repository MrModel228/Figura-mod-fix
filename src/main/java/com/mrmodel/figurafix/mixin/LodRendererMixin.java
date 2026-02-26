package com.mrmodel.figurafix.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.figuramc.figura.avatar.AvatarRenderer", remap = false)
public class LodRendererMixin {
    
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void applyLod(MatrixStack matrices, VertexConsumerProvider v, int l, AbstractClientPlayerEntity player, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) return;
        
        // Сэр, считаем квадрат расстояния (это быстрее для процессора, чем корень)
        double distanceSq = player.squaredDistanceTo(MinecraftClient.getInstance().player);
        
        // Если дальше 64 блоков (64*64 = 4096), отменяем рендер
        if (distanceSq > 4096) {
            ci.cancel();
        }
    }
}
