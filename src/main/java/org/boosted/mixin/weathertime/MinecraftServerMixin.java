package org.boosted.mixin.weathertime;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.boosted.WeatherTimeBarrierGetter;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.boosted.util.WeatherTimeBarrier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements WeatherTimeBarrierGetter {
	@Unique
	private WeatherTimeBarrier boosted$weatherTimeBarrier;
	@Unique
	private UnmodifiableMinecraftServer boosted$unmodifiableMinecraftServer;
	public WeatherTimeBarrier getWeatherTimeBarrier() {
		if (boosted$weatherTimeBarrier == null) {
			boosted$weatherTimeBarrier = new WeatherTimeBarrier();
			boosted$unmodifiableMinecraftServer = new UnmodifiableMinecraftServer(((MinecraftServer)(Object)this));
		}
		return boosted$weatherTimeBarrier;
	}

	@Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap (Ljava/lang/String;)V",
			args="ldc=levels", shift = At.Shift.AFTER, ordinal=0),
			method = "tickWorlds(Ljava/util/function/BooleanSupplier;)V")
	private void injectPreTick(CallbackInfo info) {
		// warm up boosted weather time barrier
		WeatherTimeBarrier weatherTimeBarrier = getWeatherTimeBarrier();
		weatherTimeBarrier.reset();
		for (World world : ((MinecraftServer)(Object)this).getWorlds()) {
			weatherTimeBarrier.definePhase(world);
		}
	}
}
