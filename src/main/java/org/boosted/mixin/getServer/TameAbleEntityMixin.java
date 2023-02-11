package org.boosted.mixin.getServer;

import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(TameableEntity.class)
public class TameAbleEntityMixin {

    @Redirect(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/entity/passive/TameableEntity.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    public MinecraftServer redirectGetServer(TameableEntity instance) {
        if (instance.getWorld() instanceof ServerWorld serverWorld) {
            return serverWorld.getUnsynchronizedServer();
        }
        return null;
    }

    @Redirect(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V",
        at = @At(value = "INVOKE", target = "net/minecraft/server/ServerConfigHandler.getPlayerUuidByName (Lnet/minecraft/server/MinecraftServer;Ljava/lang/String;)Ljava/util/UUID;"))
    public UUID redirectGetPlayerUuidByName(MinecraftServer server, String name) {
        return server.getSynchronizedServer()
            .writeExp(minecraftServer -> ServerConfigHandler.getPlayerUuidByName(minecraftServer, name));
    }
}
