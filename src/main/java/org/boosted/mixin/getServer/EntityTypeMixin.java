package org.boosted.mixin.getServer;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

import static net.minecraft.entity.EntityType.ENTITY_TAG_KEY;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin {
    @Shadow public abstract boolean isFireImmune();

    @Redirect(method = "loadFromEntityNbt(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/nbt/NbtCompound;)V",
    at = @At(value = "INVOKE", target = "net/minecraft/world/World.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    private static MinecraftServer redirectGetServer(World instance) {
        if (instance instanceof ServerWorld serverWorld) {
            return serverWorld.getUnsynchronizedServer();
        } else {
            return instance.getServer();
        }
    }

    private static final ThreadLocal<MinecraftServer> unsynchronizedMinecraftServer = new ThreadLocal<>();

    @Redirect(method = "loadFromEntityNbt(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getPlayerManager ()Lnet/minecraft/server/PlayerManager;"))
    private static PlayerManager redirectGetPlayerManagerServer(MinecraftServer instance) {
        unsynchronizedMinecraftServer.set(instance);
        return null;
    }

    @Redirect(method = "loadFromEntityNbt(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/PlayerManager.isOperator (Lcom/mojang/authlib/GameProfile;)Z"))
    private static boolean redirectIsOperator(PlayerManager instance, GameProfile profile) {
        boolean isOperator = unsynchronizedMinecraftServer.get().getSynchronizedServer()
                .readExp(server -> server.getPlayerManager().isOperator(profile));

        unsynchronizedMinecraftServer.remove();
        return isOperator;
    }
}
