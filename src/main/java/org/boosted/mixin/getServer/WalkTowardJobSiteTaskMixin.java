package org.boosted.mixin.getServer;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.GoToWorkTask;
import net.minecraft.entity.ai.brain.task.WalkTowardJobSiteTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(WalkTowardJobSiteTask.class)
public class WalkTowardJobSiteTaskMixin {
    /**
     * @author Vorlent
     * @reason this function needs exclusive write access to MinecraftServer
     */
    @Overwrite
    protected void finishRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        Optional<GlobalPos> optional = villagerEntity.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
        optional.ifPresent(pos -> {
            BlockPos blockPos = pos.getPos();
            /* PATCH BEGIN */
            serverWorld.getSynchronizedServer().write(server -> {
                //TODO, warning there is a cross reference to another world
                ServerWorld serverWorld2 = serverWorld.getServer().getWorld(pos.getDimension());
                if (serverWorld2 == null) {
                    return;
                }
                PointOfInterestStorage pointOfInterestStorage = serverWorld2.getPointOfInterestStorage();
                if (pointOfInterestStorage.test(blockPos, registryEntry -> true)) {
                    pointOfInterestStorage.releaseTicket(blockPos);
                }
            });
            /* PATCH END */
            DebugInfoSender.sendPointOfInterest(serverWorld, blockPos);
        });
        villagerEntity.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
    }
}
