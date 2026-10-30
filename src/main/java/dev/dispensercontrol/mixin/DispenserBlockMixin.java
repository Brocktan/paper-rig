package dev.dispensercontrol.mixin;

import dev.dispensercontrol.DispenserControl;
import dev.dispensercontrol.config.ConfigStore;
import dev.dispensercontrol.config.ConfigValidator;
import dev.dispensercontrol.config.DispenserConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin {
    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    private static void dispenserControl$customOutput(ServerWorld world, BlockState state, BlockPos pos, CallbackInfo ci) {
        DispenserConfig config = ConfigStore.get(DispenserControl.configKey(world.getRegistryKey().getValue().toString(), pos));
        if (config == null || !config.enabled()) return;
        if (!ConfigValidator.isValidItemId(config.actualItem())) return;

        Identifier itemId = Identifier.tryParse(config.actualItem());
        if (itemId == null) return;
        Item item = Registries.ITEM.getOrEmpty(itemId).orElse(net.minecraft.item.Items.AIR);
        if (item == net.minecraft.item.Items.AIR) return;

        ci.cancel();
        if (!(world.getBlockEntity(pos) instanceof DispenserBlockEntity dispenser)) return;

        int sourceSlot = -1;
        for (int slot = 0; slot < dispenser.size(); slot++) {
            if (!dispenser.getStack(slot).isEmpty()) {
                sourceSlot = slot;
                break;
            }
        }
        if (sourceSlot < 0) {
            if (config.playSound()) world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return;
        }

        dispenser.removeStack(sourceSlot, 1);
        Direction facing = state.get(DispenserBlock.FACING);
        double x = pos.getX() + 0.5 + facing.getOffsetX() * 0.7;
        double y = pos.getY() + 0.5 + facing.getOffsetY() * 0.7;
        double z = pos.getZ() + 0.5 + facing.getOffsetZ() * 0.7;

        int remaining = Math.max(1, Math.min(64, config.quantity()));
        while (remaining > 0) {
            int count = Math.min(remaining, item.getMaxCount());
            ItemEntity output = new ItemEntity(world, x, y, z, new ItemStack(item, count));
            output.setPickupDelay(10);
            world.spawnEntity(output);
            remaining -= count;
        }

        if (config.playSound()) world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (config.showParticles()) world.spawnParticles(net.minecraft.particle.ParticleTypes.POOF,
                x, y, z, 3, 0.08, 0.08, 0.08, 0.01);
        world.updateComparators(pos, state.getBlock());
    }
}
