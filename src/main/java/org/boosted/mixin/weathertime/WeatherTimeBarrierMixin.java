package org.boosted.mixin.weathertime;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class WeatherTimeBarrierMixin {
	@Shadow @NotNull
	public abstract MinecraftServer getServer();

	@Inject(at = @At("HEAD"), method = "tick(Ljava/util/function/BooleanSupplier;)V")
	private void preWeatherTime(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		this.getServer().getWeatherTimeBarrier().startPhase((World)(Object)this);
	}

	@Inject(at = @At(value ="INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickTime()V", shift = At.Shift.AFTER),
		method = "tick(Ljava/util/function/BooleanSupplier;)V")
	private void postWeatherTime(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		this.getServer().getWeatherTimeBarrier().endPhase((World)(Object)this);
	}
}
