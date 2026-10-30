package dev.dispensercontrol.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Server-owned persistent settings. All mutations are made on the server thread. */
public final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<String, DispenserConfig> CONFIGS = new ConcurrentHashMap<>();
    private static Path file;

    private ConfigStore() { }

    public static void load(MinecraftServer server) {
        CONFIGS.clear();
        file = server.getSavePath(WorldSavePath.ROOT).resolve("data").resolve("dispenser_control.json");
        if (!Files.exists(file)) return;
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            StoredData data = GSON.fromJson(json, StoredData.class);
            if (data != null && data.schemaVersion != 1) throw new IllegalStateException("Unsupported configuration schema");
            if (data != null && data.configurations != null) {
                data.configurations.forEach((key, value) -> {
                    if (key != null && value != null) {
                        DispenserConfig checked = value.validated();
                        if (ConfigValidator.isValidItemId(checked.displayedItem())
                                && ConfigValidator.isValidItemId(checked.actualItem())) CONFIGS.put(key, checked);
                    }
                });
            }
        } catch (IOException | RuntimeException exception) {
            Path broken = file.resolveSibling("dispenser_control.corrupt-" + System.currentTimeMillis() + ".json");
            try { Files.move(file, broken); } catch (IOException ignored) { }
            CONFIGS.clear();
        }
    }

    public static DispenserConfig get(String key) {
        return CONFIGS.get(key);
    }

    public static void put(String key, DispenserConfig config) {
        CONFIGS.put(key, config.validated());
    }

    public static void remove(String key) {
        CONFIGS.remove(key);
    }

    public static void save() {
        if (file == null) return;
        try {
            Files.createDirectories(file.getParent());
            StoredData data = new StoredData();
            data.configurations = new LinkedHashMap<>(CONFIGS);
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(data), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicNotSupported) {
                Files.move(temporary, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save Dispenser Control settings", exception);
        }
    }

    public static Path getFile() { return file; }

    private static final class StoredData {
        private int schemaVersion = 1;
        private Map<String, DispenserConfig> configurations = new LinkedHashMap<>();
    }
}
