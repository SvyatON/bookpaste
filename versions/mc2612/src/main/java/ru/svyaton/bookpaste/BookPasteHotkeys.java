package ru.svyaton.bookpaste;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.resources.Identifier;

public final class BookPasteHotkeys {
    public static final KeyMapping TOGGLE_FORMAT_MODE = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.bookpaste.toggle_format_mode",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_F8,
                    KeyMapping.Category.register(Identifier.fromNamespaceAndPath("bookpaste", "controls"))
            )
    );

    private BookPasteHotkeys() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (shouldIgnoreHotkey(client.screen)) {
                while (TOGGLE_FORMAT_MODE.consumeClick()) {
                }
                return;
            }

            while (TOGGLE_FORMAT_MODE.consumeClick()) {
                cycleFormatMode(client);
            }
        });
    }

    public static void cycleFormatMode(Minecraft client) {
        PasteFormatMode nextMode = BookPasteConfigManager.getConfig().formatMode().next();
        BookPasteConfigManager.getConfig().setFormatMode(nextMode);
        BookPasteConfigManager.save();
        BookPasteMessages.showModeChanged(client, nextMode);
    }

    public static void setToggleFormatModeKey(Minecraft client, InputConstants.Key key) {
        TOGGLE_FORMAT_MODE.setKey(key);
        KeyMapping.resetMapping();
        client.options.save();
    }

    public static void resetToggleFormatModeKey(Minecraft client) {
        setToggleFormatModeKey(client, TOGGLE_FORMAT_MODE.getDefaultKey());
    }

    public static ControlsScreen createControlsScreen(Minecraft client, Screen parent) {
        return new ControlsScreen(parent, client.options);
    }

    private static boolean shouldIgnoreHotkey(Screen screen) {
        return screen instanceof BookPasteConfigScreen || screen instanceof ControlsScreen;
    }
}
