package org.boosted.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.thread.ThreadExecutor;
import org.boosted.ThreadCoordinator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThreadExecutor.class)
public abstract class ThreadExecutorPatchMixin {
	@Shadow public abstract boolean isOnThread();

	/**
	 * Override isOnThread for MinecraftServer so that it returns true if the thread is in our threadpool
	 */
	@Inject(method = "isOnThread()Z", at = @At("HEAD"), cancellable = true)
	private void isOnThread(CallbackInfoReturnable<Boolean> cir) {
		if((Object)this instanceof MinecraftServer) {
			MinecraftServer server = (MinecraftServer)(Object)this;
			final ThreadCoordinator threadCoordinator = ThreadCoordinator.getInstance();
			cir.setReturnValue(this.isOnThread() || threadCoordinator.serverExecutionThreadPatch(server));
		}
	}
}
