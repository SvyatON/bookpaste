package ru.svyaton.bookpaste;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;

public final class BookPasteNetworkingClient {
    private static Object connection;
    private static boolean queried;

    private BookPasteNetworkingClient() {}

    public static void receive(BookLimitPayload payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        Object source = ((net.minecraft.client.player.LocalPlayer) context.player()).connection;
        if (client.getConnection() == source) ServerBookCapabilities.accept(source, payload.protocol(), payload.maxPages());
    }

    public static void tick(Minecraft client) {
        Object current = client.getConnection();
        if (current != connection) {
            connection = current;
            queried = false;
            ServerBookCapabilities.reset();
        }
        if (!queried && client.getConnection() != null && client.getConnection().hasChannel(BookLimitQuery.TYPE)) {
            queried = true;
            PacketDistributor.sendToServer(new BookLimitQuery(1));
        }
    }
}
