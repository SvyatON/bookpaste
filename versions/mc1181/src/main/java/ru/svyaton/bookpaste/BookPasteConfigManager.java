package ru.svyaton.bookpaste;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public final class BookPasteConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("bookpaste.json");
    private static BookPasteConfig config = new BookPasteConfig();

    private BookPasteConfigManager() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            BookPasteConfig loaded = GSON.fromJson(reader, BookPasteConfig.class);
            if (loaded != null && loaded.formatMode() != null) {
                config = loaded;
            } else {
                config = new BookPasteConfig();
                save();
            }
        } catch (IOException | RuntimeException exception) {
            config = new BookPasteConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save BookPaste config", exception);
        }
    }

    public static BookPasteConfig getConfig() {
        return config;
    }
}
