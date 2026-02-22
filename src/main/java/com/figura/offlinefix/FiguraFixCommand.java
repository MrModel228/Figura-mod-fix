package com.figura.offlinefix;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.UserUtils;

public class FiguraFixCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("figura-fix")
            .then(ClientCommandManager.literal("reload")
                .executes(context -> {
                    // Отправляем сообщение в ЛС (false означает, что другие игроки его не увидят)
                    context.getSource().sendFeedback(Text.literal("§6[FiguraFix] §eПерезагрузка аватара..."), false);
                    FiguraOfflineFix.updateAvatarManual(context.getSource().getPlayer());
                    return 1;
                })
            )
            .then(ClientCommandManager.literal("debug")
                .executes(context -> {
                    boolean authenticated = FiguraMod.isOnline();
                    boolean verified = UserUtils.isProfileVerified();
                    int status = UserUtils.getBackendStatus();
                    
                    // Вывод всей технической инфы только игроку в ЛС
                    context.getSource().sendFeedback(Text.literal("§b--- [Figura Fix Debug Info] ---"), false);
                    context.getSource().sendFeedback(Text.literal("§fСтатус авторизации: " + (authenticated ? "§aОК" : "§cОШИБКА")), false);
                    context.getSource().sendFeedback(Text.literal("§fВерификация профиля: " + (verified ? "§aПРОЙДЕНА" : "§cНЕТ")), false);
                    context.getSource().sendFeedback(Text.literal("§fСтатус бэкенда: §e" + status), false);
                    context.getSource().sendFeedback(Text.literal("§fВерсия фикса: §71.2.5"), false);
                    context.getSource().sendFeedback(Text.literal("§b----------------------------"), false);
                    return 1;
                })
            )
            .executes(context -> {
                context.getSource().sendFeedback(Text.literal("§6[FiguraFix] §fИспользуй: §e/figura-fix <reload|debug>"), false);
                return 1;
            })
        );
    }
}