package com.figura.offlinefix.mixin;

import com.figura.offlinefix.FiguraOfflineFix;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class PlayerJoinMixin {
    @Inject(method = "onPlayerList", at = @At("TAIL"))
    private void figuraOfflineFix$onPlayerJoin(PlayerListS2CPacket packet, CallbackInfo ci) {
        if (packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
            packet.getEntries().forEach(entry -> {
                FiguraOfflineFix.log("Вход игрока: " + entry.profile().getName() + " | UUID: " + entry.profile().getId());
            });
        }
    }
}
