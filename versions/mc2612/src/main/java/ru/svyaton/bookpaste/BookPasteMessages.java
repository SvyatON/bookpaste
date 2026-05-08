package ru.svyaton.bookpaste;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class BookPasteMessages {
    private BookPasteMessages() {
    }

    public static void showModeChanged(Minecraft client, PasteFormatMode mode) {
        if (client.player == null) {
            return;
        }

        client.player.sendOverlayMessage(Component.literal(I18n.get(
                "text.bookpaste.mode.changed",
                I18n.get("text.bookpaste.config.mode." + mode.id())
        )));
    }
}
