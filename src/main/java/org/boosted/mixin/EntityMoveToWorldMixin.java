package org.boosted.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.boosted.ThreadCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
		"net.minecraft.entity.Entity;moveToWorld"//,
			//"net.minecraft.entity.ItemEntity;moveToWorld"
	));

	/**
	 * moveToWorld allows non player entities to pass through portals and change dimensions
	 */
	@Inject(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;", at = @At("HEAD"))
	private void moveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
		LOGGER.info("" + this.getEntityName() + ".moveToWorld(" + destination.toString()+")");
		StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
		List<String> callingFrame = stackWalker.walk(frames ->
			frames
				.dropWhile(frame -> !INJECTED_METHOD.equals(frame.getClassName() + ";" + frame.getMethodName()))
				.skip(1)
				.map(frame -> frame.getClassName() + ";" + frame.getMethodName()).limit(5).collect(Collectors.toList())
		);
		LOGGER.info("CALLING CLASS AND METHOD: " + callingFrame);
	}
}
