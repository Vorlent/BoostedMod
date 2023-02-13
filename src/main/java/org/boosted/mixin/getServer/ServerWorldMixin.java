package org.boosted.mixin.getServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.boosted.SynchronizedServerGetter;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.boosted.util.SynchronizedResource;
import org.boosted.util.UnsynchronizedResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements SynchronizedServerGetter {
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

	/**
	 * Only evil people use this :(
	 */
	@Override
	public MinecraftServer getUnsynchronizedServer() {
		return ((ServerWorld) (Object) this).getServer();
	}
}
