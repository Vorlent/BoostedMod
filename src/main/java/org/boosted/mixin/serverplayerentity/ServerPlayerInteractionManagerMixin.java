package org.boosted.mixin.serverplayerentity;

import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow @Final protected ServerPlayerEntity player;

    @Redirect(method = "setGameMode",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;"))
    private PlayerManager redirect$setGameMode$getPlayerManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "setGameMode",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    private void redirect$setGameMode$getPlayerManager(PlayerManager instance, Packet<?> packet) {
        this.player.server.getSynchronizedServer()
            .write(server -> server.getPlayerManager().sendToAll(packet));
    }
}
