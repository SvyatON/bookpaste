package ru.svyaton.bookpaste.mixin;

import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.svyaton.bookpaste.ServerBookSettings;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerBookLimitMixin {

    @Shadow
    public ServerPlayer player;

    @ModifyConstant(method = "handleEditBook", constant = @Constant(longValue = 100L), require = 1, allow = 1)
    private long bookpaste$filterPageLimit(long original) {
        return ServerBookSettings.pageLimit();
    }

    @Inject(method = "handleEditBook", at = @At("HEAD"), cancellable = true)
    private void bookpaste$enforceLimit(ServerboundEditBookPacket packet, CallbackInfo ci) {
        int limit = ServerBookSettings.pageLimit();
        if (ru.svyaton.bookpaste.BookPageCount.count(packet) > limit
                || ru.svyaton.bookpaste.BookPageCount.count(packet) > 100 && !ru.svyaton.bookpaste.BookPaste.canReceive(this.player)) {
            ci.cancel();
        }
    }
}
