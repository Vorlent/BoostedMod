package org.boosted.mixin.getServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/block/entity/CommandBlockBlockEntity$1")
public class CommandBlockBlockEntityMixin {

    @Redirect(method = "getSource",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getServer()Lnet/minecraft/server/MinecraftServer;"))
    public MinecraftServer redirect$getSource$getServer(ServerWorld instance) {
        return instance.getUnsynchronizedServer(); // command execution is already wrapped with synchronization
    }
}
