package org.jmt.mcmt.mixin;

import net.minecraft.server.world.ServerChunkManager;
import org.jmt.mcmt.config.GeneralConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerChunkManager.class)
public class ParallelChunkMixin {

	/**
	 * Intercept the upper bound of the for loop in getChunk. Returning 0 will skip the entire loop.
	 * The loop checks the chunk cache. Skipping the loop forces the ChunkManager to always obtain a future which allows us to parallelize chunk loading
	 */
	@ModifyConstant(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", constant = @Constant(intValue = 4))
	private int injected(int value) {
		if(!GeneralConfig.disableMultiChunk) {
			return 0; // read 0 values from the chunk cache
		}
		return value; // read value entries from the chunk cache
	}
}
