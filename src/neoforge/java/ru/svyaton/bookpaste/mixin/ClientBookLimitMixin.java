package ru.svyaton.bookpaste.mixin;

import net.minecraft.client.Minecraft;
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
        if (ru.svyaton.bookpaste.BookPageCount.count(book) <= limit) return;
        ci.cancel();
        BookPasteMessages.show(Minecraft.getInstance(),
                Component.translatable("text.bookpaste.limit.changed", limit));
    }
}
