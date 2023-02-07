package org.boosted;

import net.minecraft.server.MinecraftServer;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.boosted.util.SynchronizedResource;

public interface SynchronizedServerGetter {
    default SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> getSynchronizedServer() {
        throw new UnsupportedOperationException();
    }

    default MinecraftServer getUnsynchronizedServer() {
        throw new UnsupportedOperationException();
    }
}
