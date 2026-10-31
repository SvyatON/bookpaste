package ru.svyaton.bookpaste.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.component.WritableBookContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WritableBookContent.class)
abstract class UnlimitedWritableBookMixin {
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;sizeLimitedListOf(I)Lcom/mojang/serialization/Codec;"), index = 0, require = 1, allow = 1)
    private static int bookpaste$diskPageLimit(int original) {
        return Integer.MAX_VALUE;
    }

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/codec/ByteBufCodecs;list(I)Lnet/minecraft/network/codec/StreamCodec$CodecOperation;"), index = 0, require = 1, allow = 1)
    private static int bookpaste$networkPageLimit(int original) {
        return Integer.MAX_VALUE;
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0), require = 1, allow = 1)
    private int bookpaste$contentPageLimit(int size) {
        return Math.min(size, 100);
    }
}
