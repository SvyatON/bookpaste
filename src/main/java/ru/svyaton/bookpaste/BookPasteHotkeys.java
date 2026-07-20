package ru.svyaton.bookpaste;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class BookPasteHotkeys {
    public static final KeyBinding TOGGLE_FORMAT_MODE = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.bookpaste.toggle_format_mode",
                    InputUtil.Type.KEYSYM,
                    InputUtil.GLFW_KEY_F8,
                    "key.category.bookpaste.controls"
            )
    );

    private BookPasteHotkeys() {
    }

    public static void initialize() {
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
    }

    public static void cycleFormatMode(MinecraftClient client) {
        PasteFormatMode nextMode = BookPasteConfigManager.getConfig().formatMode().next();
        BookPasteConfigManager.getConfig().setFormatMode(nextMode);
        BookPasteConfigManager.save();
        BookPasteMessages.showModeChanged(client, nextMode);
    }

    public static void setToggleFormatModeKey(MinecraftClient client, InputUtil.Key key) {
        TOGGLE_FORMAT_MODE.setBoundKey(key);
        KeyBinding.updateKeysByCode();
        client.options.write();
    }

    public static void resetToggleFormatModeKey(MinecraftClient client) {
        setToggleFormatModeKey(client, TOGGLE_FORMAT_MODE.getDefaultKey());
    }

    private static boolean shouldIgnoreHotkey(Screen screen) {
        return false;
    }
}
