package ru.svyaton.bookpaste;

//? if >=26.1 {
/*import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class BookPaste implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerBookSettings.load();
        PayloadTypeRegistry.clientboundPlay().register(BookLimitPayload.TYPE, BookLimitPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(BookLimitQuery.TYPE, BookLimitQuery.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BookLimitQuery.TYPE, (payload, context) -> {
            if (payload.protocol() == BookLimitPayload.PROTOCOL) advertise(context.player());
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> advertise(handler.player));
    }

    public static void refreshLimits(net.minecraft.server.MinecraftServer server) {
        if (server != null) {
            server.execute(() -> server.getPlayerList().getPlayers().forEach(BookPaste::advertise));
        }
    }

    public static boolean canReceive(ServerPlayer player) {
        return ServerPlayNetworking.canSend(player, BookLimitPayload.TYPE);
    }

    private static void advertise(ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, BookLimitPayload.TYPE)) {
            ServerPlayNetworking.send(player, new BookLimitPayload(BookLimitPayload.PROTOCOL, ServerBookSettings.pageLimit()));
        }
    }
}*/
//?}
//? if >=1.20.5 && <26.1 {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class BookPaste implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerBookSettings.load();
        PayloadTypeRegistry.playS2C().register(BookLimitPayload.TYPE, BookLimitPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BookLimitQuery.TYPE, BookLimitQuery.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BookLimitQuery.TYPE, (payload, context) -> {
            if (payload.protocol() == BookLimitPayload.PROTOCOL) advertise(context.player());
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> advertise(handler.player));
    }

    public static void refreshLimits(net.minecraft.server.MinecraftServer server) {
        if (server != null) {
            server.execute(() -> server.getPlayerManager().getPlayerList().forEach(BookPaste::advertise));
        }
    }

    public static boolean canReceive(ServerPlayerEntity player) {
        return ServerPlayNetworking.canSend(player, BookLimitPayload.TYPE);
    }

    private static void advertise(ServerPlayerEntity player) {
        if (ServerPlayNetworking.canSend(player, BookLimitPayload.TYPE)) {
            ServerPlayNetworking.send(player, new BookLimitPayload(BookLimitPayload.PROTOCOL, ServerBookSettings.pageLimit()));
        }
    }
}
//?}
//? if >=1.16.5 && <1.20.5 {
/*import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public final class BookPaste implements ModInitializer {
    public static final Identifier LIMITS = new Identifier("bookpaste", "book_limits_v1");
    public static final Identifier QUERY = new Identifier("bookpaste", "query_book_limits_v1");

    @Override
    public void onInitialize() {
        ServerBookSettings.load();
        ServerPlayNetworking.registerGlobalReceiver(QUERY, (server, player, handler, buffer, sender) -> {
            int protocol = buffer.readVarInt();
            if (protocol == 1) server.execute(() -> advertise(player));
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> advertise(handler.player));
    }

    public static void refreshLimits(net.minecraft.server.MinecraftServer server) {
        if (server != null) server.execute(() -> server.getPlayerManager().getPlayerList().forEach(BookPaste::advertise));
    }

    public static boolean canReceive(ServerPlayerEntity player) {
        return ServerPlayNetworking.canSend(player, LIMITS);
    }

    private static void advertise(ServerPlayerEntity player) {
        if (canReceive(player)) {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeVarInt(1);
            buffer.writeVarInt(ServerBookSettings.pageLimit());
            ServerPlayNetworking.send(player, LIMITS, buffer);
        }
    }
}*/
//?}
//? if <1.16.5 {
/*import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public final class BookPaste implements ModInitializer {
    public static final Identifier LIMITS = new Identifier("bookpaste", "book_limits_v1");
    public static final Identifier QUERY = new Identifier("bookpaste", "query_book_limits_v1");

    @Override
    public void onInitialize() {
        ServerBookSettings.load();
        ServerSidePacketRegistry.INSTANCE.register(QUERY, (context, buffer) -> {
            int protocol = buffer.readVarInt();
            if (protocol == 1) context.getTaskQueue().execute(() -> advertise((ServerPlayerEntity) context.getPlayer()));
        });
    }

    public static void refreshLimits(net.minecraft.server.MinecraftServer server) {
        if (server != null) server.execute(() -> server.getPlayerManager().getPlayerList().forEach(BookPaste::advertise));
    }

    public static boolean canReceive(ServerPlayerEntity player) {
        return ServerSidePacketRegistry.INSTANCE.canPlayerReceive(player, LIMITS);
    }

    private static void advertise(ServerPlayerEntity player) {
        if (canReceive(player)) {
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            buffer.writeVarInt(1);
            buffer.writeVarInt(ServerBookSettings.pageLimit());
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, LIMITS, buffer);
        }
    }
}*/
//?}
