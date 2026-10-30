package dev.dispensercontrol.security;

import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class PermissionValidator {
    private PermissionValidator() { }

    public static boolean canEdit(ServerPlayerEntity player, BlockPos pos) {
        if (player == null || pos == null || !player.hasPermissionLevel(2)) return false;
        if (!player.getWorld().getBlockState(pos).isOf(Blocks.DISPENSER)) return false;
        return player.squaredDistanceTo(Vec3d.ofCenter(pos)) <= 64.0;
    }
}
