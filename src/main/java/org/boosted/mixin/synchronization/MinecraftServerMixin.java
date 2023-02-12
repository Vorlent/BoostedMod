package org.boosted.mixin.synchronization;

import net.minecraft.server.MinecraftServer;
import org.boosted.SynchronizedServerGetter;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.boosted.util.RWLockSynchronizedResource;
import org.boosted.util.SynchronizedResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;


@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements SynchronizedServerGetter {
    @Unique
    private SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> boosted$synchronizedResource;

    public SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> getSynchronizedServer() {
        if (boosted$synchronizedResource == null) {
            MinecraftServer server = ((MinecraftServer) (Object) this);
            boosted$synchronizedResource = new RWLockSynchronizedResource<>(server,
                    new UnmodifiableMinecraftServer(server));
        }
        return boosted$synchronizedResource;
    }

    /**
     * Redundant if you already have a reference to MinecraftServer
     */
    public MinecraftServer getUnsynchronizedServer() {
        return ((MinecraftServer) (Object) this);
    }
}
