package com.figura.offlinefix.mixin;

import org.figuramc.figura.utils.UserUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = UserUtils.class, remap = false)
public class UserUtilsMixin {
    @Overwrite
    public static boolean isProfileVerified() {
        return true;
    }

    @Overwrite
    public static boolean isOnlineMode() {
        return true;
    }

    @Overwrite
    public static int getBackendStatus() {
        return 1;
    }
}