package ru.svyaton.bookpaste;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BookLimitQuery(int protocol) implements CustomPacketPayload {
    public static final Type<BookLimitQuery> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("bookpaste", "query_book_limits_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BookLimitQuery> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BookLimitQuery::protocol, BookLimitQuery::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
