package ru.svyaton.bookpaste;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if <26.3 {
import org.lwjgl.glfw.GLFW;
//?}
//? if >=26.1 {
/*import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;*/
//?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
//? if >=1.16.5 {
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.option.KeyBinding;
//?}
//? if <1.16.5 {
/*import net.minecraft.client.gui.screen.options.ControlsOptionsScreen;
import net.minecraft.client.options.KeyBinding;*/
//?}
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
//?}

public final class BookPasteHotkeys {
    //? if >=26.3 {
    /*public static final KeyMapping TOGGLE_FORMAT_MODE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.bookpaste.toggle_format_mode",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_F8,
            KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("bookpaste", "controls"))
    ));*/
    //?}
    //? if >=26.1 && <26.3 {
    /*public static final KeyMapping TOGGLE_FORMAT_MODE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.bookpaste.toggle_format_mode",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F8,
            KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("bookpaste", "controls"))
    ));*/
    //?}
    //? if >=1.21.10 {
    //? if <26.1 {
    public static final KeyBinding TOGGLE_FORMAT_MODE = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.bookpaste.toggle_format_mode",
                    InputUtil.Type.KEYSYM,
                    InputUtil.GLFW_KEY_F8,
                    KeyBinding.Category.create(Identifier.of("bookpaste", "controls"))
            )
    );
    //?}
    //?}
    //? if >=1.17 {
    //? if <1.21.10 {
    /*public static final KeyBinding TOGGLE_FORMAT_MODE = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.bookpaste.toggle_format_mode",
                    InputUtil.Type.KEYSYM,
                    InputUtil.GLFW_KEY_F8,
                    "category.bookpaste.controls"
            )
    );*/
    //?}
    //?}
    //? if <1.17 {
    /*public static final KeyBinding TOGGLE_FORMAT_MODE = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.bookpaste.toggle_format_mode",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_F8,
                    "category.bookpaste.controls"
            )
    );*/
    //?}

    private BookPasteHotkeys() {
    }

    public static void initialize() {
        //? if >=26.2 {
        /*ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (shouldIgnoreHotkey(client.gui.screen())) {
                while (TOGGLE_FORMAT_MODE.consumeClick()) {
                }
                return;
            }

            while (TOGGLE_FORMAT_MODE.consumeClick()) {
                cycleFormatMode(client);
            }
        });*/
        //?}
        //? if >=26.1 {
        //? if <26.2 {
        /*ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (shouldIgnoreHotkey(client.screen)) {
                while (TOGGLE_FORMAT_MODE.consumeClick()) {
                }
                return;
            }

            while (TOGGLE_FORMAT_MODE.consumeClick()) {
                cycleFormatMode(client);
            }
        });*/
        //?}
        //?}
        //? if <26.1 {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (shouldIgnoreHotkey(client.currentScreen)) {
                while (TOGGLE_FORMAT_MODE.wasPressed()) {
                }
                return;
            }

            while (TOGGLE_FORMAT_MODE.wasPressed()) {
                cycleFormatMode(client);
            }
        });
        //?}
    }

    //? if >=26.1 {
    /*public static void cycleFormatMode(Minecraft client) {*/
    //?} else {
    public static void cycleFormatMode(MinecraftClient client) {
    //?}
        PasteFormatMode nextMode = BookPasteConfigManager.getConfig().formatMode().next();
        BookPasteConfigManager.getConfig().setFormatMode(nextMode);
        BookPasteConfigManager.save();
        BookPasteMessages.showModeChanged(client, nextMode);
    }

    //? if >=26.1 {
    /*public static void setToggleFormatModeKey(Minecraft client, InputConstants.Key key) {
        TOGGLE_FORMAT_MODE.setKey(key);
        KeyMapping.resetMapping();
        client.options.save();
    }*/
    //?} else {
    public static void setToggleFormatModeKey(MinecraftClient client, InputUtil.Key key) {
        TOGGLE_FORMAT_MODE.setBoundKey(key);
        KeyBinding.updateKeysByCode();
        client.options.write();
    }
    //?}

    //? if >=26.1 {
    /*public static void resetToggleFormatModeKey(Minecraft client) {
        setToggleFormatModeKey(client, TOGGLE_FORMAT_MODE.getDefaultKey());
    }*/
    //?} else {
    public static void resetToggleFormatModeKey(MinecraftClient client) {
        setToggleFormatModeKey(client, TOGGLE_FORMAT_MODE.getDefaultKey());
    }
    //?}

    //? if >=26.1 {
    /*public static KeyBindsScreen createControlsScreen(Minecraft client, Screen parent) {
        return new KeyBindsScreen(parent, client.options);
    }*/
    //?} else {
    public static ControlsOptionsScreen createControlsScreen(MinecraftClient client, Screen parent) {
        return new ControlsOptionsScreen(parent, client.options);
    }
    //?}

    //? if >=26.1 {
    /*private static boolean shouldIgnoreHotkey(Screen screen) {
        return screen instanceof BookPasteConfigScreen || screen instanceof KeyBindsScreen;
    }*/
    //?} else {
    private static boolean shouldIgnoreHotkey(Screen screen) {
        return screen instanceof BookPasteConfigScreen || screen instanceof ControlsOptionsScreen;
    }
    //?}
}
