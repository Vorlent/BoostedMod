package org.boosted.mixin.portal;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.boosted.util.BoostedTeleportation;
import org.boosted.util.EnforceBoosted;
import org.boosted.util.UnsupportedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMoveToWorldMixin {

	private static final String INJECTED_METHOD = "net.minecraft.entity.Entity;moveToWorld";

	/**
	 * moveToWorld allows non player entities to pass through portals and change dimensions
	 */
	@Inject(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;", at = @At("HEAD"))
	private void moveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
		EnforceBoosted.enforceBoostedThreadExecutor(INJECTED_METHOD);
	}

	/*@Redirect(method = "moveToWorld",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isRemoved()Z"))
	public boolean redirectIsRemoved(Entity instance) {
		return instance.isRemoved() && instance.getRemovalReason() != Entity.RemovalReason.CHANGED_DIMENSION;
	}*/

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"),
			method = "tickPortal ()V")
	public Entity moveToWorldNetherworldPortal(Entity instance, ServerWorld destination) {
		BoostedTeleportation.teleportEntity(instance, destination);
		return new UnsupportedEntity(instance.getType(), destination); // may need to return a fake entity that throws on every method call just in case
	}
}
