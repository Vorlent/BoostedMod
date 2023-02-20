package org.boosted.mixin.getServer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net/minecraft/entity/passive/CatEntity$SleepWithOwnerGoal")
public class CatEntityMixin {


    @Shadow @Final public CatEntity cat;

    /*@Inject(method = "dropMorningGifts()V", cancellable = true,
        at = @At(value = "FIELD", target = "net/minecraft/entity/passive/CatEntity$SleepWithOwnerGoal.cat : Lnet/minecraft/entity/passive/CatEntity;",
        ordinal = 0),
        slice = @Slice(from = @At(value = "FIELD", target = "net/minecraft/entity/passive/CatEntity.world : Lnet/minecraft/world/World;", shift = At.Shift.BEFORE),
            to = @At(value = "INVOKE", target = "net/minecraft/world/World.getServer ()Lnet/minecraft/server/MinecraftServer;")))
    private void test(CallbackInfo ci) {

    }*/

    // TODO confirm that these work as intended

    @Inject(method = "dropMorningGifts()V", cancellable = true,
        at = @At(value = "INVOKE", target = "net/minecraft/world/World.getServer ()Lnet/minecraft/server/MinecraftServer;", shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void test(CallbackInfo ci,  Random random, BlockPos.Mutable mutable) {
        /* PATCH BEGIN */
        if (!(cat.getWorld() instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) cat.getWorld();
        ObjectArrayList<ItemStack> list = serverWorld.getSynchronizedServer().readExp(server -> {
            LootTable lootTable = server.getLootManager().getTable(LootTables.CAT_MORNING_GIFT_GAMEPLAY);
            LootContext.Builder builder = new LootContext.Builder(serverWorld)
                    .parameter(LootContextParameters.ORIGIN, cat.getPos())
                    .parameter(LootContextParameters.THIS_ENTITY, cat)
                    .random(random);
            return lootTable.generateLoot(builder.build(LootContextTypes.GIFT));
        });
        /* PATCH END */
        for (ItemStack itemStack : list) {
            cat.world.spawnEntity(new ItemEntity(cat.world,
                    (double) mutable.getX() - (double) MathHelper.sin(cat.bodyYaw * ((float) Math.PI / 180)),
                    mutable.getY(),
                    (double) mutable.getZ() + (double) MathHelper.cos(cat.bodyYaw * ((float) Math.PI / 180)), itemStack));
        }
    }


    /**
     * @author Vorlent
     * @reason the method needs exclusive read access to minecraft server
     */
    //@Overwrite
    private void dropMorningGifts() {
        CatEntity cat = this.cat;
        Random random = cat.getRandom();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        mutable.set(cat.getBlockPos());
        cat.teleport(mutable.getX() + random.nextInt(11) - 5, mutable.getY() + random.nextInt(5) - 2, mutable.getZ() + random.nextInt(11) - 5, false);
        mutable.set(cat.getBlockPos());
        /* PATCH BEGIN */
        if (!(cat.getWorld() instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) cat.getWorld();
        ObjectArrayList<ItemStack> list = serverWorld.getSynchronizedServer().readExp(server -> {
            LootTable lootTable = server.getLootManager().getTable(LootTables.CAT_MORNING_GIFT_GAMEPLAY);
            LootContext.Builder builder = new LootContext.Builder(serverWorld)
                .parameter(LootContextParameters.ORIGIN, cat.getPos())
                .parameter(LootContextParameters.THIS_ENTITY, cat)
                .random(random);
            return lootTable.generateLoot(builder.build(LootContextTypes.GIFT));
        });
        /* PATCH END */
        for (ItemStack itemStack : list) {
            cat.world.spawnEntity(new ItemEntity(cat.world,
                (double) mutable.getX() - (double) MathHelper.sin(cat.bodyYaw * ((float) Math.PI / 180)),
                mutable.getY(),
                (double) mutable.getZ() + (double) MathHelper.cos(cat.bodyYaw * ((float) Math.PI / 180)), itemStack));
        }

    }
}
