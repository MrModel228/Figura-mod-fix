package com.mrmodel.figurafix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class FFCommand {
    public static boolean debugMode = false;
    public static boolean globalHide = false;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var root = ClientCommandManager.literal("figurafix")
            .then(ClientCommandManager.literal("debug").then(ClientCommandManager.argument("state", BoolArgumentType.bool()).executes(ctx -> {
                debugMode = BoolArgumentType.getBool(ctx, "state");
                ctx.getSource().sendFeedback(Text.literal("§b[FF] §fDebug: " + (debugMode ? "§aВкл" : "§cВыкл")));
                return 1;
            })))
            .then(ClientCommandManager.literal("hide").then(ClientCommandManager.argument("state", BoolArgumentType.bool()).executes(ctx -> {
                globalHide = BoolArgumentType.getBool(ctx, "state");
                ctx.getSource().sendFeedback(Text.literal("§b[FF] §fСкрытие: " + (globalHide ? "§aВкл" : "§cВыкл")));
                return 1;
            })))
            .then(ClientCommandManager.literal("reload").executes(ctx -> {
                ctx.getSource().sendFeedback(Text.literal("§b[FF] §6Перезагрузка..."));
                return 1;
            }));
        
        dispatcher.register(root);
        dispatcher.register(ClientCommandManager.literal("ff").redirect(dispatcher.getRoot().getChild("figurafix")));
    }
}
