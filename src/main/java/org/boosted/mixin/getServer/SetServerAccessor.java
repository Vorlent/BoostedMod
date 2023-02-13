package org.boosted.mixin.getServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerWorld.class)
public interface SetServerAccessor {

    @Accessor
    void setServer(MinecraftServer server);

    @Accessor("server")
    MinecraftServer getServerField();
}
