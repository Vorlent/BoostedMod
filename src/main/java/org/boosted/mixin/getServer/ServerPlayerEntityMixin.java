package org.boosted.mixin.getServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.SaveProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Redirect(method = "moveToSpawn(Lnet/minecraft/server/world/ServerWorld;)V",
        at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    private MinecraftServer redirectGetServer(ServerWorld world) {
        return null;
    }

    @Redirect(method = "moveToSpawn(Lnet/minecraft/server/world/ServerWorld;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getSaveProperties ()Lnet/minecraft/world/SaveProperties;"))
    private SaveProperties redirectGetSaveProperties(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "moveToSpawn(Lnet/minecraft/server/world/ServerWorld;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/world/SaveProperties.getGameMode ()Lnet/minecraft/world/GameMode;"))
    private GameMode redirectGetGameMode(SaveProperties instance) {
        return ((ServerPlayerEntity)(Object)this).getWorld().getSynchronizedServer().readExp(server
            -> server.getSaveProperties().getGameMode());
    }
}
