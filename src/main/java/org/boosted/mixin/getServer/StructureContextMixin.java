package org.boosted.mixin.getServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(StructureContext.class)
public class StructureContextMixin {

    /**
     * @author Vorlent
     * @reason technically getServer can be redirected with @Redirect
     */
    @Overwrite
    public static StructureContext from(ServerWorld world) { // check if references escape
        MinecraftServer minecraftServer = world.getUnsynchronizedServer();
        return new StructureContext(minecraftServer.getResourceManager(), minecraftServer.getRegistryManager(), minecraftServer.getStructureTemplateManager());
    }
}
