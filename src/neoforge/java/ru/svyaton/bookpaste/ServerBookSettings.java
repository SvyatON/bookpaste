package ru.svyaton.bookpaste;

import net.neoforged.api.distmarker.Dist;

public final class ServerBookSettings {
    private static volatile boolean loaded;

    private ServerBookSettings() {
    }

    public static synchronized void load() {
        if (net.neoforged.fml.loading.FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            BookPasteConfigManager.loadForServer();
        } else {
            BookPasteConfigManager.load();
        }
        loaded = true;
    }

    public static int pageLimit() {
        if (!loaded) load();
        if (!BookPasteConfigManager.getConfig().customBookPages()) return 100;
        int value = BookPasteConfigManager.getConfig().maxPages();
        return value == 0 ? Integer.MAX_VALUE : value;
    }
}
