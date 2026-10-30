package dev.dispensercontrol.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.dispensercontrol.config.DispenserConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Local preset library. Presets are templates; the server still validates every applied setting. */
public final class ClientPresetStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("dispenser-control-presets.json");
    private static final Map<String, DispenserConfig> PRESETS = new LinkedHashMap<>();
    private static boolean loaded;

    private ClientPresetStore() { }

    public static synchronized Map<String, DispenserConfig> all() {
        load();
        return Map.copyOf(PRESETS);
    }

    public static synchronized void put(String name, DispenserConfig config) {
        load();
        PRESETS.put(cleanName(name), config.validated());
        save();
    }

    public static synchronized void remove(String name) {
        load();
        PRESETS.remove(name);
        save();
    }

    public static synchronized void rename(String oldName, String newName) {
        load();
        DispenserConfig config = PRESETS.remove(oldName);
        if (config != null) PRESETS.put(cleanName(newName), config);
        save();
    }

    public static synchronized void duplicate(String source, String target) {
        load();
        DispenserConfig config = PRESETS.get(source);
        if (config != null) PRESETS.put(cleanName(target), config);
        save();
    }

    public static synchronized void restoreDefaults() {
        load();
        PRESETS.clear();
        PRESETS.put("Default", DispenserConfig.DEFAULT);
        save();
    }

    private static String cleanName(String name) {
        if (name == null || name.isBlank()) return "Preset";
        String clean = name.strip();
        return clean.substring(0, Math.min(40, clean.length()));
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(FILE)) {
            PRESETS.put("Default", DispenserConfig.DEFAULT);
            return;
        }
        try {
            PresetData data = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), PresetData.class);
            if (data != null && data.schemaVersion != 1) throw new IllegalStateException("Unsupported preset schema");
            if (data != null && data.presets != null) {
                data.presets.forEach((name, config) -> {
                    if (name != null && config != null) PRESETS.put(cleanName(name), config.validated());
                });
            }
            if (PRESETS.isEmpty()) PRESETS.put("Default", DispenserConfig.DEFAULT);
        } catch (IOException | RuntimeException exception) {
            PRESETS.clear();
            PRESETS.put("Default", DispenserConfig.DEFAULT);
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            PresetData data = new PresetData();
            data.presets = new LinkedHashMap<>(PRESETS);
            Files.writeString(FILE, GSON.toJson(data), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save local Dispenser Control presets", exception);
        }
    }

    private static final class PresetData {
        private int schemaVersion = 1;
        private Map<String, DispenserConfig> presets = new LinkedHashMap<>();
    }
}
