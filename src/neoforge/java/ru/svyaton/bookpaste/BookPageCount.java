package ru.svyaton.bookpaste;

import net.minecraft.network.protocol.game.ServerboundEditBookPacket;

public final class BookPageCount {
    private BookPageCount() {
    }

    public static int count(ServerboundEditBookPacket packet) {
        return packet.pages().size();
    }
}
