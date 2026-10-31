package ru.svyaton.bookpaste;

public final class BookPasteBookLimits {
    public static final int MAX_PAGE_COUNT = 100;
    public static final int MAX_PAGE_LENGTH = 1024;

    public static int pageLimit() {

        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        if (client != null) {
            if (!client.isLocalServer() && client.getConnection() != null) {
                return ServerBookCapabilities.pageLimit(client.getConnection());
            }
            int supported = client.isLocalServer() ? ServerBookSettings.pageLimit() : MAX_PAGE_COUNT;
            return BookPasteConfigManager.getConfig().customBookPages()
                    ? supported : Math.min(MAX_PAGE_COUNT, supported);
        }

        return MAX_PAGE_COUNT;
    }

    private BookPasteBookLimits() {
    }
}
