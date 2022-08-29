package org.boosted.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.boosted.ThreadCoordinator;
import org.boosted.config.GeneralConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class ParallelTileEntityMixin {

	/**
	 * Intercept the BlockEntityTickInvoker.tick() call in World and distribute tile entities with the execution scheduler
	 * @param beTickInvoker the block entity invoker to parallelize
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick ()V"),
			method = "tickBlockEntities()V")
	private void redirectTick(BlockEntityTickInvoker beTickInvoker) {
		final ThreadCoordinator threadCoordinator = ThreadCoordinator.getInstance();
		//TODO implement mid tick between tile entities
		World world = null; // TODO obtain world reference
		if (GeneralConfig.disabled  || GeneralConfig.disableTileEntity || !(world instanceof ServerWorld)) {
			beTickInvoker.tick();
			return;
		}
		String taskName = null;
		if (GeneralConfig.opsTracing) {
			taskName = "TETick: " + beTickInvoker.toString()  + "@" + beTickInvoker.hashCode();
			threadCoordinator.getCurrentTasks().add(taskName);
		}
		threadCoordinator.getPhaser().register();
		String finalTaskName = taskName;
		threadCoordinator.getExecutorService().execute(() -> {
			try {
				//final boolean doLock = filterTE(beTickInvoker);
				//final ISerDesFilter filter = SerDesRegistry.getFilter(SerDesHookTypes.TETick, beTickInvoker.getClass());
				Object filter = null;
				threadCoordinator.getCurrentTEs().incrementAndGet();
				if (filter != null) {
					//filter.serialise(beTickInvoker::tick, beTickInvoker, ((TileEntity)beTickInvoker).getPos(), world, SerDesHookTypes.TETick);
				} else {
					beTickInvoker.tick();
				}
			} catch (Exception e) {
				System.err.println("Exception ticking TE at " + beTickInvoker.getPos());
				e.printStackTrace();
			} finally {
				threadCoordinator.getCurrentTEs().decrementAndGet();
				threadCoordinator.getPhaser().arriveAndDeregister();
				if (GeneralConfig.opsTracing) threadCoordinator.getCurrentTasks().remove(finalTaskName);
			}
		});
	}
}
