package org.boosted.mixin.dimensions;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.impl.dimension.FabricDimensionInternals;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.boosted.ThreadCoordinator;
import org.boosted.util.UnsupportedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FabricDimensions.class)
public abstract class FabricDimensionsMixin {

	@Redirect(at = @At(value = "INVOKE", target = "net/fabricmc/fabric/impl/dimension/FabricDimensionInternals.changeDimension (Lnet/minecraft/entity/Entity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/entity/Entity;"),
			method = "teleport(Lnet/minecraft/entity/Entity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/entity/Entity;")
	private static <E extends Entity> Entity moveToWorldEndPortal(E teleported, ServerWorld destination, TeleportTarget target) {
		ThreadCoordinator.getInstance().getBoostedContext().postTick().execute(() ->
			FabricDimensionInternals.changeDimension(teleported, destination, target)
		);
		return new UnsupportedEntity(teleported.getType(), destination); // may need to return a fake entity that throws on every method call just in case
	}
}
