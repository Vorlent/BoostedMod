package org.boosted.mixin.dimensions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.boosted.parallelized.ParallelServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    /**
     * Replace ServerWorld with ParallelServerWorld and watch everything break
     */

    @Redirect(method = "createWorlds(Lnet/minecraft/server/WorldGenerationProgressListener;)V",
            at = @At(value = "NEW", target = "net/minecraft/server/world/ServerWorld.<init> (Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/world/level/ServerWorldProperties;Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/world/dimension/DimensionOptions;Lnet/minecraft/server/WorldGenerationProgressListener;ZJLjava/util/List;Z)V"))
    private ServerWorld redirectServerWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session,
                                            ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions,
                                            WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld,
                                            long seed, List spawners, boolean shouldTickTime) {
        return new ParallelServerWorld(server, workerExecutor, session, properties, worldKey, dimensionOptions,
                worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime);
    }
}
