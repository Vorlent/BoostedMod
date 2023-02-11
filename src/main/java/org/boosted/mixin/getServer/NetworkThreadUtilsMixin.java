package org.boosted.mixin.getServer;

import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetworkThreadUtils.class)
public class NetworkThreadUtilsMixin {

    @Redirect(method = "forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
        at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    private static MinecraftServer redirectGetServer(ServerWorld instance) {
        return instance.getUnsynchronizedServer(); // the minecraft server is used as thread executor which is oks
    }
}
