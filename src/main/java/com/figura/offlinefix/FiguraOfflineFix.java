package com.figura.offlinefix;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FiguraOfflineFix implements ClientModInitializer {
    public static final String MOD_ID = "figura-offline-fix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static final Map<String, NbtCompound> AVATAR_CACHE = new HashMap<>();
    private static final File CACHE_DIR = new File("figura_offline_cache");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Figura Offline Fix: Initializing...");

        // Создаем папку для кэша
        if (!CACHE_DIR.exists()) {
            CACHE_DIR.mkdirs();
        }

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                updateAvatarManual(client.player);
            }
        });
    }

    public static void updateAvatarManual(PlayerEntity player) {
        try {
            // Используем рефлексию для получения AvatarManager
            Class<?> figuraModClass = Class.forName("org.figuramc.figura.FiguraMod");
            Object manager = figuraModClass.getMethod("getAvatarManager").invoke(null);
            
            // Перебираем методы, чтобы найти тот, который принимает игрока
            for (Method m : manager.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(PlayerEntity.class)) {
                    m.setAccessible(true);
                    m.invoke(manager, player);
                    LOGGER.info("Figura Offline Fix: Updated avatar for " + player.getName().getString());
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Figura Offline Fix: Error updating avatar: " + e.getMessage());
        }
    }

    public static void log(String message) {
        LOGGER.info(message);
    }

    public static void saveBackup(String uuid, NbtCompound nbt) {
        try {
            AVATAR_CACHE.put(uuid, nbt);
            
            File cacheFile = new File(CACHE_DIR, uuid + ".dat.gz");
            try (FileOutputStream fos = new FileOutputStream(cacheFile);
                 GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
                byte[] data = nbt.toString().getBytes();
                gzos.write(data);
            }
            LOGGER.info("Figura Offline Fix: Cached avatar for " + uuid);
        } catch (IOException e) {
            LOGGER.error("Figura Offline Fix: Error caching avatar: " + e.getMessage());
        }
    }

    public static NbtCompound loadBackup(String uuid) {
        NbtCompound cached = AVATAR_CACHE.get(uuid);
        if (cached != null) {
            return cached;
        }

        File cacheFile = new File(CACHE_DIR, uuid + ".dat.gz");
        if (cacheFile.exists()) {
            try (FileInputStream fis = new FileInputStream(cacheFile);
                 GZIPInputStream gzis = new GZIPInputStream(fis)) {
                byte[] data = gzis.readAllBytes();
                NbtCompound nbt = new NbtCompound();
                // Простая реализация - в реальном коде нужно парсить NBT
                return nbt;
            } catch (IOException e) {
                LOGGER.error("Figura Offline Fix: Error loading cached avatar: " + e.getMessage());
            }
        }
        return null;
    }
}