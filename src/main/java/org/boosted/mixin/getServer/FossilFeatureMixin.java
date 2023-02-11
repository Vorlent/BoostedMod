package org.boosted.mixin.getServer;

import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.feature.FossilFeature;
import net.minecraft.world.gen.feature.FossilFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FossilFeature.class)
public class FossilFeatureMixin {

    @Override
    public boolean generate(FeatureContext<FossilFeatureConfig> context) {
        int k;
        Random random = context.getRandom();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        BlockRotation blockRotation = BlockRotation.random(random);
        FossilFeatureConfig fossilFeatureConfig = context.getConfig();
        int i = random.nextInt(fossilFeatureConfig.fossilStructures.size());
        StructureTemplateManager structureTemplateManager = structureWorldAccess.toServerWorld().getServer().getStructureTemplateManager();
        StructureTemplate structureTemplate = structureTemplateManager.getTemplateOrBlank(fossilFeatureConfig.fossilStructures.get(i));
        StructureTemplate structureTemplate2 = structureTemplateManager.getTemplateOrBlank(fossilFeatureConfig.overlayStructures.get(i));
        ChunkPos chunkPos = new ChunkPos(blockPos);
        BlockBox blockBox = new BlockBox(chunkPos.getStartX() - 16, structureWorldAccess.getBottomY(), chunkPos.getStartZ() - 16, chunkPos.getEndX() + 16, structureWorldAccess.getTopY(), chunkPos.getEndZ() + 16);
        StructurePlacementData structurePlacementData = new StructurePlacementData().setRotation(blockRotation).setBoundingBox(blockBox).setRandom(random);
        Vec3i vec3i = structureTemplate.getRotatedSize(blockRotation);
        BlockPos blockPos2 = blockPos.add(-vec3i.getX() / 2, 0, -vec3i.getZ() / 2);
        int j = blockPos.getY();
        for (k = 0; k < vec3i.getX(); ++k) {
            for (int l = 0; l < vec3i.getZ(); ++l) {
                j = Math.min(j, structureWorldAccess.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, blockPos2.getX() + k, blockPos2.getZ() + l));
            }
        }
        k = Math.max(j - 15 - random.nextInt(10), structureWorldAccess.getBottomY() + 10);
        BlockPos blockPos3 = structureTemplate.offsetByTransformedSize(blockPos2.withY(k), BlockMirror.NONE, blockRotation);
        if (FossilFeature.getEmptyCorners(structureWorldAccess, structureTemplate.calculateBoundingBox(structurePlacementData, blockPos3)) > fossilFeatureConfig.maxEmptyCorners) {
            return false;
        }
        structurePlacementData.clearProcessors();
        fossilFeatureConfig.fossilProcessors.value().getList().forEach(structurePlacementData::addProcessor);
        structureTemplate.place(structureWorldAccess, blockPos3, blockPos3, structurePlacementData, random, 4);
        structurePlacementData.clearProcessors();
        fossilFeatureConfig.overlayProcessors.value().getList().forEach(structurePlacementData::addProcessor);
        structureTemplate2.place(structureWorldAccess, blockPos3, blockPos3, structurePlacementData, random, 4);
        return true;
    }
}
