package org.boosted.mixin;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.boosted.ThreadCoordinator;
import org.boosted.config.GeneralConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkManager.class)
public class ParallelEnvironmentMixin {

	/**
	 * Redirect the environment simulation (plant growth etc) for each tick onto an execution sheduler
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickChunk(Lnet/minecraft/world/chunk/WorldChunk;I)V"),
			method = "tickChunks()V")
	private void redirectTickChunk(ServerWorld instance, WorldChunk chunk, int randomTickSpeed) {
		final ThreadCoordinator threadCoordinator = ThreadCoordinator.getInstance();

		ServerWorld world = (ServerWorld)chunk.getWorld();
		if (GeneralConfig.disabled  || GeneralConfig.disableEnvironment) {
			world.tickChunk(chunk, randomTickSpeed);
			return;
		}
		String taskName = null;
		if (GeneralConfig.opsTracing) {
			taskName = "EnvTick: " + chunk.toString() + "@" + chunk.hashCode();
			threadCoordinator.getCurrentTasks().add(taskName);
		}
		String finalTaskName = taskName;
		threadCoordinator.getPhaser().register();
		threadCoordinator.getExecutorService().execute(() -> {
			try {
				threadCoordinator.getCurrentEnvs().incrementAndGet();
				world.tickChunk(chunk, randomTickSpeed);
			} finally {
				threadCoordinator.getCurrentEnvs().decrementAndGet();
				threadCoordinator.getPhaser().arriveAndDeregister();
				if (GeneralConfig.opsTracing) threadCoordinator.getCurrentTasks().remove(finalTaskName);
			}
		});
	}
}
