# Minecraft 1.21.8 + Fabric Loader 0.18.4 Mixin Setup

## Overview
This document provides a complete setup guide for working with mixins in Minecraft 1.21.8 using Fabric Loader 0.18.4.

## Project Configuration

### build.gradle
```gradle
plugins {
    id 'fabric-loom' version '1.10.5'
    id 'maven-publish'
}

dependencies {
    minecraft "com.mojang:minecraft:1.21.8"
    mappings loom.officialMojangMappings()
    
    modImplementation "net.fabricmc:fabric-loader:0.18.4"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.136.1+1.21.8"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

loom {
    accessWidenerPath.set(file("src/main/resources/yourmod.access_widener"))
}
```

### fabric.mod.json
```json
{
  "schemaVersion": 1,
  "id": "yourmodid",
  "version": "1.0.0",
  "name": "Your Mod Name",
  "description": "Mod description",
  "authors": ["Your Name"],
  "license": "MIT",
  "environment": "client",
  "entrypoints": {
    "client": ["com.example.yourmod.YourModClass"]
  },
  "mixins": {
    "config": "yourmod.mixins.json"
  },
  "accessWidenerPath": "yourmod.access_widener",
  "depends": {
    "fabricloader": ">=0.18.4",
    "fabric-api": ">=0.136.1+1.21.8",
    "minecraft": "1.21.8"
  }
}
```

## Mixin Configuration

### yourmod.mixins.json
```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.example.yourmod.mixin",
  "compatibilityLevel": "JAVA_21",
  "client": [
    "TitleScreenMixin",
    "PauseScreenMixin",
    "WidgetMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

## Access Widener

### yourmod.access_widener
```
accessWidener v2 named

# Access modal screen stack methods
accessMethod public net/minecraft/class_447 method_25308()V # pushModal

# Access title screen
accessField public net/minecraft/class_442 field_3103 # titleScreen

# Access screen button management
accessMethod public net/minecraft/class_4268 method_30009(Ljava/util/function/Consumer;)V # addDrawableChild

# Access button widget methods
accessMethod public net/minecraft/class_4069 method_18427()V # build
accessMethod public net/minecraft/class_4184 method_25369(IIII)Lnet/minecraft/class_4184; # dimensions
accessMethod public net/minecraft/class_4184 method_30015()Lnet/minecraft/class_4184; # build

# Tooltip access
accessMethod public net/minecraft/class_4184 method_30420()Ljava/lang/CharSequence; # getTooltipText
accessMethod public net/minecraft/class_4184 method_30421(Ljava/util/function/Consumer;)Lnet/minecraft/class_4184; # tooltip
```

## UI Component Class Names (Minecraft 1.21.8)

### Main UI Classes
| Minecraft Intermediary Name | Mapped Name | Description |
|----------------------------|-------------|-------------|
| class_442 | TitleScreen | Main menu screen |
| class_447 | PauseScreen | Pause menu screen |
| class_416 | Screen | Base screen class |
| class_4069 | ButtonWidget | Button widget |
| class_4184 | ButtonWidget.Builder | Button builder |
| class_4268 | Screen | Screen base class |
| class_3860 | DrawContext | Drawing context |

### Text Classes
| Minecraft Intermediary Name | Mapped Name | Description |
|----------------------------|-------------|-------------|
| class_27 | Text | Text class |
| class_4197 | TextParser | Text parser |
| class_5460 | FormattedText | Formatted text |

## Working Mixin Examples

### TitleScreenMixin - Add Buttons to Main Menu
```java
package com.example.yourmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    public void addCustomButtons(CallbackInfo ci) {
        TitleScreen self = (TitleScreen) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        
        // Add button 100px below the default buttons
        int buttonY = self.height / 4 + 72;
        
        self.addDrawableChild(
            ButtonWidget.builder(Text.of("Custom Button"))
                .dimensions(self.width / 2 - 100, buttonY, 200, 20)
                .callback((button) -> {
                    // Your button click logic here
                    mc.submit(() -> {
                        // Execute on main thread if needed
                    });
                })
                .build()
        );
    }
}
```

### PauseScreenMixin - Add Buttons to Pause Menu
```java
package com.example.yourmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.PauseScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    public void addCustomButtons(CallbackInfo ci) {
        PauseScreen self = (PauseScreen) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        
        self.addDrawableChild(
            ButtonWidget.builder(Text.of("Custom Pause Button"))
                .dimensions(self.width / 2 - 100, self.height / 4 + 48, 200, 20)
                .callback((button) -> {
                    // Your button click logic here
                    mc.openScreen(null); // Return to game
                })
                .build()
        );
    }
}
```

### ButtonWidget Accessor - Modify Button Properties
```java
package com.example.yourmod.mixin;

import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ButtonWidget.class)
public interface ButtonWidgetAccessor {
    @Accessor("enabled")
    void setEnabled(boolean enabled);
    
    @Accessor("visible")
    void setVisible(boolean visible);
}
```

### Screen Dimensions Accessor
```java
package com.example.yourmod.mixin;

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("width")
    int getWidth();
    
    @Accessor("height")
    int getHeight();
}
```

### Text Literal Factory
```java
package com.example.yourmod.mixin;

import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Text.class)
public interface TextInvoker {
    @Invoker("literal")
    static Text literal(String content) {
        throw new AssertionError("This should not be called directly");
    }
}
```

## Custom Widget Factory
```java
package com.example.yourmod.mixin;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ButtonWidget.class)
public interface ButtonWidgetFactory {
    @Invoker("builder")
    static ButtonWidget.Builder createBuilder(Text message) {
        throw new AssertionError("This should not be called directly");
    }
}
```

## Usage in Your Mod

### In your ClientModInitializer:
```java
public class YourModClass implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Initialize your mod
        System.out.println("Mod initialized!");
    }
}
```

## Build and Run

```bash
# Build the mod
./gradlew build

# Run with client
./gradlew runClient

# Clean and rebuild
./gradlew clean build
```

## Key Points

1. **Mixin Package**: Always use a separate `mixin` subpackage
2. **Interface vs Class**: Use interfaces for accessors and classes for injections
3. **Injection Points**: Use `@At("TAIL")` for adding buttons after initialization
4. **Thread Safety**: Use `MinecraftClient.submit()` for thread-safe operations
5. **Access Widener**: Required for accessing non-public members
6. **Mojang Mappings**: Fabric 1.21.8 uses official Mojang mappings

## Common Mixin Target Classes

| Target | Purpose |
|--------|---------|
| TitleScreen | Add buttons to main menu |
| PauseScreen | Add buttons to pause menu |
| Screen | General screen modifications |
| ButtonWidget | Modify button behavior |
| DrawContext | Custom rendering |
| Text | Create text components |

## Tips

1. Use `@At("TAIL")` in the `init()` method for adding UI elements
2. For more control, use `@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;addDrawableChild(Lnet/minecraft/client/gui/widget/ClickableWidget;)V")`
3. Use the `CallbackInfo` parameter to access the screen instance via `(Screen) (Object) this`
4. Button dimensions: `x = width/2 - 100, y = height/4 + offset, width = 200, height = 20`
5. Remember to use `addDrawableChild()` instead of `add()` for clickable widgets
