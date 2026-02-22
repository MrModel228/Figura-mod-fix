package com.figura.offlinefix.mixin;

import com.figura.offlinefix.FiguraOfflineFix;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class PlayerJoinMixin {
    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        FiguraOfflineFix.LOGGER.info("Player joined, Figura fix ready.");
    }
}