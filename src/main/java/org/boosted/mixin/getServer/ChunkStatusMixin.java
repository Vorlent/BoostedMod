package org.boosted.mixin.getServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {

    @Redirect(method = "method_39464(Lnet/minecraft/world/chunk/ChunkStatus;Ljava/util/concurrent/Executor;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/structure/StructureTemplateManager;Lnet/minecraft/server/world/ServerLightingProvider;Ljava/util/function/Function;Ljava/util/List;Lnet/minecraft/world/chunk/Chunk;Z)Ljava/util/concurrent/CompletableFuture;",
        at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    private static MinecraftServer redirectGetServer(ServerWorld instance) {
        return null;
    }

    @Redirect(method = "method_39464(Lnet/minecraft/world/chunk/ChunkStatus;Ljava/util/concurrent/Executor;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/structure/StructureTemplateManager;Lnet/minecraft/server/world/ServerLightingProvider;Ljava/util/function/Function;Ljava/util/List;Lnet/minecraft/world/chunk/Chunk;Z)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getSaveProperties ()Lnet/minecraft/world/SaveProperties;"))
    private static SaveProperties redirectGetSaveProperties(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "method_39464(Lnet/minecraft/world/chunk/ChunkStatus;Ljava/util/concurrent/Executor;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/structure/StructureTemplateManager;Lnet/minecraft/server/world/ServerLightingProvider;Ljava/util/function/Function;Ljava/util/List;Lnet/minecraft/world/chunk/Chunk;Z)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "INVOKE", target = "net/minecraft/world/SaveProperties.getGeneratorOptions ()Lnet/minecraft/world/gen/GeneratorOptions;"))
    private static GeneratorOptions redirectGetGeneratorOptions(SaveProperties instance) {
        return null;
    }

    @Redirect(method = "method_39464(Lnet/minecraft/world/chunk/ChunkStatus;Ljava/util/concurrent/Executor;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/structure/StructureTemplateManager;Lnet/minecraft/server/world/ServerLightingProvider;Ljava/util/function/Function;Ljava/util/List;Lnet/minecraft/world/chunk/Chunk;Z)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "INVOKE", target = "net/minecraft/world/gen/GeneratorOptions.shouldGenerateStructures ()Z"))
    private static boolean redirectShouldGenerateStructures(GeneratorOptions instance) {
        return true;
    }

    @Inject(method = "method_39464(Lnet/minecraft/world/chunk/ChunkStatus;Ljava/util/concurrent/Executor;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/structure/StructureTemplateManager;Lnet/minecraft/server/world/ServerLightingProvider;Ljava/util/function/Function;Ljava/util/List;Lnet/minecraft/world/chunk/Chunk;Z)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "net/minecraft/world/gen/chunk/ChunkGenerator.setStructureStarts (Lnet/minecraft/util/registry/DynamicRegistryManager;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/structure/StructureTemplateManager;J)V"))
    private static void redirectGetGeneratorOptions(ChunkStatus targetStatus, Executor executor,
            ServerWorld world, ChunkGenerator generator, StructureTemplateManager structureTemplateManager,
            ServerLightingProvider lightingProvider, Function fullChunkConverter, List chunks,
            Chunk chunk, boolean regenerate, CallbackInfoReturnable<CompletableFuture> cir) {
        if (world.getSynchronizedServer()
                .readExp(server -> server.getSaveProperties().getGeneratorOptions().shouldGenerateStructures())) {
            generator.setStructureStarts(world.getRegistryManager(), world.getChunkManager().getNoiseConfig(),
                    world.getStructureAccessor(), chunk, structureTemplateManager, world.getSeed());
        }
    }
}
