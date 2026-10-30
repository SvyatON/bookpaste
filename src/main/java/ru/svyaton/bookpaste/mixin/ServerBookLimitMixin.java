package ru.svyaton.bookpaste.mixin;

//? if >=26.1 {
/*import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.svyaton.bookpaste.ServerBookSettings;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerBookLimitMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleEditBook", at = @At("HEAD"), cancellable = true)
    private void bookpaste$enforceLimit(ServerboundEditBookPacket packet, CallbackInfo ci) {
        int limit = ServerBookSettings.pageLimit();
        if (packet.pages().size() > limit
                || packet.pages().size() > 100 && !ru.svyaton.bookpaste.BookPaste.canReceive(this.player)) {
            ci.cancel();
        }
    }
}*/
//?}
//? if <26.1 {
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//? if >=1.17 && <1.21 {
/*import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;*/
//?}
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.svyaton.bookpaste.ServerBookSettings;

@Mixin(ServerPlayNetworkHandler.class)
abstract class ServerBookLimitMixin {
    //? if >=1.17 && <1.21 {
    /*@ModifyConstant(method = "onBookUpdate", constant = @Constant(longValue = 100L), require = 1, allow = 1)
    private long bookpaste$filterPageLimit(long original) {
        return ServerBookSettings.pageLimit();
    }*/
    //?}

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onBookUpdate", at = @At("HEAD"), cancellable = true)
    private void bookpaste$enforceLimit(BookUpdateC2SPacket packet, CallbackInfo ci) {
        int limit = ServerBookSettings.pageLimit();
        if (ru.svyaton.bookpaste.BookPageCount.count(packet) > limit
                || ru.svyaton.bookpaste.BookPageCount.count(packet) > 100 && !ru.svyaton.bookpaste.BookPaste.canReceive(this.player)) {
            ci.cancel();
        }
    }
}
//?}
