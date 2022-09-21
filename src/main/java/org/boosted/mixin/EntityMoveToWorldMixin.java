package org.boosted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.boosted.ThreadCoordinator;
import org.boosted.util.EnforceBoosted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"),
			method = "tickNetherPortal ()V")
	public Entity moveToWorldNetherworldPortal(Entity instance, ServerWorld destination) {
		ThreadCoordinator.getInstance().getBoostedContext().postTick().execute(() ->
			instance.moveToWorld(destination)
		);
		return null; // may need to return a fake entity that throws on every method call just in case
	}
}
