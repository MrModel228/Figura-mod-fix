package com.figura.offlinefix;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class FiguraFixCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("figura-fix")
            .then(ClientCommandManager.literal("reload").executes(ctx -> {
                triggerGlobalReload();
                ctx.getSource().sendFeedback(Text.literal("§6[FiguraFix] §aМодели всех игроков обновлены!"));
                return 1;
            }))
        );
    }

    public static void triggerGlobalReload() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            client.world.getPlayers().forEach(player -> {
                // Используем существующий метод из FiguraOfflineFix
                FiguraOfflineFix.updateAvatarManual(player);
            });
            FiguraOfflineFix.log("Global reload executed.");
        }
    }
}
