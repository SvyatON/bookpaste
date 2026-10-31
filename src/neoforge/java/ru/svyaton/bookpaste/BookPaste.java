package ru.svyaton.bookpaste;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod("bookpaste")
public final class BookPaste {
    public BookPaste(IEventBus bus) {
        ServerBookSettings.load();
        bus.addListener(BookPaste::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1").optional();
        registrar.playToClient(BookLimitPayload.TYPE, BookLimitPayload.CODEC, (payload, context) ->
                BookPasteNetworkingClient.receive(payload, context));
        registrar.playToServer(BookLimitQuery.TYPE, BookLimitQuery.CODEC, (payload, context) -> {
            if (payload.protocol() == BookLimitPayload.PROTOCOL && context.player() instanceof ServerPlayer player) advertise(player);
        });
    }

    public static void refreshLimits(net.minecraft.server.MinecraftServer server) {
        if (server != null) server.execute(() -> server.getPlayerList().getPlayers().forEach(BookPaste::advertise));
    }

    public static boolean canReceive(ServerPlayer player) {
        return player.connection.hasChannel(BookLimitPayload.TYPE);
    }

    private static void advertise(ServerPlayer player) {
        if (canReceive(player)) PacketDistributor.sendToPlayer(player, new BookLimitPayload(1, ServerBookSettings.pageLimit()));
    }
}
