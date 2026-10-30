package ru.svyaton.bookpaste;
//? if >=26.1 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;*/
//?}
//? if >=1.19 {
//? if <26.1 {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
//?}
//?}
//? if <1.19 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;*/
//?}

public final class BookPasteMessages {
    private BookPasteMessages() {
    }

    //? if >=26.1 {
    /*public static void show(Minecraft client, Component message) {
        if (client == null || client.player == null) {
            return;
        }
        client.player.sendOverlayMessage(message);
    }*/
    //?} else {
    public static void show(MinecraftClient client, Text message) {
        if (client == null || client.player == null) {
            return;
        }
        client.player.sendMessage(message, true);
    }
    //?}
    //? if >=26.1 {
    /*public static void showModeChanged(Minecraft client, PasteFormatMode mode) {
        if (client.player == null) {
            return;
        }
        show(client, Component.literal(I18n.get(
                "text.bookpaste.mode.changed",
                I18n.get("text.bookpaste.config.mode." + mode.id())
        )));
    }*/
    //?}
    //? if >=1.19 {
    //? if <26.1 {
    public static void showModeChanged(MinecraftClient client, PasteFormatMode mode) {
        if (client.player == null) {
            return;
        }

        show(client, Text.literal(I18n.translate(
                "text.bookpaste.mode.changed",
                I18n.translate("text.bookpaste.config.mode." + mode.id())
        )));
    }
    //?}
    //?}
    //? if <1.19 {
    /*public static void showModeChanged(MinecraftClient client, PasteFormatMode mode) {
        if (client.player == null) {
            return;
        }

        show(client, new LiteralText(I18n.translate(
                "text.bookpaste.mode.changed",
                I18n.translate("text.bookpaste.config.mode." + mode.id())
        )));
    }*/
    //?}
}
