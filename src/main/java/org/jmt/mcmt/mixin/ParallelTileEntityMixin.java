package org.jmt.mcmt.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.jmt.mcmt.GeneralConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(World.class)
public class ParallelTileEntityMixin {
	private static Phaser p;
	private static ExecutorService ex;
	private static Set<String> currentTasks = ConcurrentHashMap.newKeySet();

	private static final AtomicInteger currentEnts = new AtomicInteger();
	private static final AtomicInteger currentTEs = new AtomicInteger();

	/**
	 * Intercept the BlockEntityTickInvoker.tick() call in World and distribute tile entities with the execution scheduler
	 * @param beTickInvoker the block entity invoker to parallelise
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick ()V"),
			method = "tickBlockEntities()V")
	private void redirectTick(BlockEntityTickInvoker beTickInvoker) {
		World world = null; // TODO obtain world reference
		if (GeneralConfig.disabled  || GeneralConfig.disableTileEntity || !(world instanceof ServerWorld)) {
			beTickInvoker.tick();
			return;
		}
		String taskName = null;
		if (GeneralConfig.opsTracing) {
			taskName = "TETick: " + beTickInvoker.toString()  + "@" + beTickInvoker.hashCode();
			currentTasks.add(taskName);
		}
		p.register();
		String finalTaskName = taskName;
		ex.execute(() -> {
			try {
				//final boolean doLock = filterTE(beTickInvoker);
				//final ISerDesFilter filter = SerDesRegistry.getFilter(SerDesHookTypes.TETick, beTickInvoker.getClass());
				Object filter = null;
				currentTEs.incrementAndGet();
				if (filter != null) {
					//filter.serialise(beTickInvoker::tick, beTickInvoker, ((TileEntity)beTickInvoker).getPos(), world, SerDesHookTypes.TETick);
				} else {
					beTickInvoker.tick();
				}
			} catch (Exception e) {
				System.err.println("Exception ticking TE at " + beTickInvoker.getPos());
				e.printStackTrace();
			} finally {
				currentTEs.decrementAndGet();
				p.arriveAndDeregister();
				if (GeneralConfig.opsTracing) currentTasks.remove(finalTaskName);
			}
		});
	}
}
