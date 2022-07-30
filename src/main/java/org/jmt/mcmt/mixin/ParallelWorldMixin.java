package org.jmt.mcmt.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jmt.mcmt.MCMTMod;
import org.jmt.mcmt.ThreadCoordinator;
import org.jmt.mcmt.config.GeneralConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class ParallelWorldMixin {

	private static final Logger LOGGER = LoggerFactory.getLogger("ParallelWorldMixin");
	private static MinecraftServer mcs;

	private static long tickStart = 0;

	// Statistics

	private static long[] lastTickTime = new long[32];
	private static int lastTickTimePos = 0;
	private static int lastTickTimeFill = 0;

	//original mod also hooked BasicEventHooks.onPostWorldTick(serverworld);

	/**
		Before the tick starts simulating the world, we create our necessary datastructures to run Minecraft worlds in parallel.
	 */
	@Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap (Ljava/lang/String;)V",
			args="ldc=levels", shift = At.Shift.AFTER, ordinal=0),
		method = "tickWorlds(Ljava/util/function/BooleanSupplier;)V")
	private void injectPreTick(CallbackInfo info) {
		final ThreadCoordinator threadCoordinator = ThreadCoordinator.getInstance();
		LOGGER.info("injectPreTick start");

		if(threadCoordinator.getExecutorService() == null) {
			threadCoordinator.setupThreadpool(4);
			LOGGER.info("setupThreadpool");
		}

		if (threadCoordinator.getPhaser() != null) {
			LOGGER.warn("Multiple servers?");
			return;
		} else {
			tickStart = System.nanoTime();
			threadCoordinator.getIsTicking().set(true);
			threadCoordinator.setPhaser(new Phaser());
			threadCoordinator.getPhaser().register();
			mcs = (MinecraftServer) (Object) this;
			//StatsCommand.setServer(mcs);
		}
		LOGGER.info("injectPreTick end");
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
		LOGGER.info("redirectTick start"  + serverWorld.getRegistryKey().getValue());
		if (GeneralConfig.disabled || GeneralConfig.disableWorld) {
			try {
				serverWorld.tick(shouldKeepTicking);
			} catch (Exception e) {
				throw e;
			} finally {
				//net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverWorld);
			}
			return;
		}
		if (mcs != (MinecraftServer) (Object) this) {
			LOGGER.warn("Multiple servers?");
			GeneralConfig.disabled = true;
			serverWorld.tick(shouldKeepTicking);
			//net.minecraftforge.fml.hooks.BasicEventHooks.onPostWorldTick(serverWorld);
			return;
		} else {
			String taskName = null;
			if (GeneralConfig.opsTracing) {
				taskName =  "WorldTick: " + serverWorld.toString() + "@" + serverWorld.hashCode();
				threadCoordinator.getCurrentTasks().add(taskName);
			}
			String finalTaskName = taskName;
			threadCoordinator.getPhaser().register();
			threadCoordinator.getExecutorService().execute(() -> {
				try {
					threadCoordinator.getCurrentWorlds().incrementAndGet();
					serverWorld.tick(shouldKeepTicking);
				} finally {
					threadCoordinator.getPhaser().arriveAndDeregister();
					LOGGER.warn(threadCoordinator.getPhaser().toString());
					threadCoordinator.getCurrentWorlds().decrementAndGet();
					if (GeneralConfig.opsTracing) threadCoordinator.getCurrentTasks().remove(finalTaskName);
				}
				LOGGER.info("redirectTick end" + serverWorld.getRegistryKey().getValue());
			});
		}
	}

	/**
	 * After launching the worlds on separate threads, the main thread must wait for all worlds to finish processing
	 * before we can simulate the next tick.
	 */
	@Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap (Ljava/lang/String;)V",
			args="ldc=connection"),
		method = "tickWorlds(Ljava/util/function/BooleanSupplier;)V")
	private void injectPostTick(CallbackInfo info) {
		final ThreadCoordinator threadCoordinator = ThreadCoordinator.getInstance();
		LOGGER.info("injectPostTick");
		if (mcs != (MinecraftServer) (Object) this) {
			LOGGER.warn("Multiple servers?");
			return;
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			LOGGER.warn(threadCoordinator.getPhaser().toString());
			threadCoordinator.getPhaser().arriveAndAwaitAdvance();
			threadCoordinator.getIsTicking().set(false);
			threadCoordinator.setPhaser(null);
			//PostExecute logic
			/*Deque<Runnable> queue = PostExecutePool.POOL.getQueue();
			Iterator<Runnable> qi = queue.iterator();
			while (qi.hasNext()) {
				Runnable r = qi.next();
				r.run();
				qi.remove();
			}*/

			lastTickTime[lastTickTimePos] = System.nanoTime() - tickStart;
			lastTickTimePos = (lastTickTimePos+1)%lastTickTime.length;
			lastTickTimeFill = Math.min(lastTickTimeFill+1, lastTickTime.length-1);
		}
	}
}
