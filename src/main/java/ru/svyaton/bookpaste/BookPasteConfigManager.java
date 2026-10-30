package ru.svyaton.bookpaste;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import net.fabricmc.loader.api.FabricLoader;

public final class BookPasteConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("bookpaste.json");
    private static final Path LEGACY_SERVER_PATH = FabricLoader.getInstance().getConfigDir().resolve("bookpaste-server.json");
    private static volatile BookPasteConfig config = new BookPasteConfig();

    private BookPasteConfigManager() {
    }

    public static void load() {
        load(false);
    }

    public static void loadForServer() {
        load(true);
    }

    private static synchronized void load(boolean strict) {
        try {
            boolean exists = Files.exists(CONFIG_PATH);
            JsonObject document = exists ? read(CONFIG_PATH) : new JsonObject();
            boolean legacyToggle = document.has("unlimitedBookPages");
            boolean renameToggle = !document.has("customBookPages") && legacyToggle;
            if (renameToggle) document.add("customBookPages", document.get("unlimitedBookPages"));
            if (document.has("customBookPages") && (!document.get("customBookPages").isJsonPrimitive()
                    || !document.getAsJsonPrimitive("customBookPages").isBoolean())) {
                throw new IllegalArgumentException("customBookPages must be a boolean");
            }
            BookPasteConfig loaded = GSON.fromJson(document, BookPasteConfig.class);
            if (loaded == null || loaded.formatMode() == null) {
                throw new IllegalArgumentException("Invalid BookPaste config");
            }
            boolean migrate = !document.has("maxPages") && Files.exists(LEGACY_SERVER_PATH);
            JsonObject limits = migrate ? read(LEGACY_SERVER_PATH) : document;
            if (limits.has("maxPages")) {
                if (!limits.get("maxPages").isJsonPrimitive()
                        || !limits.getAsJsonPrimitive("maxPages").isNumber()) {
                    throw new IllegalArgumentException("maxPages must be a non-negative integer");
                }
                loaded.setMaxPages(limits.get("maxPages").getAsBigDecimal().intValueExact());
            }
            boolean inferToggle = !document.has("customBookPages") && limits.has("maxPages");
            if (inferToggle || renameToggle && net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.SERVER) {
                loaded.setCustomBookPages(loaded.maxPages() != 100);
            }
            if (document.has("lastCustomMaxPages")) {
                if (!document.get("lastCustomMaxPages").isJsonPrimitive()
                        || !document.getAsJsonPrimitive("lastCustomMaxPages").isNumber()) {
                    throw new IllegalArgumentException("lastCustomMaxPages must be a non-negative integer");
                }
                loaded.setLastCustomMaxPages(document.get("lastCustomMaxPages").getAsBigDecimal().intValueExact());
            }
            config = loaded;
            if (!exists || migrate || legacyToggle || inferToggle) {
                AtomicConfigFile.write(CONFIG_PATH, serialize(config));
            }
        } catch (IOException | RuntimeException exception) {
            if (strict) {
                throw new IllegalStateException("Cannot load " + CONFIG_PATH, exception);
            }
            config = new BookPasteConfig();
            java.util.logging.Logger.getLogger("BookPaste").log(java.util.logging.Level.WARNING,
                    "Could not load BookPaste config; using defaults without replacing the file", exception);
        }
    }

    private static JsonObject read(Path path) throws IOException {
        try (Reader input = Files.newBufferedReader(path, StandardCharsets.UTF_8);
                JsonReader reader = new JsonReader(input)) {
            reader.setLenient(true);
            JsonObject document = GSON.fromJson(reader, JsonObject.class);
            if (document == null) {
                throw new IllegalArgumentException("Invalid BookPaste config");
            }
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new IllegalArgumentException("Unexpected content after BookPaste config");
            }
            return document;
        }
    }

    public static void save() {
        try {
            AtomicConfigFile.write(CONFIG_PATH, serialize(config));
        } catch (IOException exception) {
            java.util.logging.Logger.getLogger("BookPaste").log(java.util.logging.Level.WARNING,
                    "Failed to save BookPaste config", exception);
        }
    }

    private static String serialize(BookPasteConfig value) {
        JsonObject document = new JsonObject();
        document.addProperty("customBookPages", value.customBookPages());
        document.addProperty("maxPages", value.maxPages());
        document.addProperty("lastCustomMaxPages", value.lastCustomMaxPages());
        document.addProperty("formatMode", value.formatMode().name());
        return "# Server: only customBookPages and maxPages are used. Other fields are client-only.\n"
                + GSON.toJson(document)
                .replace("  \"customBookPages\":", "  # Enable a custom page limit. false = 100 pages.\n  \"customBookPages\":")
                .replace("  \"maxPages\":", "  # Maximum pages per book. 0 = unlimited.\n  \"maxPages\":")
                .replace("  \"lastCustomMaxPages\":", "  # Client only: last custom limit, restored when the switch is enabled again.\n  \"lastCustomMaxPages\":")
                .replace("  \"formatMode\":", "  # Client only: SMART_FILL = smart wrapping, WHOLE_PARAGRAPHS = keep paragraphs together.\n  \"formatMode\":")
                + "\n";
    }

    public static BookPasteConfig getConfig() {
        return config;
    }
}
