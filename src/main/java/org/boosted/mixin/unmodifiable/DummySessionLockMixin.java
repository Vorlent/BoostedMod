package org.boosted.mixin.unmodifiable;

import net.minecraft.world.level.storage.SessionLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(SessionLock.class)
public class DummySessionLockMixin {

	@Inject(method = "create(Ljava/nio/file/Path;)Lnet/minecraft/world/level/storage/SessionLock;",
		cancellable = true,	at = @At(value = "HEAD"))
	private static void skipInitialization(Path path, CallbackInfoReturnable<SessionLock> cir) throws IOException {
		if (path.startsWith("/DUMMY")) {
			cir.cancel();
		}
	}
}
