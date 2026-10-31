package ru.svyaton.bookpaste;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BookLimitPayload(int protocol, int maxPages) implements CustomPacketPayload {
    public static final int PROTOCOL = 1;
    public static final Type<BookLimitPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("bookpaste", "book_limits_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BookLimitPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BookLimitPayload::protocol,
            ByteBufCodecs.VAR_INT, BookLimitPayload::maxPages, BookLimitPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
