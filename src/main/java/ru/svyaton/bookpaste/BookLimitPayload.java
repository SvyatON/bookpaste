package ru.svyaton.bookpaste;

//? if >=26.1 {
/*import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BookLimitPayload(int protocol, int maxPages) implements CustomPacketPayload {
    public static final int PROTOCOL = 1;
    public static final Type<BookLimitPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bookpaste", "book_limits_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BookLimitPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BookLimitPayload::protocol,
            ByteBufCodecs.VAR_INT, BookLimitPayload::maxPages, BookLimitPayload::new);

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

public record BookLimitPayload(int protocol, int maxPages) implements CustomPayload {
    public static final int PROTOCOL = 1;
    public static final Id<BookLimitPayload> TYPE = new Id<>(Identifier.of("bookpaste", "book_limits_v1"));
    public static final PacketCodec<RegistryByteBuf, BookLimitPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, BookLimitPayload::protocol,
            PacketCodecs.VAR_INT, BookLimitPayload::maxPages, BookLimitPayload::new);

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

public record BookLimitPayload(int protocol, int maxPages) implements CustomPayload {
    public static final int PROTOCOL = 1;
    public static final Id<BookLimitPayload> TYPE = new Id<>(new Identifier("bookpaste", "book_limits_v1"));
    public static final PacketCodec<RegistryByteBuf, BookLimitPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, BookLimitPayload::protocol,
            PacketCodecs.VAR_INT, BookLimitPayload::maxPages, BookLimitPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}*/
//?}
