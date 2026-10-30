package dev.dispensercontrol.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ConfigStatusPayload(String message, boolean accepted) implements CustomPayload {
    public static final Id<ConfigStatusPayload> ID = CustomPayload.id("dispenser_control:config_status");
    public static final PacketCodec<RegistryByteBuf, ConfigStatusPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ConfigStatusPayload::message,
            PacketCodecs.BOOLEAN, ConfigStatusPayload::accepted,
            ConfigStatusPayload::new
    );
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
