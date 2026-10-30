package dev.dispensercontrol.network;

import dev.dispensercontrol.config.DispenserConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SaveDispenserPayload(BlockPos pos, String name, String displayedItem,
                                   String actualItem, int quantity, boolean enabled, String notes,
                                   boolean playSound, boolean showParticles)
        implements CustomPayload {
    public static final Id<SaveDispenserPayload> ID = CustomPayload.id("dispenser_control:save_dispenser");
    public static final PacketCodec<RegistryByteBuf, SaveDispenserPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SaveDispenserPayload::pos,
            PacketCodecs.STRING, SaveDispenserPayload::name,
            PacketCodecs.STRING, SaveDispenserPayload::displayedItem,
            PacketCodecs.STRING, SaveDispenserPayload::actualItem,
            PacketCodecs.VAR_INT, SaveDispenserPayload::quantity,
            PacketCodecs.BOOL, SaveDispenserPayload::enabled,
            PacketCodecs.STRING, SaveDispenserPayload::notes,
            PacketCodecs.BOOL, SaveDispenserPayload::playSound,
            PacketCodecs.BOOL, SaveDispenserPayload::showParticles,
            SaveDispenserPayload::new
    );

    @Override public Id<? extends CustomPayload> getId() { return ID; }

    public DispenserConfig toConfig() {
        return new DispenserConfig(name, displayedItem, actualItem, quantity, enabled, notes, playSound, showParticles);
    }
}
