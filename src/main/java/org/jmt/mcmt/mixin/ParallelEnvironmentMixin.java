package org.jmt.mcmt.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
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
public class ParallelEnvironmentMixin {

	private static Phaser p;
	private static ExecutorService ex;

	// Statistics
	private static AtomicInteger currentEnvs = new AtomicInteger();

	//Operation logging
	private static Set<String> currentTasks = ConcurrentHashMap.newKeySet();

	/**
	 * Redirect the environment simulation (plant growth etc) for each tick onto an execution sheduler
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickChunk(Lnet/minecraft/world/chunk/WorldChunk;I)V"),
			method = "tickChunks()V")
	private void redirectTickChunk(WorldChunk chunk, int randomTickSpeed) {
		ServerWorld world = (ServerWorld)chunk.getWorld();
		if (GeneralConfig.disabled  || GeneralConfig.disableEnvironment) {
			world.tickChunk(chunk, randomTickSpeed);
			return;
		}
		String taskName = null;
		if (GeneralConfig.opsTracing) {
			taskName = "EnvTick: " + chunk.toString() + "@" + chunk.hashCode();
			currentTasks.add(taskName);
		}
		String finalTaskName = taskName;
		p.register();
		ex.execute(() -> {
			try {
				currentEnvs.incrementAndGet();
				world.tickChunk(chunk, randomTickSpeed);
			} finally {
				currentEnvs.decrementAndGet();
				p.arriveAndDeregister();
				if (GeneralConfig.opsTracing) currentTasks.remove(finalTaskName);
			}
		});
	}
}
