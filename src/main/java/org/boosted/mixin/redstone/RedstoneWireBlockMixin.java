package org.boosted.mixin.redstone;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.boosted.util.EnforceBoosted;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The dreaded Mojang API mistake strikes again.
 * Blocks.redstoneWireBlock contains a static reference to wiresGivePower which is a private variable
 * that is used to disable redstone wire signals when calling getReceivedRedstonePower to prevent infinite recursion
 * Sure, it's an efficient way to do it but passing it via a parameter would be better.
 *
 * The hacky solution that stays true to the original is to use a thread local variable.
 */
@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {

	private static final ThreadLocal<Boolean> wiresGivePowerThreadLocal = new ThreadLocal<>();

	@Redirect(method = "getStrongRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I",
		at = @At(value = "FIELD", target = "net/minecraft/block/RedstoneWireBlock.wiresGivePower:Z", opcode = Opcodes.GETFIELD))
	public boolean getStrongRedstonePowerTL(RedstoneWireBlock instance) {
		return wiresGivePowerThreadLocal.get();
	}

	@Redirect(method = "getWeakRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I",
		at = @At(value = "FIELD", target = "net/minecraft/block/RedstoneWireBlock.wiresGivePower:Z", opcode = Opcodes.GETFIELD))
	public boolean getWeakRedstonePowerTL(RedstoneWireBlock instance) {
		return wiresGivePowerThreadLocal.get();
	}

	@Inject(method = "getReceivedRedstonePower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I",
		at = @At(value = "INVOKE", target = "net/minecraft/world/World.getReceivedRedstonePower(Lnet/minecraft/util/math/BlockPos;)I",
		shift = At.Shift.BEFORE))
	public void setWireGivesPowerThreadTrue(World world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
		wiresGivePowerThreadLocal.set(true);
	}

	@Inject(method = "getReceivedRedstonePower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I",
		at = @At(value = "INVOKE", target = "net/minecraft/world/World.getReceivedRedstonePower(Lnet/minecraft/util/math/BlockPos;)I",
		shift = At.Shift.AFTER))
	public void setWireGivesPowerThreadFalse(World world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
		wiresGivePowerThreadLocal.set(false);
	}
}
