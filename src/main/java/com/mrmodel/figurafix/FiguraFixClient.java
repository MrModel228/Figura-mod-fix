package com.mrmodel.figurafix;

import com.mrmodel.figurafix.command.FFCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FiguraFixClient implements ClientModInitializer {
    public static final String MOD_ID = "figurafix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("§b[FiguraFix] §aСистема ULTIMATE для Reign RP запущена, Сэр!");
        
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            FFCommand.register(dispatcher);
        });
    }
}
