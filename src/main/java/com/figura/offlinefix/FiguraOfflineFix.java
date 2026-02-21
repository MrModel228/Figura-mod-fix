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
        LOGGER.info("Figura Offline Fix: Initializing 1.2.5 (1.21.8 Fixed)...");

        if (!CACHE_DIR.exists()) CACHE_DIR.mkdirs();

        // Регистрация команды
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            FiguraFixCommand.register(dispatcher);
        });

        // Слушатель входа
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                new Thread(() -> {
                    try {
                        // Ждем чуть дольше (5 сек), чтобы все ресурсы Figura успели прогрузиться
                        Thread.sleep(5000);
                        updateAvatarManual(client.player);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    public static void updateAvatarManual(PlayerEntity player) {
        try {
            Class<?> figuraModClass = Class.forName("org.figuramc.figura.FiguraMod");
            Object manager = null;

            // Сначала ищем во всех методах FiguraMod тот, который возвращает AvatarManager
            for (Method m : figuraModClass.getDeclaredMethods()) {
                if (m.getReturnType().getName().contains("AvatarManager") && m.getParameterCount() == 0) {
                    m.setAccessible(true);
                    manager = m.invoke(null);
                    break;
                }
            }

            // Если через методы не нашли, ищем по полям
            if (manager == null) {
                for (Field f : figuraModClass.getDeclaredFields()) {
                    if (f.getType().getName().contains("AvatarManager")) {
                        f.setAccessible(true);
                        manager = f.get(null);
                        break;
                    }
                }
            }

            if (manager == null) {
                LOGGER.error("Figura Offline Fix: [!] CRITICAL: AvatarManager not found in FiguraMod!");
                return;
            }

            // Ищем метод обновления аватара, который принимает PlayerEntity
            boolean methodFound = false;
            for (Method m : manager.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(player.getClass())) {
                    m.setAccessible(true);
                    m.invoke(manager, player);
                    LOGGER.info("Figura Offline Fix: Successfully updated avatar via: " + m.getName());
                    methodFound = true;
                    break;
                }
            }
            
            if (!methodFound) {
                LOGGER.warn("Figura Offline Fix: Update method not found in AvatarManager.");
            }

        } catch (Exception e) {
            LOGGER.error("Figura Offline Fix: Error during reflection: " + e.getMessage());
        }
    }

    public static void saveBackup(String uuid, NbtCompound nbt) {
        try {
            AVATAR_CACHE.put(uuid, nbt);
            File cacheFile = new File(CACHE_DIR, uuid + ".dat");
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                NbtIo.writeCompressed(nbt, fos);
            }
            LOGGER.info("Figura Offline Fix: Saved cache for " + uuid);
        } catch (Exception e) {
            LOGGER.error("Figura Offline Fix: Error saving cache: " + e.getMessage());
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
                LOGGER.error("Figura Offline Fix: Error loading cache: " + e.getMessage());
            }
        }
        return null;
    }

    public static void log(String message) {
        LOGGER.info(message);
    }
}