package org.boosted.mixin.getServer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PiglinBrain.class)
public class PiglinBrainMixin {
    @Inject(method = "getBarteredItem(Lnet/minecraft/entity/mob/PiglinEntity;)Ljava/util/List;",
        at = @At("HEAD"), cancellable = true)
    private static void getBarteredItem(PiglinEntity piglin, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (piglin.world instanceof ServerWorld serverWorld) {
            ObjectArrayList<ItemStack> itemStacks = serverWorld.getSynchronizedServer().readExp(server -> {
                LootTable lootTable = piglin.world.getServer().getLootManager().getTable(LootTables.PIGLIN_BARTERING_GAMEPLAY);
                ObjectArrayList<ItemStack> list = lootTable.generateLoot(new LootContext.Builder((ServerWorld) piglin.world)
                    .parameter(LootContextParameters.THIS_ENTITY, piglin)
                    .random(piglin.world.random)
                    .build(LootContextTypes.BARTER));
                return list;
            });
            cir.setReturnValue(itemStacks);
        }
    }
}
