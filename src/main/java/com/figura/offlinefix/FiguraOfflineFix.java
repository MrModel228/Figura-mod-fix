package com.figura.offlinefix;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.AvatarManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class FiguraOfflineFix implements ClientModInitializer {
    public static final String MOD_ID = "figura-offline-fix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Map<String, NbtCompound> AVATAR_CACHE = new HashMap<>();
    private static final File CACHE_DIR = new File("figura_offline_cache");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Figura Offline Fix: Compatibility Mode (V5 + Extra Bone)");
        if (!CACHE_DIR.exists()) CACHE_DIR.mkdirs();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            FiguraFixCommand.register(dispatcher);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        updateAvatarManual(client.player);
                    } catch (InterruptedException e) {
                        LOGGER.error("Auto-update thread interrupted");
                    }
                }).start();
            }
        });
    }

    public static void updateAvatarManual(PlayerEntity player) {
        if (!(player instanceof AbstractClientPlayerEntity clientPlayer)) return;
        try {
            AvatarManager manager = FiguraMod.getAvatarManager();
            if (manager != null) {
                manager.update(clientPlayer);
                LOGGER.info("Manual avatar refresh triggered for: " + player.getGameProfile().getName());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to refresh avatar: " + e.getMessage());
        }
    }

    public static void saveBackup(String uuid, NbtCompound nbt) {
        try {
            AVATAR_CACHE.put(uuid, nbt);
            File cacheFile = new File(CACHE_DIR, uuid + ".dat");
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                NbtIo.writeCompressed(nbt, fos);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save avatar to disk: " + e.getMessage());
        }
    }

    public static NbtCompound loadBackup(String uuid) {
        if (AVATAR_CACHE.containsKey(uuid)) return AVATAR_CACHE.get(uuid);
        File cacheFile = new File(CACHE_DIR, uuid + ".dat");
        if (cacheFile.exists()) {
            try (FileInputStream fis = new FileInputStream(cacheFile)) {
                NbtCompound nbt = NbtIo.readCompressed(fis, NbtSizeTracker.ofUnlimitedBytes());
                AVATAR_CACHE.put(uuid, nbt);
                return nbt;
            } catch (Exception e) {
                LOGGER.error("Failed to load avatar from disk: " + e.getMessage());
            }
        }
        return null;
    }
}