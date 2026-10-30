package dev.dispensercontrol;

import dev.dispensercontrol.config.ConfigStore;
import dev.dispensercontrol.config.ConfigValidator;
import dev.dispensercontrol.network.ConfigStatusPayload;
import dev.dispensercontrol.network.DispenserStatePayload;
import dev.dispensercontrol.network.LoadDispenserPayload;
import dev.dispensercontrol.network.SaveDispenserPayload;
import dev.dispensercontrol.security.PermissionValidator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DispenserControl implements ModInitializer {
    public static final String MOD_ID = "dispenser_control";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(SaveDispenserPayload.ID, SaveDispenserPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(LoadDispenserPayload.ID, LoadDispenserPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ConfigStatusPayload.ID, ConfigStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DispenserStatePayload.ID, DispenserStatePayload.CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(ConfigStore::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ConfigStore.save());

        ServerPlayNetworking.registerGlobalReceiver(LoadDispenserPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            BlockPos pos = payload.pos();
            if (!PermissionValidator.canEdit(player, pos)) {
                ServerPlayNetworking.send(player, new ConfigStatusPayload(
                        "Read-only: operator permission and proximity to a dispenser are required.", false));
                return;
            }
            var current = ConfigStore.get(configKey(player.getWorld().getRegistryKey().getValue().toString(), pos));
            boolean configured = current != null;
            if (current == null) current = dev.dispensercontrol.config.DispenserConfig.DEFAULT;
            ServerPlayNetworking.send(player, DispenserStatePayload.from(pos, current, configured));
        });

        ServerPlayNetworking.registerGlobalReceiver(SaveDispenserPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            BlockPos pos = payload.pos();
            if (!PermissionValidator.canEdit(player, pos)) {
                ServerPlayNetworking.send(player, new ConfigStatusPayload(
                        "Rejected: operator permission and proximity to a dispenser are required.", false));
                return;
            }

            if (!isBounded(payload.name(), 48) || !isBounded(payload.notes(), 256)
                    || !isBounded(payload.displayedItem(), 128) || !isBounded(payload.actualItem(), 128)
                    || !ConfigValidator.isValidQuantity(payload.quantity())) {
                ServerPlayNetworking.send(player, new ConfigStatusPayload("Rejected: invalid configuration values.", false));
                return;
            }

            String displayed = ConfigValidator.normaliseItemId(payload.displayedItem());
            String actual = ConfigValidator.normaliseItemId(payload.actualItem());
            if (!ConfigValidator.isValidItemId(displayed) || !ConfigValidator.isValidItemId(actual)
                    || !itemExists(displayed) || !itemExists(actual)) {
                ServerPlayNetworking.send(player, new ConfigStatusPayload(
                        "Rejected: item identifier is unknown or not supported.", false));
                return;
            }

            var config = payload.toConfig().validated();
            String key = configKey(player.getWorld().getRegistryKey().getValue().toString(), pos);
            var previous = ConfigStore.get(key);
            ConfigStore.put(key, config);
            try {
                ConfigStore.save();
                ServerPlayNetworking.send(player, new ConfigStatusPayload("Accepted and saved by the server.", true));
            } catch (RuntimeException exception) {
                if (previous == null) ConfigStore.remove(key);
                else ConfigStore.put(key, previous);
                LOGGER.error("Could not persist dispenser configuration at {}", pos, exception);
                ServerPlayNetworking.send(player, new ConfigStatusPayload("Rejected: server could not save the configuration.", false));
            }
        });

        LOGGER.info("Dispenser Control initialised for Minecraft 1.21.11");
    }

    public static String configKey(String dimension, BlockPos pos) {
        return dimension + "|" + pos.asLong();
    }

    private static boolean itemExists(String value) {
        Identifier id = Identifier.tryParse(value);
        var item = id == null ? java.util.Optional.<net.minecraft.item.Item>empty() : Registries.ITEM.getOrEmpty(id);
        return item.isPresent() && item.get() != net.minecraft.item.Items.AIR;
    }

    private static boolean isBounded(String value, int max) {
        return value != null && value.length() <= max;
    }
}
