package dev.dispensercontrol.security;

import net.minecraft.block.Blocks;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class PermissionValidator {
    private PermissionValidator() { }

    private static boolean isOperator(ServerPlayerEntity player) {
        return player.getPermissions() instanceof LeveledPermissionPredicate leveled
                && leveled.getLevel().getLevel() >= 2;
    }

    public static boolean canEdit(ServerPlayerEntity player, BlockPos pos) {
        if (player == null || pos == null || !isOperator(player)) return false;
        if (!player.getEntityWorld().getBlockState(pos).isOf(Blocks.DISPENSER)) return false;
        return player.squaredDistanceTo(Vec3d.ofCenter(pos)) <= 64.0;
    }
}
