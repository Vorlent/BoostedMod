package org.jmt.mcmt.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ThreadExecutor.class)
public abstract class ThreadExecutorPatchMixin {
	@Shadow public abstract boolean isOnThread();

	private static Map<String, Set<Thread>> mcThreadTracker = new ConcurrentHashMap<String, Set<Thread>>();

	public static boolean isThreadPooled(String poolName, Thread t) {
		return mcThreadTracker.containsKey(poolName) && mcThreadTracker.get(poolName).contains(t);
	}

	public static boolean serverExecutionThreadPatch(MinecraftServer ms) {
		return isThreadPooled("MCMT", Thread.currentThread());
	}

	/**
	 * Override isOnThread for MinecraftServer so that it returns true if the thread is in our threadpool
	 */
	@Inject(method = "isOnThread()Z", at = @At("HEAD"), cancellable = true)
	private void isOnThread(CallbackInfoReturnable<Boolean> cir) {
		if((Object)this instanceof MinecraftServer) {
			MinecraftServer server = (MinecraftServer)(Object)this;
			cir.setReturnValue(this.isOnThread() || serverExecutionThreadPatch(server));
		}
	}
}
