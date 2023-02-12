package org.boosted.mixin.getServer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.ForgetCompletedPointOfInterestTask;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ForgetCompletedPointOfInterestTask.class)
public abstract class ForgetCompletedPointOfInterestTaskMixin {
    @Shadow @Final public MemoryModuleType<GlobalPos> memoryModule;

    @Shadow public abstract boolean hasCompletedPointOfInterest(ServerWorld world, BlockPos pos);

    @Shadow public abstract boolean isBedOccupiedByOthers(ServerWorld world, BlockPos pos, LivingEntity entity);

    /**
     * @author Vorlent
     * @reason This mixin needs exclusive write access to minecraft server,
     * it may also potentially hold cross world references.
     */
    @Overwrite
    protected void run(ServerWorld world, LivingEntity entity, long time) {
        //ForgetCompletedPointOfInterestTask task = (ForgetCompletedPointOfInterestTask)(Object)this;
        Brain<?> brain = entity.getBrain();
        GlobalPos globalPos = brain.getOptionalMemory(this.memoryModule).get();
        BlockPos blockPos = globalPos.getPos();
        /* PATCH BEGIN */
        world.getSynchronizedServer().write(server -> {
            ServerWorld serverWorld = server.getWorld(globalPos.getDimension());
            if (serverWorld == null || this.hasCompletedPointOfInterest(serverWorld, blockPos)) {
                brain.forget(this.memoryModule);
            } else if (this.isBedOccupiedByOthers(serverWorld, blockPos, entity)) {
                brain.forget(this.memoryModule);
                world.getPointOfInterestStorage().releaseTicket(blockPos);
                DebugInfoSender.sendPointOfInterest(world, blockPos);
            }
        });
        /* PATCH END */
    }
}
