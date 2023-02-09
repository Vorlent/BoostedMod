package org.boosted.mixin.getServer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.UUID;

import static net.minecraft.entity.EntityType.ENTITY_TAG_KEY;

@Mixin(EntityType.class)
public class EntityTypeMixin {

    /**
     * @author Vorlent
     * @reason We need to ensure exclusive read access to MinecraftServer
     */
    @Overwrite
    public static void loadFromEntityNbt(World world, @Nullable PlayerEntity player, @Nullable Entity entity, @Nullable NbtCompound itemNbt) {
        if (itemNbt == null || !itemNbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) {
            return;
        }
        /* Patch BEGIN */
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) world;
        if (serverWorld.getSynchronizedServer().readExp(server ->
                server == null || entity == null
                || !(world.isClient || !entity.entityDataRequiresOperator() || player != null
                    && server.getPlayerManager().isOperator(player.getGameProfile())))) {
            return;
        }
        /* Patch END */
        NbtCompound nbtCompound = entity.writeNbt(new NbtCompound());
        UUID uUID = entity.getUuid();
        nbtCompound.copyFrom(itemNbt.getCompound(ENTITY_TAG_KEY));
        entity.setUuid(uUID);
        entity.readNbt(nbtCompound);
    }
}
