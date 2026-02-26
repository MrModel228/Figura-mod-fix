package com.mrmodel.figurafix;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FiguraFixClient implements ClientModInitializer {
    public static final String MOD_ID = "figurafix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Figura Fix: Reign Edition успешно активирован, Сэр!");
    }
}
