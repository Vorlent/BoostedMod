package org.boosted.mixin.getServer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "dropLoot(Lnet/minecraft/entity/damage/DamageSource;Z)V", at = @At("HEAD"), cancellable = true)
    protected void dropLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity.world instanceof ServerWorld serverWorld) {
            serverWorld.getSynchronizedServer().read(server -> {
                Identifier identifier = livingEntity.getLootTable();
                LootTable lootTable = server.getLootManager().getTable(identifier);
                LootContext.Builder builder = livingEntity.getLootContextBuilder(causedByPlayer, source);
                lootTable.generateLoot(builder.build(LootContextTypes.ENTITY), livingEntity::dropStack);
                ci.cancel();
            });
        }
    }
}
