package org.boosted.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.boosted.ThreadCoordinator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(TeleportCommand.class)
public abstract class TeleportCommandMixin {

	@Redirect(at = @At(value = "INVOKE", target = "net/minecraft/server/command/TeleportCommand.teleport(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/entity/Entity;Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FFLnet/minecraft/server/command/TeleportCommand$LookTarget;)V"),
			method = "execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/entity/Entity;)I")
	private static void executeTeleportOne(ServerCommandSource source, Entity target, ServerWorld world, double x, double y, double z,
										   Set<PlayerPositionLookS2CPacket.Flag> movementFlags, float yaw, float pitch, TeleportCommand.@Nullable LookTarget facingLocation) throws CommandSyntaxException {
		// necessary precondition for teleportation
		BlockPos blockPos = new BlockPos(x, y, z);
		if (!World.isValid(blockPos)) {
			throw TeleportCommand.INVALID_POSITION_EXCEPTION.create();
		}
		ThreadCoordinator.getInstance().getBoostedContext().postTick().execute(() ->
			{
				try {
					TeleportCommand.teleport(source, target, world, x, y, z, movementFlags, yaw, pitch, facingLocation);
				} catch (CommandSyntaxException e) {
					throw new RuntimeException("TeleportCommand.teleport threw an exception, this should not happen anymore", e);
				}
			}
		);
	}

	@Redirect(at = @At(value = "INVOKE", target = "net/minecraft/server/command/TeleportCommand.teleport(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/entity/Entity;Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FFLnet/minecraft/server/command/TeleportCommand$LookTarget;)V"),
			method = "execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/command/argument/PosArgument;Lnet/minecraft/command/argument/PosArgument;Lnet/minecraft/server/command/TeleportCommand$LookTarget;)I")
	private static void executeTeleportTwo(ServerCommandSource source, Entity target, ServerWorld world, double x, double y, double z,
										   Set<PlayerPositionLookS2CPacket.Flag> movementFlags, float yaw, float pitch, TeleportCommand.@Nullable LookTarget facingLocation) throws CommandSyntaxException {
		// necessary precondition for teleportation
		BlockPos blockPos = new BlockPos(x, y, z);
		if (!World.isValid(blockPos)) {
			throw TeleportCommand.INVALID_POSITION_EXCEPTION.create();
		}
		ThreadCoordinator.getInstance().getBoostedContext().postTick().execute(() ->
			{
				try {
					TeleportCommand.teleport(source, target, world, x, y, z, movementFlags, yaw, pitch, facingLocation);
				} catch (CommandSyntaxException e) {
					throw new RuntimeException("TeleportCommand.teleport threw an exception, this should not happen anymore", e);
				}
			}
		);
	}
}
