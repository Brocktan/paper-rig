package dev.dispensercontrol.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record LoadDispenserPayload(BlockPos pos) implements CustomPayload {
    public static final Id<LoadDispenserPayload> ID = CustomPayload.id("dispenser_control:load_dispenser");
    public static final PacketCodec<RegistryByteBuf, LoadDispenserPayload> CODEC =
            PacketCodec.tuple(BlockPos.PACKET_CODEC, LoadDispenserPayload::pos, LoadDispenserPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
