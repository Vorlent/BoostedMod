package org.boosted.mixin.getServer;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VehicleInventory.class)
public class VehicleInventoryMixin {

    @Inject(method = "generateInventoryLoot(Lnet/minecraft/entity/player/PlayerEntity;)V", cancellable = true, at = @At("HEAD"))
    public void generateInventoryLoot(PlayerEntity player, CallbackInfo ci) {
        VehicleInventory vehicleInventory = (VehicleInventory) (Object) this;
        World world = vehicleInventory.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.getSynchronizedServer().read(minecraftServer -> {
                if (vehicleInventory.getLootTableId() != null && minecraftServer != null) {
                    LootTable lootTable = minecraftServer.getLootManager().getTable(vehicleInventory.getLootTableId());
                    if (player != null) {
                        Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, vehicleInventory.getLootTableId());
                    }
                    vehicleInventory.setLootTableId(null);
                    LootContext.Builder builder = new LootContext.Builder(serverWorld)
                            .parameter(LootContextParameters.ORIGIN, vehicleInventory.getPos()).random(vehicleInventory.getLootTableSeed());
                    if (player != null) {
                        builder.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
                    }
                    lootTable.supplyInventory(vehicleInventory, builder.build(LootContextTypes.CHEST));
                }
            });
            ci.cancel();
        }
    }

}
