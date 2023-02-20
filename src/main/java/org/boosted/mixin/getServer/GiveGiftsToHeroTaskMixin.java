package org.boosted.mixin.getServer;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.minecraft.entity.ai.brain.task.GiveGiftsToHeroTask.GIFTS;

@Mixin(GiveGiftsToHeroTask.class)
public class GiveGiftsToHeroTaskMixin {

    @Inject(method = "getGifts(Lnet/minecraft/entity/passive/VillagerEntity;)Ljava/util/List;", cancellable = true,
        at = @At(value = "FIELD", target = "net/minecraft/entity/passive/VillagerEntity.world : Lnet/minecraft/world/World;",
        ordinal = 0, shift = At.Shift.BEFORE))
    private void injectGetGifts(VillagerEntity villager, CallbackInfoReturnable<List<ItemStack>> cir) {
        ServerWorld serverWorld = (ServerWorld)villager.world;
        ObjectArrayList<ItemStack> itemStacks = serverWorld.getSynchronizedServer().readExp(server -> {
            VillagerProfession villagerProfession = villager.getVillagerData().getProfession();
            LootTable lootTable = server.getLootManager().getTable(GIFTS.get(villagerProfession));
            LootContext.Builder builder = new LootContext.Builder((ServerWorld) villager.world)
                    .parameter(LootContextParameters.ORIGIN, villager.getPos())
                    .parameter(LootContextParameters.THIS_ENTITY, villager)
                    .random(villager.getRandom());
            return lootTable.generateLoot(builder.build(LootContextTypes.GIFT));
        });
        cir.setReturnValue(itemStacks);
    }
}
