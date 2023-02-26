package org.boosted.mixin.getServer;

import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.boosted.ThreadCoordinator;
import org.boosted.util.BoostedTeleportation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Inject(method = "onEntityCollision", cancellable = true,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;",
        shift = At.Shift.BEFORE))
    public void synchronizeOnEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        RegistryKey<World> registryKey = world.getRegistryKey() == World.END ? World.OVERWORLD : World.END;
        ((ServerWorld)world).getSynchronizedServer().write(server -> {
            ServerWorld serverWorld = server.getWorld(registryKey);
            if (serverWorld == null) {
                return;
            }
            BoostedTeleportation.teleportEntity(entity, serverWorld); // foreign serverWorld reference is contained
        });
        ci.cancel();
    }
}
