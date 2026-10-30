package ru.svyaton.bookpaste;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class ServerBookSettings {
    private static volatile boolean loaded;

    private ServerBookSettings() {
    }

    public static synchronized void load() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
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
