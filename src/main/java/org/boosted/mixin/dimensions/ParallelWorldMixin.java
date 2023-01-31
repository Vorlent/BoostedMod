package org.boosted.mixin.dimensions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.boosted.BoostedGlobalContext;
import org.boosted.BoostedWorldContext;
import org.boosted.ThreadCoordinator;
import org.boosted.config.GeneralConfig;
import org.boosted.util.WorldTickBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class ParallelWorldMixin {

	private static final Logger LOGGER = LoggerFactory.getLogger("ParallelWorldMixin");
	private static MinecraftServer mcs;

	private static long tickStart = 0;

	// Statistics

	private static final long[] lastTickTime = new long[32];
	private static int lastTickTimePos = 0;
	private static int lastTickTimeFill = 0;

	private final WorldTickBarrier barrier = new WorldTickBarrier();

	//original mod also hooked BasicEventHooks.onPostWorldTick(serverworld);

	/**
		Before the tick starts simulating the world, we create our necessary datastructures to run Minecraft worlds in parallel.
	 */
	@Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap (Ljava/lang/String;)V",
			args="ldc=levels", shift = At.Shift.AFTER, ordinal=0),
		method = "tickWorlds(Ljava/util/function/BooleanSupplier;)V")
	private void injectPreTick(CallbackInfo info) {
		final ThreadCoordinator threadCoordinator = ThreadCoordinator.getInstance();
		//LOGGER.info("injectPreTick start");

		if (threadCoordinator.getExecutorService() == null) {
			threadCoordinator.setupThreadpool(Runtime.getRuntime().availableProcessors());
			LOGGER.info("setupThreadpool");
		}

		if (threadCoordinator.getPhaser() != null) {
			LOGGER.warn("Multiple servers?");
		} else {
			tickStart = System.nanoTime();
			threadCoordinator.getIsTicking().set(true);
			barrier.reset();
			mcs = (MinecraftServer) (Object) this;
			//StatsCommand.setServer(mcs);
			if (threadCoordinator.getBoostedContext() == null) {
				threadCoordinator.setBoostedContext(new BoostedGlobalContext(Thread.currentThread()));
			}
			// warm up boosted world context before the tick starts
			for (World world : mcs.getWorlds()) {
				world.getBoostedWorldContext();
			}
			threadCoordinator.getBoostedContext().preTick().runTasks();
		}
		//LOGGER.info("injectPreTick end");
	}

	/**
	 * The original Minecraft code iterates over all Minecraft worlds, we intercept each iteration
	 * and launch the world on our executor service, this means that each iteration returns immediately
	 * and starts all worlds at once
	 * @param serverWorld the world to parallelise
	 * @param shouldKeepTicking passed onto serverWorld.tick(shouldKeepTicking)
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V"),
			method = "tickWorlds(Ljava/util/function/BooleanSupplier;)V")
	private void redirectTick(ServerWorld serverWorld, BooleanSupplier shouldKeepTicking) {
		final ThreadCoordinator threadCoordinator = ThreadCoordinator.getInstance();
		//LOGGER.info("redirectTick start"  + serverWorld.getRegistryKey().getValue());
		if (GeneralConfig.disabled || GeneralConfig.disableWorld) {
			try {
				//TODO switch to single threaded executors
				serverWorld.tick(shouldKeepTicking);
			} catch (Exception e) {
				throw e;
			} finally {
				//net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverWorld);
			}
			return;
		}
		if (mcs != (Object) this) {
			LOGGER.warn("Multiple servers?");
			GeneralConfig.disabled = true;
			serverWorld.tick(shouldKeepTicking);
			//net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverWorld);
		} else {
			String taskName = null;
			if (GeneralConfig.opsTracing) {
				taskName =  "WorldTick: " + serverWorld.toString() + "@" + serverWorld.hashCode();
				threadCoordinator.getCurrentTasks().add(taskName);
			}
			String finalTaskName = taskName;
			barrier.registerWorld();
			threadCoordinator.getExecutorService().execute(() -> {

				BoostedWorldContext boostedWorldContext = serverWorld.getBoostedWorldContext();
				try {
					threadCoordinator.getCurrentWorlds().incrementAndGet();
					boostedWorldContext.setThread(Thread.currentThread());
					serverWorld.tick(shouldKeepTicking);
					boostedWorldContext.postTick().runTasks();
				} finally {
					boostedWorldContext.setThread(null);
					barrier.finishWorld();
					//LOGGER.warn(threadCoordinator.getPhaser().toString());
					threadCoordinator.getCurrentWorlds().decrementAndGet();
					if (GeneralConfig.opsTracing) threadCoordinator.getCurrentTasks().remove(finalTaskName);
				}
				//LOGGER.info("redirectTick end" + serverWorld.getRegistryKey().getValue());
			});
		}
	}

	/**
	 * After launching all worlds on separate threads, the main thread must wait for all worlds to finish processing
	 * before we can simulate the next tick.
	 */
	@Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap (Ljava/lang/String;)V",
			args="ldc=connection"),
		method = "tickWorlds(Ljava/util/function/BooleanSupplier;)V")
	private void injectPostTick(CallbackInfo info) {
		final ThreadCoordinator threadCoordinator = ThreadCoordinator.getInstance();
		//LOGGER.info("injectPostTick");
		if (mcs != (Object) this) {
			LOGGER.warn("Multiple servers?");
		} else {
			// wait until all worlds have finished
			barrier.waitForAllWorlds();
			threadCoordinator.getIsTicking().set(false);

			// Go back to main thread
			for (World world : mcs.getWorlds()) {
				world.getBoostedWorldContext().setThread(Thread.currentThread());
			}

			threadCoordinator.getBoostedContext().postTick().runTasks();

			lastTickTime[lastTickTimePos] = System.nanoTime() - tickStart;
			//LOGGER.info("Tick time " + lastTickTime[lastTickTimePos] / 1000000 + "ms");
			lastTickTimePos = (lastTickTimePos+1) % lastTickTime.length;
			lastTickTimeFill = Math.min(lastTickTimeFill + 1, lastTickTime.length - 1);
		}
	}
}
