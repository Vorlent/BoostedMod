package org.boosted.mixin.getServer;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/server/world/ThreadedAnvilChunkStorage$EntityTracker")
public class ThreadedAnvilChunkStorageMixin {

    @Shadow @Final public Entity entity;

    @Inject(method = "adjustTrackingDistance(I)I", cancellable = true,
        at = @At("HEAD"))
    private void adjustTrackingDistance(int initialDistance, CallbackInfoReturnable<Integer> cir) {
        if (this.entity.getWorld() instanceof ServerWorld serverWorld) {
            Integer result = serverWorld.getSynchronizedServer().writeExp(server -> server.adjustTrackingDistance(initialDistance));
            cir.setReturnValue(result);
        }
    }
}
