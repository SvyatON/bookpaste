package ru.svyaton.bookpaste.mixin;

//? if >=26.1 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.svyaton.bookpaste.BookPasteBookLimits;
import ru.svyaton.bookpaste.BookPasteMessages;

@Mixin(ClientCommonPacketListenerImpl.class)
abstract class ClientBookLimitMixin {
    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void bookpaste$guardBookLimit(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof ServerboundEditBookPacket book)) return;
        int limit = BookPasteBookLimits.pageLimit();
        if (book.pages().size() <= limit) return;
        ci.cancel();
        BookPasteMessages.show(Minecraft.getInstance(),
                Component.translatable("text.bookpaste.limit.changed", limit));
    }
}*/
//?}
//? if >=1.20.2 && <26.1 {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.text.Text;
//? if >=1.19.4 {
import net.minecraft.network.packet.Packet;
//?} else {
/*import net.minecraft.network.Packet;*/
//?}
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.svyaton.bookpaste.BookPasteBookLimits;
import ru.svyaton.bookpaste.BookPasteMessages;

@Mixin(ClientCommonNetworkHandler.class)
abstract class ClientBookLimitMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void bookpaste$guardBookLimit(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof BookUpdateC2SPacket book)) return;
        int limit = BookPasteBookLimits.pageLimit();
        if (ru.svyaton.bookpaste.BookPageCount.count(book) <= limit) return;
        ci.cancel();
        BookPasteMessages.show(MinecraftClient.getInstance(),
                Text.translatable("text.bookpaste.limit.changed", limit));
    }
}
//?}
//? if >=1.19 && <1.20.2 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
//? if >=1.19.4 {
import net.minecraft.network.packet.Packet;
//?} else {
import net.minecraft.network.Packet;
//?}
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.svyaton.bookpaste.BookPasteBookLimits;
import ru.svyaton.bookpaste.BookPasteMessages;

@Mixin(ClientPlayNetworkHandler.class)
abstract class ClientBookLimitMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void bookpaste$guardBookLimit(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof BookUpdateC2SPacket)) return;
        BookUpdateC2SPacket book = (BookUpdateC2SPacket) packet;
        int limit = BookPasteBookLimits.pageLimit();
        if (ru.svyaton.bookpaste.BookPageCount.count(book) <= limit) return;
        ci.cancel();
        BookPasteMessages.show(MinecraftClient.getInstance(),
                Text.translatable("text.bookpaste.limit.changed", limit));
    }
}*/
//?}
//? if <1.19 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.svyaton.bookpaste.BookPasteBookLimits;
import ru.svyaton.bookpaste.BookPasteMessages;

@Mixin(ClientPlayNetworkHandler.class)
abstract class ClientBookLimitMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void bookpaste$guardBookLimit(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof BookUpdateC2SPacket)) return;
        BookUpdateC2SPacket book = (BookUpdateC2SPacket) packet;
        int limit = BookPasteBookLimits.pageLimit();
        if (ru.svyaton.bookpaste.BookPageCount.count(book) <= limit) return;
        ci.cancel();
        BookPasteMessages.show(MinecraftClient.getInstance(),
                new net.minecraft.text.TranslatableText("text.bookpaste.limit.changed", limit));
    }
}*/
//?}
