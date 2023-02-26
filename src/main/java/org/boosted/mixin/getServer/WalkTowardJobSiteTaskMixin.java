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
import org.boosted.ThreadCoordinator;
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
    public void finishRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        Optional<GlobalPos> optional = villagerEntity.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
        optional.ifPresent(pos -> {
            BlockPos blockPos = pos.getPos();
            /* PATCH BEGIN */
            ThreadCoordinator.getInstance().getBoostedContext().postTick().execute(() -> { // run cross world logic on the main thread
                serverWorld.getSynchronizedServer().write(server -> {
                    ServerWorld serverWorld2 = server.getWorld(pos.getDimension());
                    if (serverWorld2 == null) {
                        return;
                    }
                    PointOfInterestStorage pointOfInterestStorage = serverWorld2.getPointOfInterestStorage();
                    if (pointOfInterestStorage.test(blockPos, registryEntry -> true)) {
                        pointOfInterestStorage.releaseTicket(blockPos);
                    }

                    DebugInfoSender.sendPointOfInterest(serverWorld, blockPos);
                });
            });
            /* PATCH END */
        });
        villagerEntity.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
    }
}
