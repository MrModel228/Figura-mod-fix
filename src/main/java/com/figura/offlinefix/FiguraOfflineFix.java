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
        LOGGER.info("Figura Offline Fix: Initializing 1.2.8 (1.21.8 Fixed)...");

        if (!CACHE_DIR.exists()) CACHE_DIR.mkdirs();

        // Пытаемся найти и патчить класс авторизации через reflection
        patchAuthClasses();

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

    private static void patchAuthClasses() {
        // Пытаемся найти классы авторизации в разных пакетах
        String[] possibleClasses = new String[] {
            "org.figuramc.figura.backend.Backend",
            "org.figuramc.figura.backend.AuthManager",
            "org.figuramc.figura.auth.AuthManager",
            "org.figuramc.figura.auth.Authentication",
            "org.figuramc.figura.AuthManager",
            "org.figuramc.figura.Authentication",
            "org.figuramc.figura.backend.network.AuthManager",
            "org.figuramc.figura.backend.network.Authentication"
        };
        
        for (String className : possibleClasses) {
            try {
                Class<?> clazz = Class.forName(className);
                LOGGER.info("Figura Offline Fix: Found auth class: " + className);
                
                // Логируем все методы
                for (Method m : clazz.getDeclaredMethods()) {
                    LOGGER.info("  " + className + " method: " + m.getName() + " (params: " + m.getParameterCount() + ")");
                }
                
                // Логируем все поля
                for (Field f : clazz.getDeclaredFields()) {
                    LOGGER.info("  " + className + " field: " + f.getName() + " (type: " + f.getType().getSimpleName() + ")");
                }
            } catch (ClassNotFoundException e) {
                // Игнорируем, класс не найден
            }
        }
        
        // Пытаемся найти FiguraMod и логировать его методы авторизации
        try {
            Class<?> figuraModClass = Class.forName("org.figuramc.figura.FiguraMod");
            LOGGER.info("Figura Offline Fix: Found FiguraMod class");
            
            // Ищем методы, связанные с авторизацией
            for (Method m : figuraModClass.getDeclaredMethods()) {
                String methodName = m.getName().toLowerCase();
                if (methodName.contains("auth") || methodName.contains("verify") || 
                    methodName.contains("valid") || methodName.contains("license") ||
                    methodName.contains("online") || methodName.contains("check")) {
                    LOGGER.info("  FiguraMod auth-related method: " + m.getName() + 
                               " (params: " + m.getParameterCount() + ", return: " + m.getReturnType().getSimpleName() + ")");
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Figura Offline Fix: FiguraMod class not found");
        }
    }

    public static void updateAvatarManual(PlayerEntity player) {
        try {
            // Прямой доступ к классу AvatarManager
            Class<?> avatarManagerClass = Class.forName("org.figuramc.figura.avatar.AvatarManager");
            
            // Логируем все методы AvatarManager для отладки
            LOGGER.info("Figura Offline Fix: AvatarManager methods:");
            for (Method m : avatarManagerClass.getDeclaredMethods()) {
                LOGGER.info("  - " + m.getName() + " (params: " + m.getParameterCount() + ")");
            }
            
            // Ищем метод для обновления аватара
            boolean methodFound = false;
            
            // Пробуем разные варианты методов
            String[] methodNames = {"reloadAvatar", "loadAvatar", "loadEntityAvatar", "updateAvatar", "refreshAvatar"};
            
            for (String methodName : methodNames) {
                try {
                    Method method = null;
                    
                    // Пробуем найти метод по имени
                    for (Method m : avatarManagerClass.getDeclaredMethods()) {
                        if (m.getName().equals(methodName)) {
                            method = m;
                            break;
                        }
                    }
                    
                    if (method != null) {
                        method.setAccessible(true);
                        
                        // Пробуем вызвать с разными параметрами
                        if (method.getParameterCount() == 1) {
                            Class<?> paramType = method.getParameterTypes()[0];
                            if (paramType.isAssignableFrom(player.getClass())) {
                                // Статический метод с PlayerEntity
                                method.invoke(null, player);
                                LOGGER.info("Figura Offline Fix: Successfully updated avatar via static " + methodName + "(PlayerEntity)");
                                methodFound = true;
                                break;
                            } else if (paramType.getName().equals("java.util.UUID")) {
                                // Статический метод с UUID
                                method.invoke(null, player.getUuid());
                                LOGGER.info("Figura Offline Fix: Successfully updated avatar via static " + methodName + "(UUID)");
                                methodFound = true;
                                break;
                            }
                        } else if (method.getParameterCount() == 0) {
                            // Статический метод без параметров
                            method.invoke(null);
                            LOGGER.info("Figura Offline Fix: Successfully updated avatar via static " + methodName + "()");
                            methodFound = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Figura Offline Fix: Failed to call " + methodName + ": " + e.getMessage());
                }
            }
            
            if (!methodFound) {
                LOGGER.warn("Figura Offline Fix: No suitable update method found in AvatarManager.");
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