package org.boosted.mixin.unmodifiable;

import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.function.FunctionLoader;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandFunctionManager.class)
public class CommandFunctionManagerMixin {

	@Inject(method = "load(Lnet/minecraft/server/function/FunctionLoader;)V",
			at = @At(value = "HEAD"), cancellable = true)
	private void skipLoad(FunctionLoader loader, CallbackInfo ci) {
		if (((CommandFunctionManager)(Object)this).server instanceof UnmodifiableMinecraftServer) {
			ci.cancel();
		}
	}
}
