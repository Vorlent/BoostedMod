package org.jmt.mcmt.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.jmt.mcmt.GeneralConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ServerWorld.class)
public class ParallelEntityMixin {
	private static Phaser p;
	private static ExecutorService ex;
	private static Set<String> currentTasks = ConcurrentHashMap.newKeySet();

	private static final AtomicInteger currentEnts = new AtomicInteger();
	private static final AtomicInteger currentTEs = new AtomicInteger();

	/**
	 * Intercept the Entity.tick() call in ServerWorld and distribute entities with the execution scheduler
	 * @param entity the entity to parallelise
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"),
			method = "tickEntity(Lnet/minecraft/entity/Entity;)V")
	private void redirectTick(Entity entity) {
		if (GeneralConfig.disabled || GeneralConfig.disableEntity) {
			entity.tick();
			return;
		}
		String taskName = null;
		if (GeneralConfig.opsTracing) {
			taskName = "EntityTick: " + entity.toString() + "@" + entity.hashCode();
			currentTasks.add(taskName);
		}
		String finalTaskName = taskName;
		p.register();
		ex.execute(() -> {
			try {
				currentEnts.incrementAndGet();
				//final ISerDesFilter filter = SerDesRegistry.getFilter(SerDesHookTypes.EntityTick, entity.getClass());
				Object filter = null;
				currentTEs.incrementAndGet();
				if (filter != null) {
					//filter.serialise(entity::tick, entity, entity.getPosition(), serverworld, SerDesHookTypes.EntityTick);
				} else {
					entity.tick();
				}
			} finally {
				currentEnts.decrementAndGet();
				p.arriveAndDeregister();
				if (GeneralConfig.opsTracing) currentTasks.remove(finalTaskName);
			}
		});
	}
}
