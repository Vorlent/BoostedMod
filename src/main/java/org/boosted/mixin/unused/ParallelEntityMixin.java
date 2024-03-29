package org.boosted.mixin.unused;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.boosted.ThreadCoordinator;
import org.boosted.config.GeneralConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public class ParallelEntityMixin {

	/**
	 * Intercept the Entity.tick() call in ServerWorld and distribute entities with the execution scheduler
	 * @param entity the entity to parallelize
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"),
			method = "tickEntity(Lnet/minecraft/entity/Entity;)V")
	private void redirectTick(Entity entity) {
		final ThreadCoordinator threadCoordinator = ThreadCoordinator.getInstance();
		// implement mid tick between entities
		if (GeneralConfig.disabled || GeneralConfig.disableEntity) {
			entity.tick();
			return;
		}
		String taskName = null;
		if (GeneralConfig.opsTracing) {
			taskName = "EntityTick: " + entity.toString() + "@" + entity.hashCode();
			threadCoordinator.getCurrentTasks().add(taskName);
		}
		String finalTaskName = taskName;
		threadCoordinator.getPhaser().register();
		threadCoordinator.getExecutorService().execute(() -> {
			try {
				entity.tick();
			} finally {
				threadCoordinator.getPhaser().arriveAndDeregister();
				if (GeneralConfig.opsTracing) threadCoordinator.getCurrentTasks().remove(finalTaskName);
			}
		});
	}
}
