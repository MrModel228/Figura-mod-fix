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
            // Пробуем напрямую получить AvatarManager через его класс
            Class<?> avatarManagerClass = Class.forName("org.figuramc.figura.avatar.AvatarManager");
            
            LOGGER.info("Figura Offline Fix: AvatarManager class loaded. Available methods:");
            // Перебираем все методы AvatarManager для поиска метода обновления
            for (Method m : avatarManagerClass.getDeclaredMethods()) {
                LOGGER.info("  - " + m.getName() + " (params: " + m.getParameterCount() + ", return: " + m.getReturnType().getSimpleName() + ")");
            }
            
            // Пробуем получить синглтон AvatarManager
            Object managerInstance = null;
            for (Method sm : avatarManagerClass.getDeclaredMethods()) {
                if (sm.getParameterCount() == 0 && sm.getReturnType().equals(avatarManagerClass)) {
                    sm.setAccessible(true);
                    managerInstance = sm.invoke(null);
                    LOGGER.info("Figura Offline Fix: Got AvatarManager instance via " + sm.getName());
                    break;
                }
            }
            
            if (managerInstance == null) {
                LOGGER.warn("Figura Offline Fix: Could not get AvatarManager instance");
            }
            
            // Пробуем вызвать loadLocalAvatar для загрузки из локального кэша
            try {
                Method loadLocalAvatar = avatarManagerClass.getDeclaredMethod("loadLocalAvatar", java.util.UUID.class);
                loadLocalAvatar.setAccessible(true);
                loadLocalAvatar.invoke(managerInstance, player.getUuid());
                LOGGER.info("Figura Offline Fix: Loaded local avatar for " + player.getName().getString() + " via loadLocalAvatar");
                return;
            } catch (NoSuchMethodException e) {
                LOGGER.info("Figura Offline Fix: loadLocalAvatar not found");
            }
            
            // Пробуем вызвать reloadAvatar с UUID
            try {
                Method reloadAvatar = avatarManagerClass.getDeclaredMethod("reloadAvatar", java.util.UUID.class);
                reloadAvatar.setAccessible(true);
                reloadAvatar.invoke(managerInstance, player.getUuid());
                LOGGER.info("Figura Offline Fix: Updated avatar for " + player.getName().getString() + " via reloadAvatar(UUID)");
                return;
            } catch (NoSuchMethodException e) {
                LOGGER.info("Figura Offline Fix: reloadAvatar(UUID) not found");
            }
            
            // Пробуем вызвать reloadAvatar с Entity
            try {
                Method reloadAvatar = avatarManagerClass.getDeclaredMethod("reloadAvatar", net.minecraft.entity.Entity.class);
                reloadAvatar.setAccessible(true);
                reloadAvatar.invoke(managerInstance, player);
                LOGGER.info("Figura Offline Fix: Updated avatar for " + player.getName().getString() + " via reloadAvatar(Entity)");
                return;
            } catch (NoSuchMethodException e) {
                LOGGER.info("Figura Offline Fix: reloadAvatar(Entity) not found");
            }
            
            // Пробуем вызвать loadEntityAvatar
            try {
                Method loadEntityAvatar = avatarManagerClass.getDeclaredMethod("loadEntityAvatar", net.minecraft.entity.Entity.class, boolean.class);
                loadEntityAvatar.setAccessible(true);
                loadEntityAvatar.invoke(managerInstance, player, true);
                LOGGER.info("Figura Offline Fix: Updated avatar for " + player.getName().getString() + " via loadEntityAvatar");
                return;
            } catch (NoSuchMethodException e) {
                LOGGER.info("Figura Offline Fix: loadEntityAvatar not found");
            }
            
            LOGGER.warn("Figura Offline Fix: No suitable method found to update avatar");
        } catch (Exception e) {
            LOGGER.error("Figura Offline Fix: Error updating avatar: " + e.getMessage());
            e.printStackTrace();
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