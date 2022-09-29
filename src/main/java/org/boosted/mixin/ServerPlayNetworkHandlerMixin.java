package org.boosted.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.boosted.ThreadCoordinator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

	@Shadow public abstract ServerPlayerEntity getPlayer();

	@Redirect(at = @At(value = "INVOKE", target = "net/minecraft/server/network/ServerPlayerEntity.teleport (Lnet/minecraft/server/world/ServerWorld;DDDFF)V"),
			method = "onSpectatorTeleport(Lnet/minecraft/network/packet/c2s/play/SpectatorTeleportC2SPacket;)V")
	public void teleport(ServerPlayerEntity instance, ServerWorld serverWorld, double x, double y, double z, float yaw, float pitch) {
		ThreadCoordinator.getInstance().getBoostedContext().postTick().execute(() ->
			this.getPlayer().teleport(serverWorld, x, y, z, yaw, pitch)
		);
	}
}
