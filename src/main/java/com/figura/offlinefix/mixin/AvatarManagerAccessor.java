package com.figura.offlinefix.mixin;

import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AvatarManager.class)
public interface AvatarManagerAccessor {
    // Оставляем пустым. Рефлексия в FiguraOfflineFix сделает всю работу.
}