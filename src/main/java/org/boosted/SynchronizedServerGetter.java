package org.boosted;

import net.minecraft.server.MinecraftServer;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.boosted.util.SynchronizedResource;

public interface SynchronizedServerGetter {
    default SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> getSynchronizedServer() {
        throw new UnsupportedOperationException("SynchronizedServerGetter.getSynchronizedServer has not been implemented");
    }

    default MinecraftServer getUnsynchronizedServer() {
        throw new UnsupportedOperationException("SynchronizedServerGetter.getUnsynchronizedServer has not been implemented");
    }
}
