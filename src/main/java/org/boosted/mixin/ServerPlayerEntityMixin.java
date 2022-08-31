package org.boosted.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.boosted.ThreadCoordinator;
import org.boosted.util.EnforceBoosted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

	private static final String INJECTED_METHOD = "net.minecraft.server.network.ServerPlayerEntity;teleport";

	@Inject(method = "net/minecraft/server/network/ServerPlayerEntity.teleport (Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At("HEAD"))
	public void teleport(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
		EnforceBoosted.enforceBoostedThreadExecutor(INJECTED_METHOD);
	}
}
