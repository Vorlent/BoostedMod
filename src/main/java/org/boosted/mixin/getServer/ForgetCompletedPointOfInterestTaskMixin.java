package org.boosted.mixin.getServer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.task.ForgetCompletedPointOfInterestTask;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ForgetCompletedPointOfInterestTask.class)
public class ForgetCompletedPointOfInterestTaskMixin {
    /**
     * @author Vorlent
     * @reason This mixin needs exclusive read access to minecraft server,
     * it may also potentially hold cross world references.
     */
    @Overwrite
    protected void run(ServerWorld world, LivingEntity entity, long time) {
        ForgetCompletedPointOfInterestTask task = (ForgetCompletedPointOfInterestTask)(Object)this;
        Brain<?> brain = entity.getBrain();
        GlobalPos globalPos = brain.getOptionalMemory(task.memoryModule).get();
        BlockPos blockPos = globalPos.getPos();
        ServerWorld serverWorld = world.getServer().getWorld(globalPos.getDimension());
        if (serverWorld == null || task.hasCompletedPointOfInterest(serverWorld, blockPos)) {
            brain.forget(task.memoryModule);
        } else if (task.isBedOccupiedByOthers(serverWorld, blockPos, entity)) {
            brain.forget(task.memoryModule);
            world.getPointOfInterestStorage().releaseTicket(blockPos);
            DebugInfoSender.sendPointOfInterest(world, blockPos);
        }
    }
}
