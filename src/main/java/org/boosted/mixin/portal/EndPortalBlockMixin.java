package org.boosted.mixin.portal;

import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.boosted.ThreadCoordinator;
import org.boosted.util.BoostedTeleportation;
import org.boosted.util.UnsupportedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"),
			method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	public Entity moveToWorldEndPortal(Entity instance, ServerWorld destination) {
		BoostedTeleportation.teleportEntity(instance, destination);
		return new UnsupportedEntity(instance.getType(), destination); // may need to return fake entity that throws on every method call just in case
	}
}
