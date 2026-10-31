package ru.svyaton.bookpaste.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import ru.svyaton.bookpaste.BookPacketPages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerboundEditBookPacket.class)
abstract class UnlimitedBookPacketMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/codec/ByteBufCodecs;list(I)Lnet/minecraft/network/codec/StreamCodec$CodecOperation;"), require = 1, allow = 1)
    private static StreamCodec.CodecOperation<ByteBuf, String, List<String>> bookpaste$packetPageLimit(
            StreamCodec.CodecOperation<ByteBuf, String, List<String>> original) {
        return BookPacketPages.codec();
    }
}
