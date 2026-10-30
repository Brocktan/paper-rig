package dev.dispensercontrol.client;

import dev.dispensercontrol.network.ConfigStatusPayload;
import dev.dispensercontrol.network.DispenserStatePayload;
import dev.dispensercontrol.network.LoadDispenserPayload;
import dev.dispensercontrol.network.SaveDispenserPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;

public final class DispenserControlClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigStatusPayload.ID, (payload, context) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof DispenserControlScreen screen) {
                screen.setServerStatus(payload.message());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(DispenserStatePayload.ID, (payload, context) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof DispenserControlScreen screen) {
                screen.applyServerState(payload);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient || !world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.DISPENSER)) {
                return ActionResult.PASS;
            }
            var screen = new DispenserControlScreen(hitResult.getBlockPos());
            MinecraftClient.getInstance().setScreen(screen);
            if (ClientPlayNetworking.canSend(LoadDispenserPayload.ID)) {
                ClientPlayNetworking.send(new LoadDispenserPayload(hitResult.getBlockPos()));
            } else {
                screen.setServerStatus("Server mod required: local preview only; gameplay changes unavailable.");
            }
            return ActionResult.SUCCESS;
        });
    }
}
