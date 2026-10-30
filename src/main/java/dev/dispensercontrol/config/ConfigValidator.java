package dev.dispensercontrol.config;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Validation shared by the network boundary and persistent store. */
public final class ConfigValidator {
    private static final Pattern ITEM_ID = Pattern.compile("^[a-z0-9_.-]+:[a-z0-9/._-]+$");

    private static final Set<String> SAFE_EXACT = Set.of(
            "diamond", "cobblestone", "stone", "dirt", "grass_block", "sand", "gravel", "glass",
            "iron_ingot", "gold_ingot", "emerald", "lapis_lazuli", "redstone", "coal", "copper_ingot",
            "amethyst_shard", "quartz", "obsidian", "glowstone", "clay_ball", "brick", "paper", "book",
            "bread", "apple", "carrot", "potato", "baked_potato", "torch", "lantern", "chest", "barrel",
            "crafting_table", "furnace", "bucket", "water_bucket", "milk_bucket", "compass", "clock",
            "string", "feather", "leather", "bone_meal", "sugar", "wheat", "beetroot", "melon_slice",
            "pumpkin", "carved_pumpkin", "sea_lantern", "prismarine_shard", "prismarine_crystals"
    );
    private static final Set<String> SAFE_SUFFIXES = Set.of(
            "_planks", "_log", "_wood", "_wool", "_concrete", "_terracotta", "_glass", "_pane",
            "_slab", "_stairs", "_fence", "_fence_gate", "_door", "_trapdoor", "_button",
            "_pressure_plate", "_sign", "_hanging_sign", "_carpet", "_bed", "_banner", "_candle",
            "_leaves", "_sapling", "_wall", "_bricks", "_tiles", "_ore", "_block", "_coral",
            "_coral_block", "_coral_fan"
    );
    private static final Set<String> BLOCKED_PATHS = Set.of(
            "tnt", "tnt_minecart", "command_block", "chain_command_block", "repeating_command_block",
            "structure_block", "jigsaw", "barrier", "bedrock", "spawner", "end_portal_frame"
    );

    private ConfigValidator() { }

    public static String normaliseItemId(String value) {
        if (value == null) return "minecraft:air";
        String id = value.strip().toLowerCase(Locale.ROOT);
        if (!ITEM_ID.matcher(id).matches()) return "minecraft:air";
        return id;
    }

    /** Only approved vanilla building, decorative, food, and material items are selectable. */
    public static boolean isValidItemId(String value) {
        if (value == null || !ITEM_ID.matcher(value).matches() || !value.startsWith("minecraft:")) return false;
        String path = value.substring("minecraft:".length());
        if (BLOCKED_PATHS.contains(path)) return false;
        if (SAFE_EXACT.contains(path)) return true;
        return SAFE_SUFFIXES.stream().anyMatch(path::endsWith);
    }

    public static boolean isValidQuantity(int quantity) {
        return quantity >= 1 && quantity <= 64;
    }
}
