package ru.svyaton.bookpaste;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class BookPasteClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BookPasteConfigManager.load();
        BookPasteHotkeys.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(ScribbleCompat::tick);
    }
}
