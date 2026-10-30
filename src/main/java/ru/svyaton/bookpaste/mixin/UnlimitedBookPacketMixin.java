package ru.svyaton.bookpaste.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import ru.svyaton.bookpaste.BookPacketPages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;

@Mixin(ServerboundEditBookPacket.class)
abstract class UnlimitedBookPacketMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/codec/ByteBufCodecs;list(I)Lnet/minecraft/network/codec/StreamCodec$CodecOperation;"), require = 1, allow = 1)
    private static StreamCodec.CodecOperation<ByteBuf, String, List<String>> bookpaste$packetPageLimit(
            StreamCodec.CodecOperation<ByteBuf, String, List<String>> original) {
        return BookPacketPages.codec();
    }
}*/
//?}
//? if >=1.20.5 && <26.1 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.PacketCodec;
import ru.svyaton.bookpaste.BookPacketPages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;

@Mixin(BookUpdateC2SPacket.class)
abstract class UnlimitedBookPacketMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/codec/PacketCodecs;toList(I)Lnet/minecraft/network/codec/PacketCodec$ResultFunction;"), require = 1, allow = 1)
    private static PacketCodec.ResultFunction<ByteBuf, String, List<String>> bookpaste$packetPageLimit(
            PacketCodec.ResultFunction<ByteBuf, String, List<String>> original) {
        return BookPacketPages.codec();
    }
}
//?}
//? if >=1.17 && <1.20.5 {
/*import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import io.netty.handler.codec.DecoderException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.svyaton.bookpaste.ServerBookSettings;

@Mixin(BookUpdateC2SPacket.class)
abstract class UnlimitedBookPacketMixin {
    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("HEAD"))
    private static void bookpaste$validateCount(PacketByteBuf buffer, CallbackInfo ci) {
        int position = buffer.readerIndex();
        buffer.readVarInt();
        int count = buffer.readVarInt();
        if (count < 0 || count > ServerBookSettings.pageLimit() || count > buffer.readableBytes()) {
            throw new DecoderException("Invalid book page count: " + count);
        }
        buffer.readerIndex(position);
    }

    @ModifyConstant(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", constant = @Constant(intValue = 200), require = 1, allow = 1)
    private int bookpaste$packetPageLimit(int original) {
        return ServerBookSettings.pageLimit();
    }
}*/
//?}
