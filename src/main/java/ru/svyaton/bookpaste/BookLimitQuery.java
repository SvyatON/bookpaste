package ru.svyaton.bookpaste;

//? if >=26.1 {
/*import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BookLimitQuery(int protocol) implements CustomPacketPayload {
    public static final Type<BookLimitQuery> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bookpaste", "query_book_limits_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BookLimitQuery> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BookLimitQuery::protocol, BookLimitQuery::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}*/
//?}
//? if >=1.21 && <26.1 {
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BookLimitQuery(int protocol) implements CustomPayload {
    public static final Id<BookLimitQuery> TYPE = new Id<>(Identifier.of("bookpaste", "query_book_limits_v1"));
    public static final PacketCodec<RegistryByteBuf, BookLimitQuery> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, BookLimitQuery::protocol, BookLimitQuery::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
//?}
//? if >=1.20.5 && <1.21 {
/*import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BookLimitQuery(int protocol) implements CustomPayload {
    public static final Id<BookLimitQuery> TYPE = new Id<>(new Identifier("bookpaste", "query_book_limits_v1"));
    public static final PacketCodec<RegistryByteBuf, BookLimitQuery> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, BookLimitQuery::protocol, BookLimitQuery::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}*/
//?}
