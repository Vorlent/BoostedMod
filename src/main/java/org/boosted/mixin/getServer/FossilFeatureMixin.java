package org.boosted.mixin.getServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.FossilFeature;
import net.minecraft.world.gen.feature.FossilFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FossilFeature.class)
public class FossilFeatureMixin {

    private ServerWorld serverWorld = null; // ugly, TODO check if this is thread safe

    @Redirect(method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
        at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    public MinecraftServer skipGetServer(ServerWorld instance) {
        serverWorld = instance;
        return null;
    }

    @Redirect(method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getStructureTemplateManager ()Lnet/minecraft/structure/StructureTemplateManager;"))
    public StructureTemplateManager skipGetStructureTemplateManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
            at = @At(value = "INVOKE", target = "net/minecraft/structure/StructureTemplateManager.getTemplateOrBlank (Lnet/minecraft/util/Identifier;)Lnet/minecraft/structure/StructureTemplate;"))
    public StructureTemplate redirectGetTemplateOrBlank(StructureTemplateManager instance, Identifier id) {
        return serverWorld.getSynchronizedServer().readExp(server ->
                server.getStructureTemplateManager().getTemplateOrBlank(id)
        );
    }
}
