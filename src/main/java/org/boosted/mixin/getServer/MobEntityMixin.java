package org.boosted.mixin.getServer;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Redirect(method = "tickNewAi()V",
        at = @At(value = "INVOKE", target = "net/minecraft/world/World.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    private MinecraftServer redirectGetServer(World instance) {
        return null;
    }

    @Redirect(method = "tickNewAi()V",
        at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getTicks ()I"))
    private int redirectGetServer(MinecraftServer instance) {
        MobEntity mobEntity = (MobEntity) (Object) this;
        World world = mobEntity.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            return serverWorld.getSynchronizedServer().readExp(server -> server.getTicks());
        }
        return world.getServer().getTicks();
    }
}
