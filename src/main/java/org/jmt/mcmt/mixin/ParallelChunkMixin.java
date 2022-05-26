package org.jmt.mcmt.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.jmt.mcmt.GeneralConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

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
