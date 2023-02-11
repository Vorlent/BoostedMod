package org.boosted.mixin.getServer;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;

import static net.minecraft.entity.passive.VillagerEntity.POINTS_OF_INTEREST;

@Mixin(AbstractHorseEntity.class)
public class AbstractHorseEntityMixin {

    @Redirect(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/entity/passive/AbstractHorseEntity.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    public MinecraftServer redirectGetServer(AbstractHorseEntity instance) {
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
