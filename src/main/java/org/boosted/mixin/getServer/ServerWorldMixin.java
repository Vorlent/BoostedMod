package org.boosted.mixin.getServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.boosted.SynchronizedServerGetter;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.boosted.util.SynchronizedResource;
import org.boosted.util.UnsynchronizedResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements SynchronizedServerGetter {
	@Unique
	private SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> boosted$unsynchronizedResource;
	@Override
	public SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> getSynchronizedServer() {
		if (boosted$unsynchronizedResource == null) {
			MinecraftServer server = ((SynchronizedServerGetter) (Object) this).getUnsynchronizedServer();
			boosted$unsynchronizedResource = new UnsynchronizedResource<>(server,
				new UnmodifiableMinecraftServer(server));
		}
		return boosted$unsynchronizedResource;
	}

	@Redirect(method = "tickWeather()V",
		at = @At(value = "FIELD", target = "net/minecraft/server/world/ServerWorld.server : Lnet/minecraft/server/MinecraftServer;"))
	private MinecraftServer getServerInTickWeather(ServerWorld instance) {
		return getUnsynchronizedServer(); // tickWeather is already synchronized
	}

	/**
	 * Only evil people use this :(
	 */
	@Override
	public MinecraftServer getUnsynchronizedServer() {
		return ((ServerWorld) (Object) this).getServer();
	}
}
