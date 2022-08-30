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
	@Shadow public abstract String getEntityName();

	@Shadow public abstract boolean startRiding(Entity entity);

	private static final Logger LOGGER = LoggerFactory.getLogger("EntityMoveToWorldMixin");

	private static final String INJECTED_METHOD = "net.minecraft.entity.Entity;moveToWorld";

	private static final List<String> whiteListedMethods = Collections.unmodifiableList(Arrays.asList(
		"org.boosted.BoostedThreadExecutor;executeTask"
	));

	/**
	 * moveToWorld allows non player entities to pass through portals and change dimensions
	 */
	@Inject(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;", at = @At("HEAD"))
	private void moveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
		LOGGER.info("" + this.getEntityName() + ".moveToWorld(" + destination.toString()+")");
		StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
		// TODO formalized enforcing mechanism to make sure that methods
		// that have been changed by Boosted cannot be misused
		// the current solution is quite ad hoc
		// and does not account for whether the correct executor is being used
		List<String> callingFrame = stackWalker.walk(frames ->
			frames
				.dropWhile(frame -> !INJECTED_METHOD.equals(frame.getClassName() + ";" + frame.getMethodName()))
				.skip(1)
				.map(frame -> frame.getClassName() + ";" + frame.getMethodName()).limit(5).collect(Collectors.toList())
		);
		Optional<String> relevantFrame = callingFrame.stream().filter(method -> whiteListedMethods.contains(method)).findFirst();
		if(relevantFrame.isEmpty()) {
			throw new UnsupportedOperationException("Calling moveToWorld outside of a org.boosted.BoostedThreadExecutor;executeTask is not allowed. Actual frames: " + callingFrame);
		}
		LOGGER.info("CALLING CLASS AND METHOD: " + callingFrame);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"),
			method = "tickNetherPortal ()V")
	public Entity moveToWorldNetherworldPortal(Entity instance, ServerWorld destination) {
		ThreadCoordinator.getInstance().getBoostedContext().postTick().execute(() ->
			instance.moveToWorld(destination)
		);
		return null; // may need to return fake entity that throws on every method call just in case
	}
}
