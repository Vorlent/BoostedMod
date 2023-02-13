package org.boosted.mixin.getServer;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.task.ForgetCompletedPointOfInterestTask;
import net.minecraft.entity.ai.brain.task.GiveGiftsToHeroTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

import static net.minecraft.entity.ai.brain.task.GiveGiftsToHeroTask.GIFTS;

@Mixin(GiveGiftsToHeroTask.class)
public class GiveGiftsToHeroTaskMixin {
    /**
     * @author Vorlent
     * @reason getGifts needs exclusive read access to MinecraftServer
     */
    @Overwrite
    private List<ItemStack> getGifts(VillagerEntity villager) {
        if (villager.isBaby()) {
            return ImmutableList.of(new ItemStack(Items.POPPY));
        }
        VillagerProfession villagerProfession = villager.getVillagerData().getProfession();
        if (GIFTS.containsKey(villagerProfession)) {
            /* PATCH BEGIN */
            ServerWorld serverWorld = (ServerWorld)villager.world;
            return serverWorld.getSynchronizedServer().readExp(server -> {
                LootTable lootTable = server.getLootManager().getTable(GIFTS.get(villagerProfession));
                LootContext.Builder builder = new LootContext.Builder((ServerWorld)villager.world)
                        .parameter(LootContextParameters.ORIGIN, villager.getPos())
                        .parameter(LootContextParameters.THIS_ENTITY, villager)
                        .random(villager.getRandom());
                return lootTable.generateLoot(builder.build(LootContextTypes.GIFT));
            });
            /* PATCH END */
        }
        return ImmutableList.of(new ItemStack(Items.WHEAT_SEEDS));
    }
}
