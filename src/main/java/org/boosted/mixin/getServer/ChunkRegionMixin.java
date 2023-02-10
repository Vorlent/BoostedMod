package org.boosted.mixin.getServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkRegion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkRegion.class)
public class ChunkRegionMixin {
    /* this doesn't need a mixin but since WorldAccess exposes getServer() it
    will throw when ParallelServerWorld throws.
    @Nullable
    public MinecraftServer getServer() {
        return this.world.getServer();
    }*/

}
