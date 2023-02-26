package org.boosted.mixin.serverplayerentity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Shadow @Final public MinecraftServer server;

    @Redirect(method = "moveToSpawn",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getSpawnRadius(Lnet/minecraft/server/world/ServerWorld;)I"))
    private int redirect$moveToSpawn$GetSpawnRadius(MinecraftServer instance, ServerWorld world) {
        return instance.getSynchronizedServer().readExp(server -> server.getSpawnRadius(world));
    }

    @Redirect(method = "readCustomDataFromNbt",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerRecipeBook;readNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/recipe/RecipeManager;)V"))
    private void redirect$readCustomDataFromNbt$ReadNbt(ServerRecipeBook instance, NbtCompound nbt, RecipeManager recipeManager) {
        this.server.getSynchronizedServer()
            .read(server -> instance.readNbt(nbt, server.getRecipeManager()));
    }

    @Redirect(method = "readCustomDataFromNbt",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"))
    private RecipeManager redirect$readCustomDataFromNbt$GetRecipeManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "onDeath",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;"))
    private PlayerManager redirect$onDeath$GetPlayerManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "onDeath",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    private void redirect$onDeath$GetPlayerManagerBroadcast(PlayerManager instance, Text message, boolean overlay) {
        this.server.getSynchronizedServer()
            .write(server -> server.getPlayerManager().broadcast(message, overlay));
    }

    @Redirect(method = "onDeath",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToTeam(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/text/Text;)V"))
    private void redirect$onDeath$GetPlayerManagerSendToTeam(PlayerManager instance, PlayerEntity source, Text message) {
        this.server.getSynchronizedServer()
            .write(server -> server.getPlayerManager().sendToTeam(source, message));
    }

    @Redirect(method = "onDeath",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToOtherTeams(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/text/Text;)V"))
    private void redirect$onDeath$GetPlayerManagerSendToOtherTeams(PlayerManager instance, PlayerEntity source, Text message) {
        this.server.getSynchronizedServer()
            .write(server -> server.getPlayerManager().sendToOtherTeams(source, message));
    }

    @Redirect(method = "damage",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isDedicated()Z"))
    private boolean redirect$damage$IsDedicated(MinecraftServer instance) {
        return this.server.getSynchronizedServer()
            .readExp(server -> server.isDedicated());
    }

    @Redirect(method = "isPvpEnabled",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isPvpEnabled()Z"))
    private boolean redirectIsPvpEnabled(MinecraftServer instance) {
        return this.server.getSynchronizedServer()
            .readExp(server -> server.isPvpEnabled());
    }

    @Redirect(method = "moveToWorld",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;"))
    private PlayerManager redirect$moveToWorld$getPlayerManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "moveToWorld",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void redirect$moveToWorld$sendCommandTree(PlayerManager instance, ServerPlayerEntity player) {
        player.server.getSynchronizedServer().write(server -> server.getPlayerManager().sendCommandTree(player));
    }

    @Redirect(method = "moveToWorld",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendWorldInfo(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V"))
    private void redirect$moveToWorld$sendWorldInfo(PlayerManager instance, ServerPlayerEntity player, ServerWorld world) {
        player.server.getSynchronizedServer().write(server -> server.getPlayerManager().sendWorldInfo(player, world));
    }

    @Redirect(method = "moveToWorld",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendPlayerStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void redirect$moveToWorld$sendPlayerStatus(PlayerManager instance, ServerPlayerEntity player) {
        player.server.getSynchronizedServer().write(server -> server.getPlayerManager().sendPlayerStatus(player));
    }


    @Redirect(method = "unlockRecipes([Lnet/minecraft/util/Identifier;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"))
    private RecipeManager redirect$unlockRecipes$GetRecipeManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "unlockRecipes([Lnet/minecraft/util/Identifier;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;get(Lnet/minecraft/util/Identifier;)Ljava/util/Optional;"))
    private Optional<? extends Recipe<?>> redirect$unlockRecipes$get(RecipeManager instance, Identifier id) {
        return this.server.getSynchronizedServer()
            .readExp(server -> server.getRecipeManager().get(id));
    }

    @Redirect(method = "getPermissionLevel",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPermissionLevel(Lcom/mojang/authlib/GameProfile;)I"))
    private int redirect$getPermissionLevel$getPermissionLevel(MinecraftServer instance, GameProfile profile) {
        return this.server.getSynchronizedServer()
            .readExp(server -> server.getPermissionLevel(profile));
    }

    @Redirect(method = "teleport",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;"))
    private PlayerManager redirect$teleport$GetPlayerManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "teleport",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void redirect$teleport$sendCommandTree(PlayerManager instance, ServerPlayerEntity player) {
        this.server.getSynchronizedServer()
            .write(server -> server.getPlayerManager().sendCommandTree(player));
    }

    @Redirect(method = "teleport",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendWorldInfo(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V"))
    private void redirect$teleport$sendWorldInfo(PlayerManager instance, ServerPlayerEntity player, ServerWorld world) {
        this.server.getSynchronizedServer()
            .write(server -> server.getPlayerManager().sendWorldInfo(player, world));
    }

    @Redirect(method = "teleport",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendPlayerStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void redirect$teleport$sendPlayerStatus(PlayerManager instance, ServerPlayerEntity player) {
        this.server.getSynchronizedServer()
            .write(server -> server.getPlayerManager().sendPlayerStatus(player));
    }

    @Redirect(method = "getServerGameMode",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getForcedGameMode()Lnet/minecraft/world/GameMode;"))
    private GameMode redirect$getServerGameMode$getForcedGameMode(MinecraftServer instance) {
        return this.server.getSynchronizedServer()
                .readExp(server -> server.getForcedGameMode());
    }

    @Redirect(method = "getServerGameMode",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getDefaultGameMode()Lnet/minecraft/world/GameMode;"))
    private GameMode redirect$getServerGameMode$getDefaultGameMode(MinecraftServer instance) {
        return this.server.getSynchronizedServer()
                .readExp(server -> server.getDefaultGameMode());
    }

}
