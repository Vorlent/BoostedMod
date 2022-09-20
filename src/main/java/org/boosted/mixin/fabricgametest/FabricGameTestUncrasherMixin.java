package org.boosted.mixin.fabricgametest;

import net.minecraft.test.GameTestState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.test.StructureTestListener;

/**
 * fabric-gametest-api crashes Minecraft, we have to uncrash it.
 */
@Mixin(StructureTestListener.class)
public class FabricGameTestUncrasherMixin {
	/**
		test.getPos() means the structure hasn't been loaded, just skip the visualization altogether
	 */
	@Inject(method = "finishPassedTest(Lnet/minecraft/test/GameTestState;Ljava/lang/String;)V",
		at = @At("HEAD"), cancellable = true)
	private static void finishPassedTest(GameTestState test, String output, CallbackInfo info) {
		if(test.getPos() == null) {
			info.cancel();
		}
	}

	/**
	 test.getPos() means the structure hasn't been loaded, just skip the visualization altogether
	 */
	@Inject(method = "passTest(Lnet/minecraft/test/GameTestState;Ljava/lang/String;)V",
			at = @At("HEAD"), cancellable = true)
	private static void passTest(GameTestState test, String output, CallbackInfo info) {
		if(test.getPos() == null) {
			info.cancel();
		}
	}

	/**
	 test.getPos() means the structure hasn't been loaded, just skip the visualization altogether
	 */
	@Inject(method = "failTest(Lnet/minecraft/test/GameTestState;Ljava/lang/Throwable;)V",
			at = @At("HEAD"), cancellable = true)
	private static void failTest(GameTestState test, Throwable output, CallbackInfo info) {
		if (test.getPos() == null) {
			info.cancel();
		}
	}
}
