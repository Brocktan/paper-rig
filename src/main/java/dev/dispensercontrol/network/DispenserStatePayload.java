package dev.dispensercontrol.network;

import dev.dispensercontrol.config.DispenserConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record DispenserStatePayload(BlockPos pos, String name, String displayedItem,
                                    String actualItem, int quantity, boolean enabled,
                                    String notes, boolean playSound, boolean showParticles, boolean configured) implements CustomPayload {
    public static final Id<DispenserStatePayload> ID = CustomPayload.id("dispenser_control:dispenser_state");
    public static final PacketCodec<RegistryByteBuf, DispenserStatePayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, DispenserStatePayload::pos,
            PacketCodecs.STRING, DispenserStatePayload::name,
            PacketCodecs.STRING, DispenserStatePayload::displayedItem,
            PacketCodecs.STRING, DispenserStatePayload::actualItem,
            PacketCodecs.VAR_INT, DispenserStatePayload::quantity,
            PacketCodecs.BOOLEAN, DispenserStatePayload::enabled,
            PacketCodecs.STRING, DispenserStatePayload::notes,
            PacketCodecs.BOOLEAN, DispenserStatePayload::playSound,
            PacketCodecs.BOOLEAN, DispenserStatePayload::showParticles,
            PacketCodecs.BOOLEAN, DispenserStatePayload::configured,
            DispenserStatePayload::new
    );
    @Override public Id<? extends CustomPayload> getId() { return ID; }

    public static DispenserStatePayload from(BlockPos pos, DispenserConfig config, boolean configured) {
        return new DispenserStatePayload(pos, config.name(), config.displayedItem(), config.actualItem(),
                config.quantity(), config.enabled(), config.notes(), config.playSound(), config.showParticles(), configured);
    }
}
