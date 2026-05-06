package org.figuramc.figurafix;

import net.fabricmc.api.ClientModInitializer;
import org.figuramc.figurafix.github.GitHubAPI;
import org.figuramc.figurafix.github.ModelManager;
import org.figuramc.figurafix.github.SyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FiguraFix implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Figura-Fix");
    public static FiguraFix INSTANCE;
    public static SyncManager SYNC_MANAGER;
    public static ModelManager MODEL_MANAGER;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        
        GitHubAPI gitHubAPI = new GitHubAPI("none", "MrModel228", "figura-fix-models");
        MODEL_MANAGER = new ModelManager(gitHubAPI);
        SYNC_MANAGER = new SyncManager(gitHubAPI, MODEL_MANAGER);
        
        LOGGER.info("========================================");
        LOGGER.info("   Figura-Fix v1.0.5 Loading...");
        LOGGER.info("========================================");
        LOGGER.info("GitHub sync: enabled");
        LOGGER.info("Repo: MrModel228/figura-fix-models");
        LOGGER.info("Auto sync: start=enabled, exit=enabled");
        LOGGER.info("File watcher: enabled");
        LOGGER.info("========================================");
        LOGGER.info("[Figura-Fix] Figura-Fix is working!");
        
        if (SYNC_MANAGER.isAutoStart()) {
            new Thread(() -> {
                LOGGER.info("[Figura-Fix] Auto-start sync...");
                SYNC_MANAGER.syncAll();
            }).start();
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("[Figura-Fix] Shutting down file watcher...");
            if (MODEL_MANAGER != null) {
                MODEL_MANAGER.stopWatcher();
            }
            if (SYNC_MANAGER.isAutoExit()) {
                new Thread(() -> {
                    LOGGER.info("[Figura-Fix] Auto-exit sync...");
                    SYNC_MANAGER.syncAll();
                }).start();
            }
        }));
    }
}
