package dev.dispensercontrol.config;

/** Immutable, validated settings for one dispenser. */
public record DispenserConfig(
        String name,
        String displayedItem,
        String actualItem,
        int quantity,
        boolean enabled,
        String notes,
        boolean playSound,
        boolean showParticles
) {
    public static final DispenserConfig DEFAULT = new DispenserConfig(
            "Default", "minecraft:diamond", "minecraft:cobblestone", 1, false, "", true, true
    );

    public DispenserConfig validated() {
        String safeName = name == null || name.isBlank() ? "Unnamed" : name.strip().substring(0, Math.min(48, name.strip().length()));
        String display = ConfigValidator.normaliseItemId(displayedItem);
        String actual = ConfigValidator.normaliseItemId(actualItem);
        String safeNotes = notes == null ? "" : notes.substring(0, Math.min(256, notes.length()));
        return new DispenserConfig(safeName, display, actual, Math.max(1, Math.min(64, quantity)), enabled, safeNotes, playSound, showParticles);
    }
}
