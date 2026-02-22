package com.figura.offlinefix.mixin;

import org.figuramc.figura.FiguraMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = FiguraMod.class, remap = false)
public class FiguraAuthMixin {
    @Overwrite
    public boolean isAuthenticated() {
        return true;
    }

    @Overwrite
    public static boolean isOnline() {
        return true;
    }
}