package org.boosted.mixin.getServer;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureContext.class)
public class StructureContextMixin {
    
 /* This is tricky...
    public static StructureContext from(ServerWorld world) {
        MinecraftServer minecraftServer = world.getServer();
        return new StructureContext(minecraftServer.getResourceManager(), minecraftServer.getRegistryManager(), minecraftServer.getStructureTemplateManager());
    }
  */
}
