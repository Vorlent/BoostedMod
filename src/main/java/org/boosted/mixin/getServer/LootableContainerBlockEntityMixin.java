package org.boosted.mixin.getServer;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LootableContainerBlockEntity.class)
public class LootableContainerBlockEntityMixin {

    private final ThreadLocal<Identifier> prevLootTableId = new ThreadLocal<>();

    @Redirect(method = "checkLootInteraction(Lnet/minecraft/entity/player/PlayerEntity;)V",
        at = @At(value = "INVOKE", target = "net/minecraft/world/World.getServer ()Lnet/minecraft/server/MinecraftServer;",
        ordinal = 0))
    public MinecraftServer redirectGetUnsynchronizedServer(World instance) {
        if (instance instanceof ServerWorld serverWorld) {
            return serverWorld.getUnsynchronizedServer();
        }
        return instance.getServer();
    }

    @Redirect(method = "checkLootInteraction(Lnet/minecraft/entity/player/PlayerEntity;)V",
        at = @At(value = "INVOKE", target = "net/minecraft/world/World.getServer ()Lnet/minecraft/server/MinecraftServer;",
        ordinal = 1))
    public MinecraftServer skipGetServer(World instance) {
        return null;
    }

    @Redirect(method = "checkLootInteraction(Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getLootManager ()Lnet/minecraft/loot/LootManager;"))
    public LootManager skipGetLootManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "checkLootInteraction(Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/loot/LootManager.getTable (Lnet/minecraft/util/Identifier;)Lnet/minecraft/loot/LootTable;"))
    public LootTable skipGetTable(LootManager instance, Identifier id) {
        prevLootTableId.set(id);
        return null;
    }

    @Redirect(method = "checkLootInteraction(Lnet/minecraft/entity/player/PlayerEntity;)V",
        at = @At(value = "INVOKE", target = "net/minecraft/loot/LootTable.supplyInventory (Lnet/minecraft/inventory/Inventory;Lnet/minecraft/loot/context/LootContext;)V"))
    public void redirectSupplyInventory(LootTable instance, Inventory inventory, LootContext context) {
        Identifier identifier = prevLootTableId.get();
        prevLootTableId.remove();
        LootableContainerBlockEntity entity = (LootableContainerBlockEntity)(Object)this;

        World world = entity.getWorld();
         if (world instanceof ServerWorld serverWorld) {
            serverWorld.getSynchronizedServer().write(server -> {
                LootTable lootTable = server.getLootManager().getTable(identifier);
                lootTable.supplyInventory(inventory, context);
            });
        }
    }
}
