package org.boosted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(targets = "net.minecraft.block.AbstractBlock.AbstractBlockState")
public abstract class OnEntityCollisionPatchMixin {

	@Shadow protected abstract BlockState asBlockState();

	static Set<Class<?>> onCollisionFixBypassClass = new HashSet<>();

	static {
		try {
			onCollisionFixBypassClass.add(Class.forName("blusunrize.immersiveengineering.common.blocks.IETileProviderBlock"));
		} catch (ClassNotFoundException cnfe) {}
	}

	/**
	 * Override onEntityCollision for AbstractBlockState so we can synchronize collision detection
	 */
	@Inject(method = "onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V",
			at = @At("HEAD"), cancellable = true)
	private void onEntityCollisionFix(World w, BlockPos p, Entity e, CallbackInfoReturnable<Boolean> cir) {
		BlockState bs = this.asBlockState();
		//TODO Add config for this
		if (onCollisionFixBypassClass.contains(bs.getBlock().getClass())) {
			synchronized (bs.getBlock()) {
				bs.getBlock().onEntityCollision(bs, w, p, e);
			}
			cir.cancel();
		}
	}
}
