package org.jmt.mcmt.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jmt.mcmt.MCMTMod;
import org.jmt.mcmt.GeneralConfig;
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

	private static Phaser p;
	private static ExecutorService ex;
	private static MinecraftServer mcs;
	private static AtomicBoolean isTicking = new AtomicBoolean();
	private static AtomicInteger threadID = new AtomicInteger();
	private static long tickStart = 0;

	private static Map<String, Set<Thread>> mcThreadTracker = new ConcurrentHashMap<String, Set<Thread>>();

	// Statistics
	private static AtomicInteger currentWorlds = new AtomicInteger();

	//Operation logging
	private static Set<String> currentTasks = ConcurrentHashMap.newKeySet();

	private static long[] lastTickTime = new long[32];
	private static int lastTickTimePos = 0;
	private static int lastTickTimeFill = 0;


	private static void setupThreadpool(int parallelism) {
		threadID = new AtomicInteger();
		final ClassLoader cl = MCMTMod.class.getClassLoader();
		ForkJoinPool.ForkJoinWorkerThreadFactory fjpf = p -> {
			ForkJoinWorkerThread fjwt = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
			fjwt.setName("MCMT-Pool-Thread-"+threadID.getAndIncrement());
			regThread("MCMT", fjwt);
			fjwt.setContextClassLoader(cl);
			return fjwt;
		};
		ex = new ForkJoinPool(
				parallelism,
				fjpf,
				null, false);
	}

	private static void regThread(String poolName, Thread thread) {
		mcThreadTracker.computeIfAbsent(poolName, s -> ConcurrentHashMap.newKeySet()).add(thread);
	}

	//original mod also hooked BasicEventHooks.onPostWorldTick(serverworld);

	/**
		Before the tick starts simulating the world, we create our necessary datastructures to run Minecraft worlds in parallel.
	 */
	@Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap (Ljava/lang/String;)V",
			args="ldc=levels", shift = At.Shift.AFTER, ordinal=0),
		method = "tickWorlds(Ljava/util/function/BooleanSupplier;)V")
	private void injectPreTick(CallbackInfo info) {
		LOGGER.info("injectPreTick start");

		if(ex == null) {
			setupThreadpool(4);
			LOGGER.info("setupThreadpool");

		}

		if (p != null) {
			LOGGER.warn("Multiple servers?");
			return;
		} else {
			tickStart = System.nanoTime();
			isTicking.set(true);
			p = new Phaser();
			p.register();
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
				currentTasks.add(taskName);
			}
			String finalTaskName = taskName;
			p.register();
			ex.execute(() -> {
				try {
					currentWorlds.incrementAndGet();
					serverWorld.tick(shouldKeepTicking);
				} finally {
					p.arriveAndDeregister();
					LOGGER.warn(p.toString());
					currentWorlds.decrementAndGet();
					if (GeneralConfig.opsTracing) currentTasks.remove(finalTaskName);
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
			LOGGER.warn(p.toString());
			p.arriveAndAwaitAdvance();
			isTicking.set(false);
			p = null;
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
