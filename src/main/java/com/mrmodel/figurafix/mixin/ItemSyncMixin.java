package com.mrmodel.figurafix.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "org.figuramc.figura.utils.ItemUtils", remap = false, priority = 1001)
public class ItemSyncMixin {
    @Inject(method = "getHeldItem", at = @At("RETURN"), cancellable = true)
    private static void forceSync(AbstractClientPlayerEntity player, boolean offhand, CallbackInfoReturnable<ItemStack> cir) {
        if (player != null) {
            ItemStack stack = offhand ? player.getOffHandStack() : player.getMainHandStack();
            if (!stack.isEmpty()) cir.setReturnValue(stack);
        }
    }
}
