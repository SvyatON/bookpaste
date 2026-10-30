package ru.svyaton.bookpaste;

public final class BookPasteBookLimits {
    public static final int MAX_PAGE_COUNT = 100;
    public static final int MAX_PAGE_LENGTH = 1024;

    public static int pageLimit() {
//? if >=26.1 {
/*net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        if (client != null) {
            if (!client.hasSingleplayerServer() && client.getConnection() != null) {
                return ServerBookCapabilities.pageLimit(client.getConnection());
            }
            int supported = client.hasSingleplayerServer() ? ServerBookSettings.pageLimit() : MAX_PAGE_COUNT;
            return BookPasteConfigManager.getConfig().customBookPages()
                    ? supported : Math.min(MAX_PAGE_COUNT, supported);
        }*/
//?}
//? if <26.1 {
net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client != null) {
            if (!client.isInSingleplayer() && client.getNetworkHandler() != null) {
                return ServerBookCapabilities.pageLimit(client.getNetworkHandler());
            }
            int supported = client.isInSingleplayer() ? ServerBookSettings.pageLimit() : MAX_PAGE_COUNT;
            return BookPasteConfigManager.getConfig().customBookPages()
                    ? supported : Math.min(MAX_PAGE_COUNT, supported);
        }
//?}

        return MAX_PAGE_COUNT;
    }

    private BookPasteBookLimits() {
    }
}
