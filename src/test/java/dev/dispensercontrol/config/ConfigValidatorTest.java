package dev.dispensercontrol.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigValidatorTest {
    @Test void normalisesIdentifiers() {
        assertEquals("minecraft:diamond", ConfigValidator.normaliseItemId(" Minecraft:Diamond "));
    }

    @Test void invalidIdentifiersFallBackToAir() {
        assertEquals("minecraft:air", ConfigValidator.normaliseItemId("not an item"));
        assertEquals("minecraft:air", ConfigValidator.normaliseItemId(null));
    }

    @Test void quantitiesAreBounded() {
        assertTrue(ConfigValidator.isValidQuantity(1));
        assertTrue(ConfigValidator.isValidQuantity(64));
        assertFalse(ConfigValidator.isValidQuantity(0));
        assertFalse(ConfigValidator.isValidQuantity(65));
    }

    @Test void unsupportedCategoriesAreRejected() {
        assertFalse(ConfigValidator.isValidItemId("minecraft:diamond_sword"));
        assertFalse(ConfigValidator.isValidItemId("minecraft:tnt"));
        assertFalse(ConfigValidator.isValidItemId("othermod:custom_item"));
        assertTrue(ConfigValidator.isValidItemId("minecraft:diamond"));
        assertTrue(ConfigValidator.isValidItemId("minecraft:cobblestone"));
        assertTrue(ConfigValidator.isValidItemId("minecraft:oak_planks"));
    }

    @Test void configValidationClampsValues() {
        DispenserConfig config = new DispenserConfig("", "minecraft:diamond", "minecraft:cobblestone", 999, true, "", true, true).validated();
        assertEquals("Unnamed", config.name());
        assertEquals(64, config.quantity());
    }
}
