package com.figura.offlinefix.mixin;

import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FiguraMod.class, remap = false)
public interface AvatarManagerAccessor {
    @Accessor("avatarManager")
    static AvatarManager getAvatarManager() {
        throw new UnsupportedOperationException();
    }
}