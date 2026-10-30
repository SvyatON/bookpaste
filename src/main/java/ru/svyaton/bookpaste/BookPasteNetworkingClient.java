package ru.svyaton.bookpaste;

//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class BookPasteNetworkingClient {
    private BookPasteNetworkingClient() {
    }

    public static void initialize() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> ServerBookCapabilities.reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ServerBookCapabilities.reset());
        ClientPlayNetworking.registerGlobalReceiver(BookLimitPayload.TYPE, (payload, context) -> {
            Object connection = context.player().connection;
            if (context.client().getConnection() == connection) {
                ServerBookCapabilities.accept(connection, payload.protocol(), payload.maxPages());
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (ClientPlayNetworking.canSend(BookLimitQuery.TYPE)) {
                ClientPlayNetworking.send(new BookLimitQuery(BookLimitPayload.PROTOCOL));
            }
        });
    }
}*/
//?}
//? if >=1.20.5 && <26.1 {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class BookPasteNetworkingClient {
    private BookPasteNetworkingClient() {
    }

    public static void initialize() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> ServerBookCapabilities.reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ServerBookCapabilities.reset());
        ClientPlayNetworking.registerGlobalReceiver(BookLimitPayload.TYPE, (payload, context) -> {
            Object connection = context.player().networkHandler;
            if (context.client().getNetworkHandler() == connection) {
                ServerBookCapabilities.accept(connection, payload.protocol(), payload.maxPages());
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (ClientPlayNetworking.canSend(BookLimitQuery.TYPE)) {
                ClientPlayNetworking.send(new BookLimitQuery(BookLimitPayload.PROTOCOL));
            }
        });
    }
}
//?}
//? if >=1.16.5 && <1.20.5 {
/*import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public final class BookPasteNetworkingClient {
    private BookPasteNetworkingClient() {
    }

    public static void initialize() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> ServerBookCapabilities.reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ServerBookCapabilities.reset());
        ClientPlayNetworking.registerGlobalReceiver(BookPaste.LIMITS, (client, handler, buffer, sender) -> {
            int protocol = buffer.readVarInt();
            int pages = buffer.readVarInt();
            client.execute(() -> {
                if (client.getNetworkHandler() == handler) ServerBookCapabilities.accept(handler, protocol, pages);
            });
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (ClientPlayNetworking.canSend(BookPaste.QUERY)) {
                PacketByteBuf buffer = PacketByteBufs.create();
                buffer.writeVarInt(1);
                ClientPlayNetworking.send(BookPaste.QUERY, buffer);
            }
        });
    }
}*/
//?}
//? if <1.16.5 {
/*import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

public final class BookPasteNetworkingClient {
    private static Object connection;
    private static boolean queried;

    private BookPasteNetworkingClient() {
    }

    public static void initialize() {
        ClientSidePacketRegistry.INSTANCE.register(BookPaste.LIMITS, (context, buffer) -> {
            int protocol = buffer.readVarInt();
            int pages = buffer.readVarInt();
            MinecraftClient client = MinecraftClient.getInstance();
            Object source = ((net.minecraft.client.network.ClientPlayerEntity) context.getPlayer()).networkHandler;
            context.getTaskQueue().execute(() -> {
                if (client.getNetworkHandler() == source) ServerBookCapabilities.accept(source, protocol, pages);
            });
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Object current = client.getNetworkHandler();
            if (current != connection) {
                connection = current;
                queried = false;
                ServerBookCapabilities.reset();
            }
            if (current != null && !queried && ClientSidePacketRegistry.INSTANCE.canServerReceive(BookPaste.QUERY)) {
                queried = true;
                PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
                buffer.writeVarInt(1);
                ClientSidePacketRegistry.INSTANCE.sendToServer(BookPaste.QUERY, buffer);
            }
        });
    }
}*/
//?}
