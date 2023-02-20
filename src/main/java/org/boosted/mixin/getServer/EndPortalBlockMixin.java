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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

	/**
     * @author Vorlent
     * @reason onEntityCollision needs exclusive write access to MinecraftServer
     * during portal collision to get the destination world
     */
    @Overwrite
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (world instanceof ServerWorld
            && !entity.hasVehicle()
            && !entity.hasPassengers()
            && entity.canUsePortals()
            && VoxelShapes.matchesAnywhere(
                VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())),
                state.getOutlineShape(world, pos), BooleanBiFunction.AND)) {

			RegistryKey<World> registryKey = world.getRegistryKey() == World.END ? World.OVERWORLD : World.END;
            /* PATCH BEGIN */ // TODO using inject and return should simplify this mixin
            ((ServerWorld)world).getSynchronizedServer().write(server -> {
                ServerWorld serverWorld = server.getWorld(registryKey);
                if (serverWorld == null) {
                    return;
                }
                ThreadCoordinator.getInstance().getBoostedContext().postTick().execute(() -> entity.moveToWorld(serverWorld));
                // TODO remove the teleported entities and put them in a queue that is emptied in a mid tick executor
            });
            /* PATCH END */
		}
	}
}
