package com.figura.offlinefix;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FiguraOfflineFix implements ClientModInitializer {
    public static final String MOD_ID = "figura-offline-fix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static final Map<String, NbtCompound> AVATAR_CACHE = new HashMap<>();
    private static final File CACHE_DIR = new File("figura_offline_cache");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Figura Offline Fix: Initializing 1.2.5...");

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
                    } catch (Exception e) { e.printStackTrace(); }
                }).start();
            }
        });
    }

    public static void updateAvatarManual(PlayerEntity player) {
        try {
            // ШАГ 1: Достаем менеджер через рефлексию (так как метод getAvatarManager не найден)
            Class<?> figuraModClass = Class.forName("org.figuramc.figura.FiguraMod");
            Object manager = null;
            
            for (Field f : figuraModClass.getDeclaredFields()) {
                if (f.getType().getName().contains("AvatarManager")) {
                    f.setAccessible(true);
                    manager = f.get(null);
                    break;
                }
            }

            if (manager == null) {
                LOGGER.error("Figura Offline Fix: Could not find AvatarManager field!");
                return;
            }

            // ШАГ 2: Ищем метод обновления
            for (Method m : manager.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(player.getClass())) {
                    m.setAccessible(true);
                    m.invoke(manager, player);
                    LOGGER.info("Figura Offline Fix: Updated avatar for " + player.getName().getString());
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Figura Offline Fix: Update failed: " + e.getMessage());
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
            LOGGER.error("Error saving cache: " + e.getMessage());
        }
    }

    public static NbtCompound loadBackup(String uuid) {
        if (AVATAR_CACHE.containsKey(uuid)) return AVATAR_CACHE.get(uuid);
        File cacheFile = new File(CACHE_DIR, uuid + ".dat");
        if (cacheFile.exists()) {
            try (FileInputStream fis = new FileInputStream(cacheFile)) {
                return NbtIo.readCompressed(fis, NbtSizeTracker.ofUnlimitedBytes());
            } catch (Exception e) {
                LOGGER.error("Error loading cache: " + e.getMessage());
            }
        }
        return null;
    }

    public static void log(String message) {
        LOGGER.info(message);
    }
}