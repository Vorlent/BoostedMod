package org.boosted.mixin.getServer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.tag.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {

    /**
     * @author Vorlent
     * @reason This mixin needs exclusive read access to the MinecraftServer for loot generation
     */
    @Overwrite
    public int use(ItemStack usedItem) {
        FishingBobberEntity entity = (FishingBobberEntity)(Object)this;
        PlayerEntity playerEntity = entity.getPlayerOwner();
        if (entity.world.isClient || playerEntity == null || entity.removeIfInvalid(playerEntity)) {
            return 0;
        }
        int i = 0;
        if (entity.hookedEntity != null) {
            entity.pullHookedEntity(entity.hookedEntity);
            Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, usedItem, entity, Collections.emptyList());
            entity.world.sendEntityStatus(entity, EntityStatuses.PULL_HOOKED_ENTITY);
            i = entity.hookedEntity instanceof ItemEntity ? 3 : 5;
        } else if (entity.hookCountdown > 0) {
            LootContext.Builder builder = new LootContext.Builder((ServerWorld)entity.world).parameter(LootContextParameters.ORIGIN, entity.getPos()).parameter(LootContextParameters.TOOL, usedItem).parameter(LootContextParameters.THIS_ENTITY, entity).random(entity.random).luck((float)entity.luckOfTheSeaLevel + playerEntity.getLuck());
            /* PATCH BEGIN */
            ServerWorld serverWorld = (ServerWorld) entity.world;
            ObjectArrayList<ItemStack> list = serverWorld.getSynchronizedServer().readExp(server -> {
                LootTable lootTable = server.getLootManager().getTable(LootTables.FISHING_GAMEPLAY);
                return lootTable.generateLoot(builder.build(LootContextTypes.FISHING));
            });
            /* PATCH END */
            Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, usedItem, entity, list);
            for (ItemStack itemStack : list) {
                ItemEntity itemEntity = new ItemEntity(entity.world, entity.getX(), entity.getY(), entity.getZ(), itemStack);
                double d = playerEntity.getX() - entity.getX();
                double e = playerEntity.getY() - entity.getY();
                double f = playerEntity.getZ() - entity.getZ();
                double g = 0.1;
                itemEntity.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
                entity.world.spawnEntity(itemEntity);
                playerEntity.world.spawnEntity(new ExperienceOrbEntity(playerEntity.world, playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ() + 0.5, entity.random.nextInt(6) + 1));
                if (!itemStack.isIn(ItemTags.FISHES)) continue;
                playerEntity.increaseStat(Stats.FISH_CAUGHT, 1);
            }
            i = 1;
        }
        if (entity.isOnGround()) {
            i = 2;
        }
        entity.discard();
        return i;
    }
}
