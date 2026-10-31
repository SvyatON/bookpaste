package ru.svyaton.bookpaste;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class BookPacketPages {
    private BookPacketPages() {
    }

    public static <B extends ByteBuf> StreamCodec.CodecOperation<B, String, List<String>> codec() {
        return element -> new StreamCodec<B, List<String>>() {
            @Override
            public List<String> decode(B buffer) {
                int count = ByteBufCodecs.readCount(buffer, ServerBookSettings.pageLimit());
                if (count < 0 || count > buffer.readableBytes()) {
                    throw new DecoderException("Invalid book page count: " + count);
                }
                List<String> pages = new ArrayList<>(Math.min(count, 256));
                for (int i = 0; i < count; i++) pages.add(element.decode(buffer));
                return pages;
            }

            @Override
            public void encode(B buffer, List<String> pages) {
                ByteBufCodecs.writeCount(buffer, pages.size(), Integer.MAX_VALUE);
                for (String page : pages) element.encode(buffer, page);
            }
        };
    }
}
