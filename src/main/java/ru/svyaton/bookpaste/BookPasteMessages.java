package ru.svyaton.bookpaste;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

public final class BookPasteMessages {
    private BookPasteMessages() {
    }

    public static void showModeChanged(MinecraftClient client, PasteFormatMode mode) {
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.literal(I18n.translate(
                "text.bookpaste.mode.changed",
                I18n.translate("text.bookpaste.config.mode." + mode.id())
        )), true);
    }
}
